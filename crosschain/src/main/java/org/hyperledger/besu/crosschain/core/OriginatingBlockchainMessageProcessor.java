/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.hyperledger.besu.crosschain.core;

import org.hyperledger.besu.crosschain.core.keys.CrosschainKeyManager;
import org.hyperledger.besu.crosschain.core.messages.CrosschainTransactionCommitMessage;
import org.hyperledger.besu.crosschain.core.messages.CrosschainTransactionStartMessage;
import org.hyperledger.besu.crosschain.core.messages.SubordinateTransactionReadyMessage;
import org.hyperledger.besu.crosschain.core.messages.ThresholdSignedMessage;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsCryptoProvider;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Hash;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.tuples.generated.Tuple2;

/**
 * Does the coordination of the processing required for the Crosschain Transaction Start, Commit,
 * and Ignore messages.
 */
public class OriginatingBlockchainMessageProcessor {
  private static final Logger LOG = LogManager.getLogger();

  CrosschainKeyManager keyManager;
  CoordContractManager coordContractManager;
  SECP256K1.KeyPair nodeKeys;
  Map<BigInteger, Tuple2<CrosschainTransaction, Set<Hash>>> txToBeMined;

  public OriginatingBlockchainMessageProcessor(
      final CrosschainKeyManager keyManager, final CoordContractManager coordContractManager) {
    this.keyManager = keyManager;
    this.coordContractManager = coordContractManager;
    this.txToBeMined = new HashMap<BigInteger, Tuple2<CrosschainTransaction, Set<Hash>>>();
  }

  public void init(final SECP256K1.KeyPair nodeKeys) {
    this.nodeKeys = nodeKeys;
  }

