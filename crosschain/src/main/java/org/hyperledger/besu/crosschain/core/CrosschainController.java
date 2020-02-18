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

import org.hyperledger.besu.crosschain.core.keys.BlsThresholdCryptoSystem;
import org.hyperledger.besu.crosschain.core.keys.BlsThresholdPublicKey;
import org.hyperledger.besu.crosschain.core.keys.CrosschainKeyManager;
import org.hyperledger.besu.crosschain.core.keys.KeyStatus;
import org.hyperledger.besu.crosschain.core.keys.generation.KeyGenFailureToCompleteReason;
import org.hyperledger.besu.crosschain.core.messages.SubordinateTransactionReadyMessage;
import org.hyperledger.besu.crosschain.core.messages.SubordinateViewResultMessage;
import org.hyperledger.besu.crosschain.ethereum.crosschain.CrosschainThreadLocalDataHolder;
import org.hyperledger.besu.crosschain.ethereum.storage.keyvalue.CrosschainNodeStorage;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.exception.InvalidJsonRpcRequestException;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.Account;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.WorldState;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.mainnet.MainnetTransactionProcessor;
import org.hyperledger.besu.ethereum.mainnet.TransactionProcessor;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.transaction.TransactionSimulator;
import org.hyperledger.besu.ethereum.transaction.TransactionSimulatorResult;
import org.hyperledger.besu.ethereum.worldstate.WorldStateArchive;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: This class needs to use (the main instance of) Vertx rather than blocking

/**
 * This class is initialised when the Ethereum Client starts up, and holds references to all of the
 * parts of the crosschain core code.
 */
public class CrosschainController {
  protected static final Logger LOG = LogManager.getLogger();

  TransactionPool transactionPool;
  Blockchain blockchain;
  WorldStateArchive worldStateArchive;
  CrosschainNodeStorage nodeStorage;

  CrosschainProcessor processor;
  OriginatingBlockchainMessageProcessor origMsgProcessor;
  CrosschainKeyManager crosschainKeyManager;

  LinkedNodeManager linkedNodeManager;
  CoordContractManager coordContractManager;

  public CrosschainController() {
    this.linkedNodeManager = new LinkedNodeManager();
    this.coordContractManager = new CoordContractManager();
    this.processor = new CrosschainProcessor(this.linkedNodeManager, this.coordContractManager);
    this.crosschainKeyManager = CrosschainKeyManager.getCrosschainKeyManager();
    this.origMsgProcessor =
        new OriginatingBlockchainMessageProcessor(
            this.crosschainKeyManager, this.coordContractManager);
  }

  public void init(
      final TransactionSimulator transactionSimulator,
      final TransactionPool transactionPool,
      final BigInteger sidechainId,
      final SECP256K1.KeyPair nodeKeys,
      final Blockchain blockchain,
      final WorldStateArchive worldStateArchive,
      final CrosschainNodeStorage nodeStorage) {
    this.crosschainKeyManager.init(sidechainId, nodeKeys);
    this.processor.init(
        transactionSimulator,
        transactionPool,
        sidechainId,
        nodeKeys,
        blockchain,
        worldStateArchive,
        this.crosschainKeyManager);
    this.origMsgProcessor.init(nodeKeys);
    this.transactionPool = transactionPool;
    this.blockchain = blockchain;
    this.worldStateArchive = worldStateArchive;
    this.nodeStorage = nodeStorage;
    nodeStorage.restoreNodeData(linkedNodeManager, coordContractManager, crosschainKeyManager);
    CrosschainThreadLocalDataHolder.controller = this;
  }

