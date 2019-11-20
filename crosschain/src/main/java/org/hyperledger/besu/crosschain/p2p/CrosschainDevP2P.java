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
package org.hyperledger.besu.crosschain.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CrosschainDevP2P {
  protected static final Logger LOG = LogManager.getLogger();
  BigInteger realNodesAddress;

  public void setMyNodeAddress(final BigInteger myNodeAddress) {
    this.realNodesAddress = myNodeAddress;
  }

  /**
   * Request other nodes start the Threshold Key Generation process.
   *
   * @param keyVersion The key version to be generated.
   */
  public void requestStartNewKeyGeneration(final long keyVersion) {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestStartNewKeyGeneration(keyVersion);
    }
  }

  public void sendPrivateValues(
      final BigInteger myAddress,
      final List<BigInteger> nodeAddresses,
      final Map<BigInteger, BigInteger> mySecretShares) {
    for (BigInteger nodeAddress : nodeAddresses) {
      if (!nodeAddress.equals(myAddress)) {
        this.otherNodes
            .get(nodeAddress)
            .receivePrivateValue(myAddress, mySecretShares.get(nodeAddress));
      }
    }
  }

  CrosschainPartSecretShareCallback cb;

  public void setSecretShareCallback(final CrosschainPartSecretShareCallback implementation) {
    this.cb = implementation;
  }

  Map<BigInteger, SimulatedOtherNode> otherNodes = new TreeMap<>();

  public void clearSimulatedNodes() {
    otherNodes = new TreeMap<>();
  }

  public void addSimulatedOtherNode(final BigInteger address, final SimulatedOtherNode node) {
    otherNodes.put(address, node);
  }

  public void simulatedNodesSendPrivateValues(
      final BigInteger myAddress,
      final List<BigInteger> nodeAddresses,
      final Map<BigInteger, BigInteger> mySecretShares) {
    LOG.info("my address: {}", myAddress);
    LOG.info("real address: {}", realNodesAddress);

    for (BigInteger nodeAddress : nodeAddresses) {
      LOG.info("node address: {}", nodeAddress);
      if (nodeAddress.equals(realNodesAddress)) {
        this.cb.storePrivateSecretShareCallback(myAddress, mySecretShares.get(nodeAddress));
      }
      else if (!nodeAddress.equals(myAddress)) {
        LOG.info("processing node address: {}", nodeAddress);
        LOG.info(" othernodes.get(): {}", this.otherNodes.get(nodeAddress));
        this.otherNodes
            .get(nodeAddress)
            .receivePrivateValue(myAddress, mySecretShares.get(nodeAddress));
      }
    }
  }


  public void requestPostCommits(final long keyVersion) {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestPostCommits(keyVersion);
    }
  }

  public void requestPostPublicValues(final long keyVersion) {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestPostPublicValues(keyVersion);
    }
  }


  public void requestGetOtherNodeCoefs(final long keyVersion) {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestGetOtherNodeCoefs(keyVersion);
    }
  }

  public void requestSendPrivateValues(final long keyVersion) {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestSendPrivateValues(keyVersion);
    }
  }

  public void requestNodesCompleteKeyGen() {
    for (SimulatedOtherNode node : this.otherNodes.values()) {
      node.requestNodesCompleteKeyGen();
    }
  }


}