  /**
   * Start message processing: - Get the current Coordination Contract block number. - Validate that
   * the number in the transaction is greater than what the current block number is. - Create the
   * message. - Threshold sign it with this node and other validators. - Upload it to the
   * appropriate Crosschain Coordination Contract.
   *
   * @param transaction Originating (with enclosed subordinates) Transaction to kick off the start.
   */
  public void doStartMessageMagic(final CrosschainTransaction transaction) {
    // If this is an originating transaction, then we are sure the optional fields will exist.
    BigInteger coordBcId = transaction.getCrosschainCoordinationBlockchainId().get();
    Address coordContractAddress = transaction.getCrosschainCoordinationContractAddress().get();

    // We must trust the Crosschain Coordination Contract to proceed.
    String ipAndPort = this.coordContractManager.getIpAndPort(coordBcId, coordContractAddress);
    if (ipAndPort == null) {
      String msg =
          "Crosschain Transaction uses unknown Coordination Blockchain and Address combination "
              + "Blockchain: 0x"
              + coordBcId.toString(16)
              + ", Address: "
              + coordContractAddress.getHexString();
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    // TODO We could get block number from Coordination blockchain, and get the timeout block
    // number from the transaction, and check whether there will be enough time to execute the
    // transaction. A simpler approach could be to just specify that there must be at least
    // a certain number of blocks between when the message is submitted to the Coordination
    // Blockchain
    // and when it is executed.

    // Create message to be signed.
    ThresholdSignedMessage message = new CrosschainTransactionStartMessage(transaction);

    // Cooperate with other nodes to threshold sign the message.
    this.keyManager.thresholdSign(message);
    // Now the message is signed.

    // Submit message to Coordination Contract.
    boolean startedOK =
        new OutwardBoundConnectionManager(this.nodeKeys)
            .coordContractStart(ipAndPort, coordBcId, coordContractAddress, message);
    LOG.info("started OK {}", startedOK);
  }

  /**
   * This method lists all the transactions that needs to be mined so as to start committing a
   * crosschain transaction.
   *
   * @param transaction The originating transaction containing all subordinate transactions
   */
  public void listMiningTxForCommit(final CrosschainTransaction transaction) {
    Set<Hash> txs = new HashSet<Hash>();
    txs.add(transaction.hash());
    for (CrosschainTransaction subTx : transaction.getSubordinateTransactionsAndViews()) {
      txs.add(subTx.hash());
    }
    Tuple2<CrosschainTransaction, Set<Hash>> val =
        new Tuple2<CrosschainTransaction, Set<Hash>>(transaction, txs);
    this.txToBeMined.put(transaction.getCrosschainTransactionId().get(), val);
  }

  /**
   * This method updates the txsToBeMined by removing the originating transaction, because it has
   * already been mined.
   *
   * @param txId Originating chain Id
   * @param origTxHash Originating transaction hash
   */
  public void removeOrigTxInsideToBeMined(final CrosschainTransaction origTx) {
    BigInteger txId = origTx.getCrosschainTransactionId().get();
    Tuple2<CrosschainTransaction, Set<Hash>> val = this.txToBeMined.get(txId);
    Set<Hash> txs = val.component2();
    txs.remove(origTx.hash());
    if (txs.isEmpty()) {
      LOG.info(
          "All transaction ready messages have been received. Mining of the "
              + "originating transaction has been the last. Send commit message.");
      sendCommitMsg(origTx);
    }
    this.txToBeMined.replace(txId, val);
  }

  /**
   * This method is called after all the originating and subordinate transactions are mined, and
   * after receiving the subordinateTransactionReady message. This method will update the
   * txsToBeMined set by removing the already mined transaction.
   *
   * @param subTxReadyMsg The SubordinateTransactionReady message received from the subordinate
   *     chain.
   * @return Returns true if there is any error, otherwise false.
   */
  public boolean receiveSubTxReadyMsg(final SubordinateTransactionReadyMessage subTxReadyMsg) {
    BigInteger coordChainId = subTxReadyMsg.getCoordChainId();
    BigInteger subChainId = subTxReadyMsg.getSubChainId();
    Address coordAddress = subTxReadyMsg.getCoordAddress();

    String coordIpAddrAndPort = coordContractManager.getIpAndPort(coordChainId, coordAddress);
    BigInteger publicKey =
        new OutwardBoundConnectionManager(this.nodeKeys)
            .getPublicKeyFromCoordContract(
                coordIpAddrAndPort,
                coordChainId,
                coordAddress,
                subChainId,
                subTxReadyMsg.getKeyVersion());
    LOG.info(
        "Obtained the public key {} from crosschain coordination contract for chain {}.",
        publicKey.toString(16),
        subChainId.longValue());

    // Verify the signature
    BlsPoint publicKeyBlsPoint = BlsPoint.load(publicKey.toByteArray());

    BlsCryptoProvider cryptoProvider =
        BlsCryptoProvider.getInstance(
            BlsCryptoProvider.CryptoProviderTypes.LOCAL_ALT_BN_128,
            BlsCryptoProvider.DigestAlgorithm.KECCAK256);

    boolean signatureVerification =
        cryptoProvider.verify(
            publicKeyBlsPoint,
            subTxReadyMsg.getEncodedCoreMessage().extractArray(),
            BlsPoint.load(subTxReadyMsg.getSignature().getByteArray()));

    if (signatureVerification) {
      LOG.info("The signature of Subordinate Transaction Ready message verified.");
    } else {
      LOG.error("Verification of the subordinate transaction ready message's signature failed.");
      return true;
    }

    BigInteger origChainId = subTxReadyMsg.getOrigChainId();
    Tuple2<CrosschainTransaction, Set<Hash>> val = this.txToBeMined.get(origChainId);
    Set<Hash> txs = val.component2();
    txs.remove(subTxReadyMsg.getTxHash());
    if (txs.isEmpty()) {
      LOG.info("All transaction ready messages have been received. Sending the commit message.");
      sendCommitMsg(val.component1());
    }
    this.txToBeMined.replace(origChainId, val);
    return false;
  }

  private void sendCommitMsg(final CrosschainTransaction origTx) {
    // Construct the commit message.
    CrosschainTransactionCommitMessage msg = new CrosschainTransactionCommitMessage(origTx);
    // Sign it.
    this.keyManager.thresholdSign(msg);
    // Send it to the coordination contract
    new OutwardBoundConnectionManager(this.nodeKeys).sendCommitToCoordContract(origTx, msg);
  }
}
