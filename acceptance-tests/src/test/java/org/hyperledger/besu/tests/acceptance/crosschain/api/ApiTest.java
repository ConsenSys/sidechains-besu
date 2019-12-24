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
package org.hyperledger.besu.tests.acceptance.crosschain.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.tests.acceptance.crosschain.common.CrosschainAcceptanceTestBase;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.besu.response.crosschain.BlockchainNodeInformation;

/*
 * Two blockchains with one node are created. And all the crosschain API methods are then tested.
 */

public class ApiTest extends CrosschainAcceptanceTestBase {
  // private static final Logger LOG = LogManager.getLogger();

  @Before
  public void setUp() throws Exception {

    setUpCoordiantionChain();
    setUpBlockchain1();
    setUpBlockchain2();
  }

  @Test
  /*
   * Tests the cross_addLinkedNode, cross_removeLinkedNode and cross_getListLinkedNodes APIs
   */
  public void multinodeApiTest() throws Exception {
    final int LIMIT = 100;
    final int OFFSET = 10;

    for (int i = 0; i < LIMIT; i++) {
      int portVal = 8545 + i;
      this.nodeOnBlockchain1.execute(
          crossTransactions.getAddLinkedNode(
              BigInteger.valueOf(i + OFFSET), "127.0.0.1:" + portVal));
    }
    List<BlockchainNodeInformation> nodes =
        this.nodeOnBlockchain1.execute(crossTransactions.getListLinkedNodes());
    assertThat(nodes.size()).isEqualTo(LIMIT);

    Set<BigInteger> chainIds = new HashSet<BigInteger>();
    Set<BigInteger> expectedChainIds = new HashSet<BigInteger>();
    for (int i = 0; i < LIMIT; i++) {
      chainIds.add(nodes.get(i).blockchainId);
      expectedChainIds.add(BigInteger.valueOf(i + OFFSET));
    }
    assertThat(chainIds).isEqualTo(expectedChainIds);

    for (int i = 0; i < LIMIT / 2; i++) {
      this.nodeOnBlockchain1.execute(
          crossTransactions.getRemoveLinkedNode(BigInteger.valueOf(i + OFFSET)));
      expectedChainIds.remove(BigInteger.valueOf(i + OFFSET));
    }

    nodes.clear();
    nodes = this.nodeOnBlockchain1.execute(crossTransactions.getListLinkedNodes());
    assertThat(nodes.size()).isEqualTo(LIMIT / 2);

    chainIds.clear();
    for (int i = 0; i < LIMIT / 2; i++) {
      chainIds.add(nodes.get(i).blockchainId);
    }
    assertThat(chainIds).isEqualTo(expectedChainIds);
  }

  @After
  public void closeDown() throws Exception {
    this.cluster.close();
    this.clusterBc1.close();
    this.clusterBc2.close();
  }
}
