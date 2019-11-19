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
package org.hyperledger.besu.crosschain.core.keys;

import org.hyperledger.besu.crosschain.core.keys.generation.ThresholdKeyGenContract;
import org.hyperledger.besu.crosschain.core.keys.generation.ThresholdKeyGeneration;
import org.hyperledger.besu.crosschain.p2p.CrosschainDevP2P;
import org.hyperledger.besu.crosschain.p2p.SimulatedOtherNode;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.util.bytes.Bytes32;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CrosschainKeyManager {
  public enum ActiveCredentialStatus {
    // No credentials available for this node. The Crosschain Coordination Contract
    // does not have any public key specified for this blockchain.
    NO_CREDENTIALS,

    // The credentials related to the active public key specified by the Crosschain
    // Coordination Contract are not available on this node.
    ACTIVE_CREDENTIALS_NOT_AVAILABLE_ON_THIS_NODE,
    ACTIVE_CREDENTIALS_AVAILABLE
  }
  public enum NegotiatingCredentialsStatus {
    NO_NEGOTIATION,
    ACTIVE_NEGOTIATION,
    NEGOTIATED_CREDENTIALS_READY
  }

  private static long NOT_KNOWN = -1;
  //long highestKnownVersion = NOT_KNOWN;
  long activeKeyVersion = NOT_KNOWN;
  boolean activeVersionNotKnown = true;


  // TODO blockkchain ID will be used when interacting with the crosschain coordination contract.
//  private BigInteger blockchainId;
  private SECP256K1.KeyPair nodeKeys;

  private static class Coord {
    BigInteger coordinationBlockchainId;
    Address coodinationContract;
    Coord(final BigInteger coordinationBlockchainId, final Address coodinationContract) {
      this.coodinationContract = coodinationContract;
      this.coordinationBlockchainId = coordinationBlockchainId;
    }
  }
  List<Coord> coordinationContracts = new ArrayList<>();


  Map<Long, BlsThresholdCredentials> credentials;

  public Map<Long, ThresholdKeyGeneration> activeKeyGenerations = new TreeMap<>();
  ThresholdKeyGenContract thresholdKeyGenContract = new ThresholdKeyGenContract();
  CrosschainDevP2P p2p = new CrosschainDevP2P();



  // TODO add key generation contract address
  public CrosschainKeyManager() {

    this.credentials = CrosschainKeyManagerStorage.loadAllCredentials();
    if (this.credentials.size() != 0) {
      // TODO Set the highest known key number.

      // TODO check with the Coordination Contract to see what the active version is.

      // TODO check that this node has credentials for the version that is the "active version".
    }
  }

  public void init(
      final BigInteger sidechainId,
      final SECP256K1.KeyPair nodeKeys) {
//    this.blockchainId = sidechainId;
    this.nodeKeys = nodeKeys;

  }

  public void addCoordinationContract(final BigInteger coordinationBlockchainId, final Address coodinationContract) {
    // TODO check that this coodination contrat is not already in the list.
    this.coordinationContracts.add(new Coord(coordinationBlockchainId, coodinationContract));
  }

  public void removeCoordinationContract(final BigInteger coordinationBlockchainId, final Address coodinationContract) {
    // TODO
  }



    public BlsThresholdCredentials getActiveCredentials() {
    if (this.activeVersionNotKnown) {
      return BlsThresholdCredentials.emptyCredentials();
    }
    return this.credentials.get(activeKeyVersion);
  }

  public BlsThresholdCredentials getGenerationCompleteCredentials() {
    return null;
  }


  int NUM = 2;
  public SimulatedOtherNode[] others = new SimulatedOtherNode[NUM];

  public long generateNewKeys(final int threshold) {
    //TODO ****************************************
    this.p2p.clearSimulatedNodes();
    for (int i = 0; i < 2; i++) {
      others[i] = new SimulatedOtherNode(threshold, BigInteger.valueOf(i), this.thresholdKeyGenContract, this.p2p);
      others[i].init();
    }

    ThresholdKeyGeneration keyGen = new ThresholdKeyGeneration(threshold, this.nodeKeys, this.thresholdKeyGenContract, this.p2p);
    long keyVersionNumber = keyGen.startKeyGeneration();
    this.activeKeyGenerations.put(keyVersionNumber, keyGen);
    return keyVersionNumber;
  }




}
