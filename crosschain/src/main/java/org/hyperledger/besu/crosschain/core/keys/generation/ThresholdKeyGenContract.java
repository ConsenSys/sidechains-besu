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
package org.hyperledger.besu.crosschain.core.keys.generation;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.transaction.TransactionSimulator;
import org.hyperledger.besu.ethereum.worldstate.WorldStateArchive;
import org.hyperledger.besu.util.bytes.Bytes32;

import java.math.BigInteger;

/**
 * Class wraps the Threshold Key Generation Contract.
 */
public class ThresholdKeyGenContract {
  public static final int DEFAULT_ROUND_DURATION = 5;

  private SimulatedThresholdKeyGenContract keyGen = new SimulatedThresholdKeyGenContract();


  protected static final Logger LOG = LogManager.getLogger();

  // TODO: All of this will be needed when exexuting transactions and views on the key gen contract.
  TransactionSimulator transactionSimulator;
  TransactionPool transactionPool;
  SECP256K1.KeyPair nodeKeys;
  Blockchain blockchain;
  WorldStateArchive worldStateArchive;
  int sidechainId;

  BigInteger msgSender;


  Vertx vertx;

  // TODO will need to take a parameter: the address of the deployed contract.
  public ThresholdKeyGenContract() {
  }


  public void init(
      final TransactionSimulator transactionSimulator,
      final TransactionPool transactionPool,
      final int sidechainId,
      final SECP256K1.KeyPair nodeKeys,
      final Blockchain blockchain,
      final WorldStateArchive worldStateArchive) {
    this.transactionSimulator = transactionSimulator;
    this.transactionPool = transactionPool;
    this.sidechainId = sidechainId;
    this.nodeKeys = nodeKeys;
    this.blockchain = blockchain;
    this.worldStateArchive = worldStateArchive;

    // Just have something for the simulator based on the node public key.
    this.msgSender = new BigInteger(1, this.nodeKeys.getPublicKey().getEncoded());



    this.vertx = Vertx.vertx();
  }


  public void startNewKeyGeneration(final long version, final int threshold) throws Exception {
    startNewKeyGeneration(version, threshold, DEFAULT_ROUND_DURATION);
  }


  public void startNewKeyGeneration(final long version, final int threshold, final int roundDurationInBlocks) throws Exception {
    // TODO when this is implemented for real, submit http request via vertx.
    // TODO indicate a time-out for the overall key generation process
    this.keyGen.startNewKeyGeneration(version, this.msgSender, threshold, roundDurationInBlocks);
  }


  public void setNodeId(final long version) throws Exception {
    this.keyGen.setNodeId(version, this.msgSender);
  }
  public void setNodeId(final long version, final BigInteger msgSender) throws Exception {
    this.keyGen.setNodeId(version, msgSender);
  }

  public void setNodeCoefficientsCommitments(
      final long version, final Bytes32[] coefPublicPointCommitments) throws Exception {
    this.keyGen.setNodeCoefficientsCommitments(version, this.msgSender, coefPublicPointCommitments);
  }
  public void setNodeCoefficientsCommitments(
      final long version, final BigInteger msgSender, final Bytes32[] coefPublicPointCommitments) throws Exception {
    this.keyGen.setNodeCoefficientsCommitments(version, msgSender, coefPublicPointCommitments);
  }

  public void setNodeCoefficientsPublicValues(final long version,
                                              final BlsPoint[] coefPublicPoints) throws Exception {
    this.keyGen.setNodeCoefficientsPublicValues(version,  this.msgSender, coefPublicPoints);
  }
  public void setNodeCoefficientsPublicValues(final long version, final BigInteger msgSender,
                                              final BlsPoint[] coefPublicPoints) throws Exception {
    this.keyGen.setNodeCoefficientsPublicValues(version,  msgSender, coefPublicPoints);
  }

  public long getExpectedKeyGenerationVersion() {
    return this.keyGen.getExpectedKeyGenerationVersion();
  }

  public int getNumberOfNodes(final long version) throws Exception {
    return this.keyGen.getNumberOfNodes(version);
  }


  public BigInteger getNodeAddress(final long version, final int index) throws Exception {
    return this.keyGen.getNodeAddress(version, index);
  }


  public BlsPoint getCoefficientPublicValue(final long version, final BigInteger fromAddress, final int coefNumber) throws Exception {
    return this.keyGen.getCoefficientPublicValue(version, fromAddress, coefNumber);
  }

}
