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
package org.hyperledger.besu.tests.acceptance.crosschain.getinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.tests.acceptance.crosschain.common.CrosschainAcceptanceTestBase;
import org.hyperledger.besu.tests.acceptance.crosschain.getinfo.generated.Ctrt1;
import org.hyperledger.besu.tests.acceptance.crosschain.getinfo.generated.Ctrt2;
import org.hyperledger.besu.tests.acceptance.crosschain.getinfo.generated.Ctrt3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.CrosschainContext;
import org.web3j.tx.CrosschainContextGenerator;

/*
 * Three contracts - Ctrt1, Ctrt2 and Ctrt3 are deployed on three separate blockchains.
 * Ctrt1.callCtrt2 calls a transaction function Ctrt2.callCtrt3, which calls a view
 * Ctrt3.viewfn. Various getInfo methods are checked in the tests.
 */

public class GetInfoTest extends CrosschainAcceptanceTestBase {
  private static final Logger LOG = LogManager.getLogger();

  private Ctrt1 ctrt1;
  private Ctrt2 ctrt2;
  private Ctrt3 ctrt3;

  @Before
  public void setUp() throws Exception {

    setUpCoordiantionChain();
    setUpBlockchain1();
    setUpBlockchain2();
    setUpBlockchain3();

    // Deploying the contracts
    ctrt1 =
        nodeOnBlockchain1.execute(
            contractTransactions.createLockableSmartContract(
                Ctrt1.class, this.transactionManagerBlockchain1));
    ctrt2 =
        nodeOnBlockchain2.execute(
            contractTransactions.createLockableSmartContract(
                Ctrt2.class, this.transactionManagerBlockchain2));
    ctrt3 =
        nodeOnBlockchain3.execute(
            contractTransactions.createLockableSmartContract(
                Ctrt3.class, this.transactionManagerBlockchain3));

    // Making nodeOnBlockChain1 a 3-chain node and nodeOnBlockChain2 a 2-chain node
    addMultichainNode(nodeOnBlockchain1, nodeOnBlockchain2);
    addMultichainNode(nodeOnBlockchain1, nodeOnBlockchain3);
    addMultichainNode(nodeOnBlockchain2, nodeOnBlockchain3);

    // Setting up the environment
    ctrt1.setCtrt2ChainId(nodeOnBlockchain2.getChainId()).send();
    ctrt1.setCtrt2(ctrt2.getContractAddress()).send();
    ctrt2.setCtrt3ChainId(nodeOnBlockchain3.getChainId()).send();
    ctrt2.setCtrt3(ctrt3.getContractAddress()).send();
  }

  @Test
  public void getInfoTest() throws Exception {
    // Constructing the crosschain transaction
    CrosschainContextGenerator ctxGen =
        new CrosschainContextGenerator(nodeOnBlockchain1.getChainId());
    CrosschainContext ctx =
        ctxGen.createCrosschainContext(nodeOnBlockchain2.getChainId(), ctrt2.getContractAddress());
    byte[] subT1 = ctrt3.txfn_AsSignedCrosschainSubordinateTransaction(ctx);
    byte[][] subTxV1 = new byte[][] {subT1};

    ctx =
        ctxGen.createCrosschainContext(
            nodeOnBlockchain1.getChainId(), ctrt1.getContractAddress(), subTxV1);
    byte[] subTx2 = ctrt2.callCtrt3_AsSignedCrosschainSubordinateTransaction(ctx);
    byte[][] subTxV2 = new byte[][] {subTx2};

    ctx = ctxGen.createCrosschainContext(subTxV2);

    // Executing the crosschain transaction
    TransactionReceipt txReceipt = ctrt1.callCtrt2_AsCrosschainTransaction(ctx).send();
    if (!txReceipt.isStatusOK()) {
      LOG.info("txReceipt details " + txReceipt.toString());
    }

    // Checking the GetInfo methods return values
    waitForUnlock(ctrt1.getContractAddress(), nodeOnBlockchain1);
    assertThat(ctrt1.myChainId().send().longValue()).isEqualTo(51);
  }

  @After
  public void closeDown() throws Exception {
    this.cluster.close();
    this.clusterBc1.close();
    this.clusterBc2.close();
    this.clusterBc3.close();
  }
}
