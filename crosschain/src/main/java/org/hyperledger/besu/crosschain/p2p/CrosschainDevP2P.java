package org.hyperledger.besu.crosschain.p2p;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CrosschainDevP2P {
  BigInteger realNodesAddress;

  /**
   * Request other nodes start the Threshold Key Generation process.
   *
   * @param keyVersion The key version to be generated.
   */
  public void requestStartNewKeyGeneration(final long keyVersion) {
    for (SimulatedOtherNode node: this.otherNodes.values()) {
      node.requestStartNewKeyGeneration(keyVersion);
    }
  }


  public void sendPrivateValues(final BigInteger myAddress, final List<BigInteger> nodeAddresses, final Map<BigInteger, BigInteger> mySecretShares) {
    this.realNodesAddress = myAddress;
    for (BigInteger nodeAddress: nodeAddresses) {
      if (!nodeAddress.equals(myAddress)) {
        this.otherNodes.get(nodeAddress).receivePrivateValue(myAddress, mySecretShares.get(nodeAddress));
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


  public void simulatedNodesSendPrivateValues(final BigInteger myAddress, final List<BigInteger> nodeAddresses, final Map<BigInteger, BigInteger> mySecretShares) {
    for (BigInteger nodeAddress: nodeAddresses) {
      if (nodeAddress.equals(realNodesAddress)) {
        this.cb.storePrivateSecretShareCallback(myAddress, mySecretShares.get(nodeAddress));
      }
      if (!nodeAddress.equals(myAddress)) {
        this.otherNodes.get(nodeAddress).receivePrivateValue(myAddress, mySecretShares.get(nodeAddress));
      }
    }

  }


}
