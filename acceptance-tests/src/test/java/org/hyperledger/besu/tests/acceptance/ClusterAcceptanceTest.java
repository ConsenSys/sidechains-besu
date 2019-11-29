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
package org.hyperledger.besu.tests.acceptance;

import org.hyperledger.besu.tests.acceptance.dsl.AcceptanceTestBase;
import org.hyperledger.besu.tests.acceptance.dsl.node.Node;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClusterAcceptanceTest extends AcceptanceTestBase {

  private Node minerNode;
  private Node fullNode;

  @Before
  public void setUp() throws Exception {
    minerNode = besu.createMinerNode("node1");
    fullNode = besu.createArchiveNode("node2");
    cluster.start(minerNode, fullNode);
  }

  @Test
  public void shouldConnectToOtherPeer() {
    minerNode.verify(net.awaitPeerCount(1));
    fullNode.verify(net.awaitPeerCount(1));
  }

  @Test
  public void shouldRestartAfterStop() {
    minerNode.verify(net.awaitPeerCount(1));
    fullNode.verify(net.awaitPeerCount(1));
    cluster.stop();
    cluster.start(minerNode, fullNode);
    minerNode.verify(net.awaitPeerCount(1));
    fullNode.verify(net.awaitPeerCount(1));
  }
}
