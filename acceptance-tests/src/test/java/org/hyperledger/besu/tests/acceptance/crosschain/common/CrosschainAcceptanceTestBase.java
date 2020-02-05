/*
 * Copyright 2018 ConsenSys AG.
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
package org.hyperledger.besu.tests.acceptance.crosschain.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.crosschain.core.coordination.generated.CrosschainCoordinationV1;
import org.hyperledger.besu.crosschain.core.keys.BlsThresholdPublicKey;
import org.hyperledger.besu.crosschain.core.keys.BlsThresholdPublicKeyImpl;
import org.hyperledger.besu.tests.acceptance.crosschain.generated.VotingAlgMajorityWhoVoted;
import org.hyperledger.besu.tests.acceptance.dsl.AcceptanceTestBase;
import org.hyperledger.besu.tests.acceptance.dsl.account.Accounts;
import org.hyperledger.besu.tests.acceptance.dsl.node.BesuNode;
import org.hyperledger.besu.tests.acceptance.dsl.node.cluster.Cluster;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import org.hyperledger.besu.util.bytes.BytesValue;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.besu.JsonRpc2_0Besu;
import org.web3j.protocol.besu.crypto.crosschain.BlsThresholdCryptoSystem;
import org.web3j.protocol.besu.response.crosschain.CrossBlockchainPublicKeyResponse;
import org.web3j.protocol.besu.response.crosschain.CrossIsLockedResponse;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.CrosschainTransactionManager;

import static org.hyperledger.besu.crosschain.core.coordination.CoordinationContractWrapper.VOTE_CHANGE_PUBLIC_KEY;

public abstract class CrosschainAcceptanceTestBase extends AcceptanceTestBase {
  private static final Logger LOG = LogManager.getLogger();

  public static final int VOTING_TIME_OUT = 2;
  public static final int VOTING_TIME_PERIOD = 1;
  public static final long CROSSCHAIN_TRANSACTION_TIMEOUT = 10;
  public static final long BLOCK_PERIOD = 2000;
  public static final long VOTING_WAIT_TIME = VOTING_TIME_PERIOD * BLOCK_PERIOD;
  protected Credentials BENEFACTOR_ONE;

  protected Cluster clusterCoordinationBlockchain;
  protected BesuNode nodeOnCoordinationBlockchain;
  protected CrosschainCoordinationV1 coordContract;
  protected VotingAlgMajorityWhoVoted votingContract;

  protected Cluster clusterBc1;
  protected BesuNode nodeOnBlockchain1;
  protected CrosschainTransactionManager transactionManagerBlockchain1;
  protected long BLOCKCHAIN1_SLEEP_DURATION = 2000;
  protected int BLOCKCHAIN1_RETRY_ATTEMPTS = 3;

  protected Cluster clusterBc2;
  protected BesuNode nodeOnBlockchain2;
  protected CrosschainTransactionManager transactionManagerBlockchain2;

  protected Cluster clusterBc3;
  protected BesuNode nodeOnBlockchain3;
  protected CrosschainTransactionManager transactionManagerBlockchain3;

  public void setUpCoordinationChain() throws Exception {
    nodeOnCoordinationBlockchain =
        besu.createCrosschainCoordinationBlockchainIbft2Node("coord-node");
    this.clusterCoordinationBlockchain = new Cluster(this.net);
    this.clusterCoordinationBlockchain.start(nodeOnCoordinationBlockchain);

    this.votingContract =
        nodeOnCoordinationBlockchain.execute(
            contractTransactions.createSmartContract(VotingAlgMajorityWhoVoted.class));
    this.coordContract =
        nodeOnCoordinationBlockchain.execute(
            contractTransactions.createSmartContract(
                CrosschainCoordinationV1.class,
                votingContract.getContractAddress(),
                BigInteger.valueOf(VOTING_TIME_OUT)));

    // Adding the coordination contract
    String ipAddress = this.nodeOnCoordinationBlockchain.jsonRpcListenHost1();
    int port = this.nodeOnCoordinationBlockchain.getJsonRpcSocketPort1().intValue();
    String ipAddressAndPort = ipAddress + ":" + port;
    this.nodeOnCoordinationBlockchain.execute(
        crossTransactions.addCoordinationContract(
            this.nodeOnCoordinationBlockchain.getChainId(),
            this.coordContract.getContractAddress(),
            ipAddressAndPort));
  }

  private void commonSetup(final BesuNode nodeOnBlockchain) throws Exception {
    // Adding the coordination contract
    String ipAddress = this.nodeOnCoordinationBlockchain.jsonRpcListenHost1();
    int port = this.nodeOnCoordinationBlockchain.getJsonRpcSocketPort1().intValue();
    String ipAddressAndPort = ipAddress + ":" + port;
    nodeOnBlockchain.execute(
      crossTransactions.addCoordinationContract(
        this.nodeOnCoordinationBlockchain.getChainId(),
        this.coordContract.getContractAddress(),
        ipAddressAndPort));

    BigInteger keyVersionGenerated =
      nodeOnBlockchain.execute(
        crossTransactions.startThresholdKeyGeneration(
          1, BlsThresholdCryptoSystem.ALT_BN_128_WITH_KECCAK256));
    System.out.println("Key version generated: " + keyVersionGenerated);

    LOG.info("Adding the blockchain to the coordination contract");
    TransactionReceipt receipt = coordContract.addBlockchain(
      nodeOnBlockchain.getChainId(),
      this.votingContract.getContractAddress(),
      BigInteger.valueOf(VOTING_TIME_PERIOD)).send();
    LOG.info(" TX Receipt: {}", receipt);

    boolean exists = coordContract.getBlockchainExists(nodeOnBlockchain.getChainId()).send();
    LOG.info(" Blockchain {} has been added to coordination contract: {}",
      nodeOnBlockchain.getChainId(), exists);

    CrossBlockchainPublicKeyResponse publicKeyResponse = nodeOnBlockchain.execute(
      this.crossTransactions.getBlockchainPublicKey(keyVersionGenerated.longValue()));
    BlsThresholdPublicKey publicKey =
      BlsThresholdPublicKeyImpl.readFrom(BytesValue.fromHexString(publicKeyResponse.getEncodedKey()));

    LOG.info("Propose vote to add the public key {} {}", publicKey.getPublicKey().store(),
      keyVersionGenerated);
    receipt = coordContract.proposeVote(
      nodeOnBlockchain.getChainId(),
      VOTE_CHANGE_PUBLIC_KEY,
      BigInteger.ZERO,
      keyVersionGenerated,
      publicKey.getPublicKey().store()).send();
    LOG.info(" TX Receipt: {}", receipt);

    // Sleep for voting period
    LOG.info("Waiting for end of voting period");
    Thread.sleep(VOTING_WAIT_TIME);

    LOG.info(" Action vote to add key");
    receipt = coordContract.actionVotes(nodeOnBlockchain.getChainId(), BigInteger.ZERO).send();
    LOG.info(" TX Receipt: {}", receipt);

    LOG.info("Waiting for block to be mined");
    Thread.sleep(BLOCK_PERIOD);

    boolean keyExists = coordContract.publicKeyExists(nodeOnBlockchain.getChainId(), keyVersionGenerated).send();
    if (keyExists) {
      LOG.info("Key successfully added to coordination contract");
    }
    else {
      LOG.error("FAILED to add key to contract");
      return;
    }

    // Finally, activate the key.
    nodeOnBlockchain.execute(crossTransactions.activateKey(keyVersionGenerated.longValue()));

    Tuple3<BigInteger, BigInteger, List<BigInteger>> result =
      this.coordContract.getPublicKey(nodeOnBlockchain.getChainId(), keyVersionGenerated).send();
    LOG.info("***** {} {} {}", result.component1(), result.component2(), result.component3().size());
    ByteBuffer buf = ByteBuffer.allocate(32);
    for(BigInteger a : result.component3()) {
      buf.putLong(a.longValue());
    }
    LOG.info("*** RETRIEVED PUBLIC KEY = {}", new BigInteger(buf.array()));
  }

  public void setUpBlockchain1() throws Exception {
    this.nodeOnBlockchain1 = besu.createCrosschainBlockchain1Ibft2Node("bc1-node");
    this.clusterBc1 = new Cluster(this.net);
    this.clusterBc1.start(nodeOnBlockchain1);

    JsonRpc2_0Besu blockchain1Web3j = this.nodeOnBlockchain1.getJsonRpc();
    BENEFACTOR_ONE = Credentials.create(Accounts.GENESIS_ACCOUNT_ONE_PRIVATE_KEY);
    JsonRpc2_0Besu coordinationWeb3j = this.nodeOnCoordinationBlockchain.getJsonRpc();

    this.transactionManagerBlockchain1 =
        new CrosschainTransactionManager(
            blockchain1Web3j,
            BENEFACTOR_ONE,
            this.nodeOnBlockchain1.getChainId(),
            BLOCKCHAIN1_RETRY_ATTEMPTS,
            BLOCKCHAIN1_SLEEP_DURATION,
            coordinationWeb3j,
            this.nodeOnCoordinationBlockchain.getChainId(),
            this.coordContract.getContractAddress(),
            CROSSCHAIN_TRANSACTION_TIMEOUT);

    commonSetup(this.nodeOnBlockchain1);
  }

  public void setUpBlockchain2() throws Exception {
    setUpBlockchain2_NoKeyGenerate();
    commonSetup(this.nodeOnBlockchain2);
  }

  public void setUpBlockchain2_NoKeyGenerate() throws Exception {
    this.nodeOnBlockchain2 = besu.createCrosschainBlockchain2Ibft2Node("bc2-node");
    this.clusterBc2 = new Cluster(this.net);
    this.clusterBc2.start(nodeOnBlockchain2);

    JsonRpc2_0Besu blockchain2Web3j = this.nodeOnBlockchain2.getJsonRpc();
    final Credentials BENEFACTOR_ONE = Credentials.create(Accounts.GENESIS_ACCOUNT_ONE_PRIVATE_KEY);
    JsonRpc2_0Besu coordinationWeb3j = this.nodeOnCoordinationBlockchain.getJsonRpc();

    this.transactionManagerBlockchain2 =
        new CrosschainTransactionManager(
            blockchain2Web3j,
            BENEFACTOR_ONE,
            this.nodeOnBlockchain2.getChainId(),
            BLOCKCHAIN1_RETRY_ATTEMPTS,
            BLOCKCHAIN1_SLEEP_DURATION,
            coordinationWeb3j,
            this.nodeOnCoordinationBlockchain.getChainId(),
            this.coordContract.getContractAddress(),
            CROSSCHAIN_TRANSACTION_TIMEOUT);
  }

  public void setUpBlockchain3() throws Exception {
    this.nodeOnBlockchain3 = besu.createCrosschainBlockchain3Ibft2Node("bc3-node");
    this.clusterBc3 = new Cluster(this.net);
    this.clusterBc3.start(this.nodeOnBlockchain3);

    JsonRpc2_0Besu blockchain3Web3j = this.nodeOnBlockchain3.getJsonRpc();
    final Credentials BENEFACTOR_ONE = Credentials.create(Accounts.GENESIS_ACCOUNT_ONE_PRIVATE_KEY);
    JsonRpc2_0Besu coordinationWeb3j = this.nodeOnCoordinationBlockchain.getJsonRpc();

    this.transactionManagerBlockchain3 =
        new CrosschainTransactionManager(
            blockchain3Web3j,
            BENEFACTOR_ONE,
            this.nodeOnBlockchain3.getChainId(),
            BLOCKCHAIN1_RETRY_ATTEMPTS,
            BLOCKCHAIN1_SLEEP_DURATION,
            coordinationWeb3j,
            this.nodeOnCoordinationBlockchain.getChainId(),
            this.coordContract.getContractAddress(),
            CROSSCHAIN_TRANSACTION_TIMEOUT);

    commonSetup(this.nodeOnBlockchain3);
  }

  public void addMultichainNode(final BesuNode node, final BesuNode nodeToAdd) {
    String ipAddress = nodeToAdd.jsonRpcListenHost1();
    int port = nodeToAdd.getJsonRpcSocketPort1().intValue();
    String ipAddressAndPort = ipAddress + ":" + port;
    BigInteger chainId = nodeToAdd.getChainId();

    node.execute(crossTransactions.getAddLinkedNode(chainId, ipAddressAndPort));
  }

  protected void waitForUnlock(final String ctrtAddress, final BesuNode node) throws Exception {
    CrossIsLockedResponse isLockedObj =
        node.getJsonRpc()
            .crossIsLocked(ctrtAddress, DefaultBlockParameter.valueOf("latest"))
            .send();
    while (isLockedObj.isLocked()) {
      Thread.sleep(100);
      isLockedObj =
          node.getJsonRpc()
              .crossIsLocked(ctrtAddress, DefaultBlockParameter.valueOf("latest"))
              .send();
    }
  }
}