  /**
   * Execute a subordinate transaction.
   *
   * @param transaction Subordinate Transaction to execute.
   * @return Validation result.
   */
  public ValidationResult<TransactionValidator.TransactionInvalidReason> addLocalTransaction(
      final CrosschainTransaction transaction) {
    if (transaction.getType().isOriginatingTransaction()) {
      // TODO The start message stuff will take a while. The rest of the code should be executed in
      // some sort of "do later"
      origMsgProcessor.doStartMessageMagic(transaction);
      // Setup the list of to be mined (originating and subordinate) transactions.
      origMsgProcessor.listMiningTxForCommit(transaction);
    }

    // Get Subordinate View results.
    if (this.processor.processSubordinates(transaction, false)) {
      return ValidationResult.invalid(
          TransactionValidator.TransactionInvalidReason.CROSSCHAIN_FAILED_SUBORDINATE_VIEW);
    }

    Optional<ValidationResult<TransactionValidator.TransactionInvalidReason>> executionError =
        this.processor.trialExecution(transaction);
    if (executionError.isPresent()) {
      return executionError.get();
    }

    // Dispatch Subordinate Transactions if the trial execution worked OK.
    if (this.processor.processSubordinates(transaction, true)) {
      return ValidationResult.invalid(
          TransactionValidator.TransactionInvalidReason.CROSSCHAIN_FAILED_SUBORDINATE_TRANSACTION);
    }

    // TODO there is a synchronized inside this call. This should be surrounded by a Vertx
    // blockingExecutor, maybe
    ValidationResult<TransactionValidator.TransactionInvalidReason> validationResult =
        this.transactionPool.addLocalTransaction(transaction);

    if (transaction.getType().isSubordinateTransaction()
        || transaction.getType().isOriginatingTransaction()) {
      // Wait for the transaction to be mined. The transaction is deemed to be mined when the
      // transaction disappears from the list of pendingTransactions.
      while (this.transactionPool
          .getPendingTransactions()
          .containsTransaction(transaction.hash())) {
        try {
          Thread.sleep(1000);
        } catch (Exception e) {
          LOG.error(
              "Exception in Thread.sleep while waiting for the transaction to be mined: {}",
              e.toString());
        }
      }

      // Now that the transaction is mined, send subordinate transaction ready messages in case of
      // subordinate transactions. After receiving the ready message update the list of
      // txsToBeMined.
      // In case of originating transaction update the list directly.
      Optional<ValidationResult<TransactionValidator.TransactionInvalidReason>> txReadyMsgError =
          updateListAndSendTxReadyMsg(transaction);
      if (txReadyMsgError.isPresent()) {
        return txReadyMsgError.get();
      }
    }

    if (transaction.getType().isLockableTransaction()) {
      validationResult.ifValid(
          () -> {
            this.processor.startCrosschainTransactionCommitIgnoreTimeOut(transaction);
          });
    }
    return validationResult;
  }

  /**
   * Execute a subordinate view.
   *
   * @param subordinateView The subordinate view to process.
   * @param blockNumber Execute view at this block number.
   * @return Result or an error.
   */
  public Object getSignedSubordinateViewResult(
      final CrosschainTransaction subordinateView, final long blockNumber) {
    // Get Subordinate View results.
    if (this.processor.processSubordinates(subordinateView, false)) {
      return TransactionValidator.TransactionInvalidReason.CROSSCHAIN_FAILED_SUBORDINATE_VIEW;
    }

    Object resultObj = this.processor.executeSubordinateView(subordinateView, blockNumber);
    TransactionProcessor.Result txResult;
    if (resultObj instanceof TransactionSimulatorResult) {
      TransactionSimulatorResult resultTxSim = (TransactionSimulatorResult) resultObj;
      BytesValue resultBytesValue = resultTxSim.getOutput();
      LOG.info("Transaction Simulator Result: " + resultBytesValue.toString());
      SubordinateViewResultMessage resultMessage =
          new SubordinateViewResultMessage(subordinateView, resultBytesValue, blockNumber);

      // Cooperate with other nodes to threshold sign (in-place) the message.
      this.crosschainKeyManager.thresholdSign(resultMessage);

      // TODO: Broadcast using P2P

      txResult =
          MainnetTransactionProcessor.Result.successful(
              resultTxSim.getResult().getLogs(),
              resultTxSim.getResult().getGasRemaining(),
              resultMessage.getEncodedMessage(),
              resultTxSim.getValidationResult());
      return new TransactionSimulatorResult(subordinateView, txResult);
    } else {
      // An error occurred - propagate the error.
      LOG.info("Transaction Simulator returned an error");
      return resultObj;
    }
  }

