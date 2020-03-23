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
import org.hyperledger.besu.crosschain.core.messages.SubordinateTransactionReadyMessage;
import org.hyperledger.besu.crosschain.core.messages.SubordinateViewResultMessage;
import org.hyperledger.besu.crosschain.core.messages.ThresholdSignedMessage;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsCryptoProvider;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crosschain.ethereum.crosschain.CrosschainThreadLocalDataHolder;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.Account;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.MutableWorldState;
import org.hyperledger.besu.ethereum.core.Wei;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.mainnet.TransactionValidator;
import org.hyperledger.besu.ethereum.mainnet.ValidationResult;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.transaction.TransactionSimulator;
import org.hyperledger.besu.ethereum.transaction.TransactionSimulatorResult;
import org.hyperledger.besu.ethereum.worldstate.WorldStateArchive;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrosschainProcessor {
  protected static final Logger LOG = LogManager.getLogger();

  TransactionSimulator transactionSimulator;
  TransactionPool transactionPool;
  SECP256K1.KeyPair nodeKeys;
  Blockchain blockchain;
  WorldStateArchive worldStateArchive;
  BigInteger sidechainId;

  Vertx vertx;

  private LinkedNodeManager linkedNodeManager;
  private CoordContractManager coordContractManager;
  private CrosschainKeyManager crosschainKeyManager;

  boolean commitOrIgnoreFlag;

  public CrosschainProcessor(
      final LinkedNodeManager linkedNodeManager, final CoordContractManager coordContractManager) {
    this.linkedNodeManager = linkedNodeManager;
    this.coordContractManager = coordContractManager;
  }

  public void init(
      final TransactionSimulator transactionSimulator,
      final TransactionPool transactionPool,
      final BigInteger sidechainId,
      final SECP256K1.KeyPair nodeKeys,
      final Blockchain blockchain,
      final WorldStateArchive worldStateArchive,
      final CrosschainKeyManager crosschainKeyManager) {
    this.transactionSimulator = transactionSimulator;
    this.transactionPool = transactionPool;
    this.sidechainId = sidechainId;
    this.nodeKeys = nodeKeys;
    this.blockchain = blockchain;
    this.worldStateArchive = worldStateArchive;
    this.crosschainKeyManager = crosschainKeyManager;

    this.vertx = Vertx.vertx();
    // this seems to cause a couple of internal exceptions visible in DEBUG
    // logging. java.lang.UnsupportedOperationException: Reflective
    // setAccessible(true) disabled
  }
  /**
   * Process subordinate transactions or subordinate views.
   *
   * @param transaction The Originating Transaction, Subordinate Transaction or Subordinate View to
   *     fetch the subordinate Subordinate Transactions or Views from.
   * @param processSubordinateTransactions true if transactions rather than views should be
   *     processed.
   * @return true if execution failed.
   */
  boolean processSubordinates(
      final CrosschainTransaction transaction, final boolean processSubordinateTransactions) {
    List<CrosschainTransaction> subordinates = transaction.getSubordinateTransactionsAndViews();
    for (CrosschainTransaction subordinateTransactionsAndView : subordinates) {
      if ((processSubordinateTransactions
              && subordinateTransactionsAndView.getType().isSubordinateTransaction())
          || (!processSubordinateTransactions
              && subordinateTransactionsAndView.getType().isSubordinateView())) {

        String method =
            subordinateTransactionsAndView.getType().isSubordinateView()
                ? RpcMethod.Constants.CROSS_PROCESS_SUBORDINATE_VIEW
                : RpcMethod.Constants.CROSS_SEND_RAW_CROSSCHAIN_TRANSACTION_STR;

        BytesValueRLPOutput out = new BytesValueRLPOutput();
        subordinateTransactionsAndView.writeTo(out);
        BytesValue signedTransaction = out.encoded();

        if (signedTransaction == null) {
          LOG.error("Subordinate view does not exist");
          // Indicate execution failed unexpectedly.
          return true;
        }

        Optional<BigInteger> optionalSidechainId = subordinateTransactionsAndView.getChainId();
        BigInteger sidechainId = optionalSidechainId.orElse(BigInteger.ZERO);

        // Get the address from chain mapping.
        String ipAddress = this.linkedNodeManager.getIpAddressAndPort(sidechainId);
        String response = null;
        LOG.info("Sending Crosschain Transaction or view to chain at " + ipAddress);
        try {
          response =
              OutwardBoundConnectionManager.post(ipAddress, method, signedTransaction.toString());
          LOG.debug("Crosschain Response: " + response);
        } catch (Exception e) {
          LOG.error("Exception during crosschain happens here: " + e.getMessage());
          // Indicate execution failed unexpectedly.
          return true;
        }

        BytesValue result = processResult(response);

        if ((!processSubordinateTransactions
            && subordinateTransactionsAndView.getType().isSubordinateView())) {

          // Decode the response
          SubordinateViewResultMessage viewResultMessage =
              (SubordinateViewResultMessage) ThresholdSignedMessage.decodeEncodedMessage(result);

          // Obtain the blockchain public key from the coordination contract using the sidechainId
          Optional<BigInteger> coordChainId = transaction.getCrosschainCoordinationBlockchainId();
          Optional<Address> coordAddr = transaction.getCrosschainCoordinationContractAddress();
          if (coordChainId.isEmpty() || coordAddr.isEmpty()) {
            LOG.error("Coordination Chain is not set up");
            return true;
          }
          String coordIpAddrAndPort =
              coordContractManager.getIpAndPort(coordChainId.get(), coordAddr.get());
          BigInteger publicKey =
              new OutwardBoundConnectionManager(this.nodeKeys)
                  .getPublicKeyFromCoordContract(
                      coordIpAddrAndPort,
                      coordChainId.get(),
                      coordAddr.get(),
                      sidechainId,
                      viewResultMessage.getKeyVersion());
          LOG.info(
              "Obtained the public key {} from crosschain coordination contract.",
              publicKey.toString(16));

          // Verify the signature
          BlsPoint publicKeyBlsPoint = BlsPoint.load(publicKey.toByteArray());

          BlsCryptoProvider cryptoProvider =
              BlsCryptoProvider.getInstance(
                  BlsCryptoProvider.CryptoProviderTypes.LOCAL_ALT_BN_128,
                  BlsCryptoProvider.DigestAlgorithm.KECCAK256);

          boolean signatureVerification =
              cryptoProvider.verify(
                  publicKeyBlsPoint,
                  viewResultMessage.getEncodedCoreMessage().extractArray(),
                  BlsPoint.load(viewResultMessage.getSignature().getByteArray()));

          if (signatureVerification) {
            LOG.info("The signature of Subordinate View Result message verified.");
          } else {
            LOG.error("Verification of the subordinate view result message's signature failed.");
            return true;
          }

          // Check that the Subordiante View hash returned matches the submitted subordiante view.
          if (subordinateTransactionsAndView.hash().equals(viewResultMessage.getTxHash())) {
            LOG.info("The hash of the subordinate view result transaction matches.");
          } else {
            LOG.error("The hash of the subordinate view result transaction does not match");
            return true;
          }

          LOG.info("Crosschain Result: " + viewResultMessage.getResult().toString());
          subordinateTransactionsAndView.addSignedResult(viewResultMessage.getResult());
        } else {
          LOG.info("Crosschain Result: " + result.toString());
          subordinateTransactionsAndView.addSignedResult(result);
        }
      }
    }

    return false;
  }

  /**
   * Do a trial execution of the Crosschain Transaction.
   *
   * @param subordinateTransaction transaction to execute.
   * @return Empty if the transaction and subordinate views execute correctly, otherwise an error is
   *     returned.
   */
  Optional<ValidationResult<TransactionValidator.TransactionInvalidReason>> trialExecution(
      final CrosschainTransaction subordinateTransaction) {
    // Add to thread local storage.
    CrosschainThreadLocalDataHolder.setCrosschainTransaction(subordinateTransaction);
    // Rewind to the first subordinate transaction or view for each execution.
    subordinateTransaction.resetSubordinateTransactionsAndViewsList();

    Optional<TransactionSimulatorResult> result =
        this.transactionSimulator.processAtHead(subordinateTransaction);
    CrosschainThreadLocalDataHolder.removeCrosschainTransaction();

    if (result.isPresent()) {
      TransactionSimulatorResult simulatorResult = result.get();
      LOG.info("Transaction Simulation Status {}", simulatorResult.getResult().getStatus());

      if (simulatorResult.isSuccessful()) {
        return Optional.empty();
      }
      // The transaction may have failed, but the transaction is valid. This could occur when a
      // revert is thrown
      // while executing the code.
      if (simulatorResult.getValidationResult().isValid()) {
        // TODO If we return a TransactionInvalidReason, then the HTTP response will be 400.
        // Hence, return as if everything has been successful, and rely on the user to see that no
        // status update occurred as a result of their transaction.
        return Optional.of(
            ValidationResult.invalid(
                TransactionValidator.TransactionInvalidReason.CROSSCHAIN_FAILED_EXECUTION));
      }
      return Optional.of(simulatorResult.getValidationResult());
    }
    return Optional.of(
        ValidationResult.invalid(
            TransactionValidator.TransactionInvalidReason.CROSSCHAIN_UNKNOWN_FAILURE));
  }

  /**
   * TODO THIS METHOD SHOULD PROBABLY MERGE WITH THE ONE ABOVE Process the Subordinate View for this
   * block number.
   *
   * @param subordinateView Subordinate view to execute.
   * @param blockNumber block number to execute the view call at.
   * @return TransactionSimulatorResult if the execution completed. TransactionInvalidReason if
   *     there was an error.
   */
  public Object executeSubordinateView(
      final CrosschainTransaction subordinateView, final long blockNumber) {
    return this.transactionSimulator
        .process(subordinateView, blockNumber)
        .map(result -> result.getValidationResult().either((() -> result), reason -> reason))
        .orElse(null);
  }

  private BytesValue processResult(final String response) {
    final JsonObject responseJson = new JsonObject(response);
    String result = responseJson.getString("result");
    return BytesValue.fromHexString(result);
  }

  /**
   * This method queries the state of the crosschain transaction from the coordination contract.
   *
   * @param transaction Crosschain transaction whose state is queried upon.
   * @return status of the crosschain transaction
   */
  private long getCrosschainTransactionStatus(final CrosschainTransaction transaction) {
    Optional<BigInteger> coordChainId = transaction.getCrosschainCoordinationBlockchainId();
    Optional<Address> coordAddr = transaction.getCrosschainCoordinationContractAddress();
    if (coordChainId.isEmpty() || coordAddr.isEmpty()) {
      LOG.error("Coordination Chain is not set up");
      return 0;
    }

    final String coordIpAddrAndPort =
        this.coordContractManager.getIpAndPort(coordChainId.get(), coordAddr.get());
    final BigInteger origChainId =
        (transaction.getType().isOriginatingTransaction()
            ? transaction.getChainId().get()
            : transaction.getOriginatingSidechainId().get());

    return new OutwardBoundConnectionManager(this.nodeKeys)
        .getCrosschainTransactionStatus(
            coordIpAddrAndPort,
            coordChainId.get(),
            coordAddr.get(),
            origChainId,
            transaction.getCrosschainTransactionId().get());
  }

  void startCrosschainTransactionCommitIgnoreTimeOut(final CrosschainTransaction transaction) {
    this.commitOrIgnoreFlag = false;

    while (!commitOrIgnoreFlag) {
      this.vertx.setTimer(
          2000,
          id -> {
            // check coordination contract if committed / ignored
            long txStatus = getCrosschainTransactionStatus(transaction);
            LOG.info(
                "Crosschain Transaction Status as fetched from the coordination contract is {}",
                txStatus);
            if (txStatus == 2 || txStatus == 3) {
              List<Address> addressesToUnlock = transaction.getLockedAddresses();
              commitOrIgnoreFlag = true;
              if (addressesToUnlock == null || addressesToUnlock.size() == 0) {
                LOG.info("No addresses to unlock. Not sending signalling transaction");
              } else {
                sendSignallingTransaction(addressesToUnlock, txStatus);
              }
            } else {
              try {
                Thread.sleep(60000);
              } catch (Exception e) {
                LOG.info("Thead.sleep throws an exception {}", e.toString());
              }
            }
          });
    }
  }

  /**
   * This method threshold signs and sends the subordinateTransactionReady message to the
   * originating chain.
   *
   * @param subTx The given subordinate transaction.
   * @return Returns error (if any) in the ValidationResult with the reason.
   */
  public Optional<ValidationResult<TransactionValidator.TransactionInvalidReason>> sendSubTxReady(
      final CrosschainTransaction subTx) {
    // Because this is a subordinate transaction, we are guaranteed to have the Optional
    // field of originating chain Id and subordinate chain Id to be set.
    BigInteger origChainId = subTx.getOriginatingSidechainId().get();
    String origIpAddressAndPort = this.linkedNodeManager.getIpAddressAndPort(origChainId);
    SubordinateTransactionReadyMessage txReadyMsg = new SubordinateTransactionReadyMessage(subTx);
    this.crosschainKeyManager.thresholdSign(txReadyMsg);

    // Submit the message to the linked node that is on originating chain
    try {
      LOG.info("Sending signed Transaction Ready message from chain {}", subTx.getChainId());
      OutwardBoundConnectionManager.post(
          origIpAddressAndPort,
          RpcMethod.CROSS_SEND_TRANSACTION_READY_MESSAGE.getMethodName(),
          txReadyMsg.getEncodedMessage().getHexString());
    } catch (Exception e) {
      LOG.error(
          "Exception during sending transaction ready message happens here: " + e.getMessage());
      return Optional.of(
          ValidationResult.invalid(
              TransactionValidator.TransactionInvalidReason
                  .CROSSCHAIN_FAILED_SUBORDINATE_TRANSACTION));
    }
    return Optional.empty();
  }

  /**
   * Send a signalling transaction to an address to unlock a contract.
   *
   * <p>TODO we should probably check the response and retry if appropriate.
   *
   * @param addressesToUnlock Addresses of contracts to unlock / send the signalling transaction to.
   * @param txStatus Status of the crosschain transaction
   */
  void sendSignallingTransaction(final List<Address> addressesToUnlock, final long txStatus) {
    LOG.debug("Crosschain Signalling Transaction: Initiated");

    // Work out sender's nonce.
    // TODO The code below only determines the nonce up until the latest block. It does not
    // TODO look at pending transactions.
    Hash latestBlockStateRootHash = this.blockchain.getChainHeadBlock().getHeader().getStateRoot();
    final Optional<MutableWorldState> maybeWorldState =
        worldStateArchive.getMutable(latestBlockStateRootHash);
    if (maybeWorldState.isEmpty()) {
      LOG.error("Crosschain Signalling Transaction: Can't fetch world state");
      return;
    }
    MutableWorldState worldState = maybeWorldState.get();
    final Address senderAddress =
        Address.extract(Hash.hash(this.nodeKeys.getPublicKey().getEncodedBytes()));
    final Account sender = worldState.get(senderAddress);
    final long nonce = sender != null ? sender.getNonce() : 0L;

    List<CrosschainTransaction> emptyList = List.of();

    BytesValue payload = BytesValue.EMPTY;
    for (Address addr : addressesToUnlock) {
      LOG.info("Sending Signalling Transaction for address {}", addr);
      payload = payload.concat(addr);
    }

    final CrosschainTransaction.CrosschainTransactionType txType;
    if (txStatus == 2) {
      txType = CrosschainTransaction.CrosschainTransactionType.UNLOCK_COMMIT_SIGNALLING_TRANSACTION;
    } else if (txStatus == 3) {
      txType = CrosschainTransaction.CrosschainTransactionType.UNLOCK_IGNORE_SIGNALLING_TRANSACTION;
    } else {
      LOG.error("Signalling transaction called when the transaction is not COMMITTED or IGNORED");
      return;
    }

    CrosschainTransaction ignoreCommitTransaction =
        CrosschainTransaction.builderX()
            .type(txType)
            .nonce(nonce)
            .gasPrice(Wei.ZERO)
            .gasLimit(100000)
            .to(Address.ZERO)
            .value(Wei.ZERO)
            .payload(payload)
            .chainId(this.sidechainId)
            .subordinateTransactionsAndViews(emptyList)
            .signAndBuild(this.nodeKeys);

    ValidationResult<TransactionValidator.TransactionInvalidReason> validationResult =
        this.transactionPool.addLocalTransaction(ignoreCommitTransaction);
    if (!validationResult.isValid()) {
      LOG.warn(
          "Crosschain Signalling Transaction: Validation result:{}", validationResult.toString());
    }
  }
}