  public boolean isLocked(final Address address) {
    Hash latestBlockStateRootHash = this.blockchain.getChainHeadBlock().getHeader().getStateRoot();
    final Optional<WorldState> maybeWorldState = worldStateArchive.get(latestBlockStateRootHash);
    if (maybeWorldState.isEmpty()) {
      LOG.error("Can't fetch world state");
      // TODO should rather be an exception
      return false;
    }
    WorldState worldState = maybeWorldState.get();
    final Account contract = worldState.get(address);

    if (!contract.isLockable()) {
      throw new InvalidJsonRpcRequestException("Contract is not lockable");
    }

    return contract.isLocked();
  }

  /**
   * Called by the JSON RPC method: CrossCheckUnlock.
   *
   * <p>If a contract is lockable and locked, then check with the Crosschain Coordination Contract
   * which is coordinating the Crosschain Transaction to see if the transaction has completed and if
   * the contract can be unlocked.
   *
   * <p>The thought is that this method should never be used. However, if there was some unexpected
   * situation, probably due to a code defect, this method would provide a way to unlock a contract.
   *
   * @param address Address of contract to check.
   */
  public void checkUnlock(final Address address) {

    // TODO For the moment just unlock the contract.

    if (isLocked(address)) {
      // TODO here we need to check the Crosschain Coordination Contract.
      List<Address> addressesToUnlock = new ArrayList<>();
      addressesToUnlock.add(address);
      this.processor.sendSignallingTransaction(addressesToUnlock);
    }
  }

  /**
   * Called by the JSON RPC method: cross_startThresholdKeyGeneration.
   *
   * @param threshold The threshold number of validators that will be needed to sign messages.
   * @param algorithm The ECC curve and message digest function to be used.
   * @return The key version number.
   */
  public long startThresholdKeyGeneration(
      final int threshold, final BlsThresholdCryptoSystem algorithm) {
    long keyVersion = this.crosschainKeyManager.generateNewKeys(threshold, algorithm);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.putKeyData(
        crosschainKeyManager.getActiveKeyVersion(),
        crosschainKeyManager.activeKeyGenerations,
        crosschainKeyManager.credentials);
    updater.commit();
    return keyVersion;
  }

  /**
   * Called by the JSON RPC method: cross_getKeyStatus.
   *
   * @param keyVersion version of key to fetch information about.
   * @return Indicates the status of the key.
   */
  public KeyStatus getKeyStatus(final long keyVersion) {
    return this.crosschainKeyManager.getKeyStatus(keyVersion);
  }

  /**
   * Called by the JSON RPC Call cross_getKeyGenNodesDroppedOutOfKeyGeneration
   *
   * @param keyVersion version of key to fetch information about.
   * @return The current public key and meta-data.
   */
  public Map<BigInteger, KeyGenFailureToCompleteReason> getKeyGenNodesDroppedOutOfKeyGeneration(
      final long keyVersion) {
    return this.crosschainKeyManager.getKeyGenNodesDroppedOutOfKeyGeneration(keyVersion);
  }

  /**
   * Called by the JSON RPC Call cross_getKeyGenFailureReason. Returns the top level reason why the
   * key generation failed, if it did. If a key generation didn't fail, then the indicate is
   * success.
   *
   * @param keyVersion version of key to fetch information about.
   * @return key generation failure status.
   */
  public KeyGenFailureToCompleteReason getKeyGenFailureReason(final long keyVersion) {
    return this.crosschainKeyManager.getKeyGenFailureReason(keyVersion);
  }

  /**
   * Called by the JSON RPC call: cross_getKeyGenActiveNodes. Returns the list of nodes that hold
   * secret shares and who can participate in threshold signing. During a key generation, this will
   * be the set of nodes still active in the key generation process.
   *
   * @param keyVersion version of key to fetch information about.
   * @return nodes active in a threshold key.
   */
  public Set<BigInteger> getKeyGenActiveNodes(final long keyVersion) {
    return this.crosschainKeyManager.getKeyGenActiveNodes(keyVersion);
  }

  /**
   * Called by the JSON RPC call: cross_activateKeyVersion.
   *
   * @param keyVersion Key version to activate.
   */
  public void activateKey(final long keyVersion) {
    this.crosschainKeyManager.activateKey(keyVersion);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.putKeyData(
        crosschainKeyManager.getActiveKeyVersion(),
        crosschainKeyManager.activeKeyGenerations,
        crosschainKeyManager.credentials);
    updater.commit();
  }

  /**
   * Called by JSON RPC call: cross_getActiveKeyVersion
   *
   * @return The key that is currently active. 0 is returned if no key is active.
   */
  public long getActiveKeyVersion() {
    return this.crosschainKeyManager.getActiveKeyVersion();
  }

  /**
   * Called by the JSON RPC Call cross_getBlockchainPublicKeyByVersion
   *
   * @param keyVersion to fetch key for.
   * @return The current public key and meta-data.
   */
  public BlsThresholdPublicKey getBlockchainPublicKey(final long keyVersion) {
    return this.crosschainKeyManager.getPublicKey(keyVersion);
  }

  /**
   * Now that the transaction is mined, send subordinate transaction ready messages in case of
   * subordinate transactions. After receiving the ready message update the list of txsToBeMined. In
   * case of originating transaction update the list directly.
   *
   * @param transaction Transaction that was mined.
   * @return Any error in the process of sending subordinateTransactionReady message.
   */
  private Optional<ValidationResult<TransactionValidator.TransactionInvalidReason>>
      updateListAndSendTxReadyMsg(final CrosschainTransaction transaction) {
    if (transaction.getType().isOriginatingTransaction()) {
      this.origMsgProcessor.removeOrigTxInsideToBeMined(
          transaction.getChainId().get(), transaction.hash());
      return Optional.empty();
    } else {
      return this.processor.sendSubTxReady(transaction);
    }
  }

  /**
   * We are receiving the subordinateTransactionReady message on the originating chain. This method
   * verifies the signature and removes the entry from the txsToBeMined set. When this set becomes
   * empty, we are good to send the commit message to the coordination contract
   *
   * @param subTxReadyMsg SubordinateTransactionReady message.
   * @return Returns true if there is any error, otherwise false.
   */
  public boolean receiveSubTxReadyMsg(final SubordinateTransactionReadyMessage subTxReadyMsg) {
    return this.origMsgProcessor.removeTxInsideToBeMined(subTxReadyMsg);
  }

  public void setKeyGenerationContractAddress(final Address address) {
    this.crosschainKeyManager.setKeyGenerationContractAddress(address);
  }

  public void addCoordinationContract(
      final BigInteger blockchainId, final Address address, final String ipAddressAndPort) {
    this.coordContractManager.addCoordinationContract(blockchainId, address, ipAddressAndPort);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.putCoordCtrt(blockchainId, address, ipAddressAndPort);
    updater.commit();
  }

  public void removeCoordinationContract(final BigInteger blockchainId, final Address address) {
    this.coordContractManager.removeCoordinationContract(blockchainId, address);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.removeCoordCtrt(blockchainId, address);
    updater.commit();
  }

  public Collection<CoordinationContractInformation> listCoordinationContracts() {
    return this.coordContractManager.getAllCoordinationContracts();
  }

  public void addLinkedNode(final BigInteger blockchainId, final String ipAddressAndPort) {
    this.linkedNodeManager.addNode(blockchainId, ipAddressAndPort);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.putLinkedNode(blockchainId, ipAddressAndPort);
    updater.commit();
  }

  public void removeLinkedNode(final BigInteger blockchainId) {
    this.linkedNodeManager.removeNode(blockchainId);
    CrosschainNodeStorage.Updater updater = nodeStorage.updater();
    updater.removeLinkedNode(blockchainId);
    updater.commit();
  }

  public Set<BlockchainNodeInformation> listLinkedNodes() {
    return this.linkedNodeManager.listAllNodes();
  }
}
