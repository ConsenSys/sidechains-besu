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

import org.hyperledger.besu.crosschain.core.keys.generation.KeyGenFailureToCompleteReason;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds all of the information related to a round of key generation.
 */
public class BlsThresholdCredentials extends BlsThresholdPubKey {
  private Map<BigInteger, BigInteger> mySecretShares;
  private BigInteger myNodeAddress;
  private Set<BigInteger> nodesStillActiveInKeyGeneration;
  private Map<BigInteger, KeyGenFailureToCompleteReason> nodesNoLongerInKeyGeneration;
  private KeyGenFailureToCompleteReason failureReason;

  public BlsThresholdCredentials(
      final long keyVersion,
      final int threshold,
      final BlsPoint publicKey,
      final BigInteger blockchainId,
      final BlsThresholdCryptoSystem algorithm,
      final Map<BigInteger, BigInteger> mySecretShares,
      final BigInteger myNodeAddress,
      final Set<BigInteger> nodesStillActiveInKeyGeneration,
      final Map<BigInteger, KeyGenFailureToCompleteReason> nodesNoLongerInKeyGeneration,
      final KeyGenFailureToCompleteReason failureReason) {
    super(publicKey, keyVersion, threshold, blockchainId, algorithm);
    this.mySecretShares = mySecretShares;
    this.myNodeAddress = myNodeAddress;
    this.nodesStillActiveInKeyGeneration = nodesStillActiveInKeyGeneration;
    this.nodesNoLongerInKeyGeneration = nodesNoLongerInKeyGeneration;
    this.failureReason = failureReason;
  }

  public Map<BigInteger, BigInteger> getMySecretShares() {
    return mySecretShares;
  }

  public BigInteger getMyNodeAddress() {
    return myNodeAddress;
  }

  public Set<BigInteger> getNodesCompletedKeyGeneration() {
    return nodesStillActiveInKeyGeneration;
  }

  public Map<BigInteger, KeyGenFailureToCompleteReason> getNodesDoppedOutOfKeyGeneration() {
    return nodesNoLongerInKeyGeneration;
  }

  public KeyGenFailureToCompleteReason getFailureReason() {
    return failureReason;
  }


  public static class Builder {
    private long keyVersion;
    private int threshold;
    private BlsPoint publicKey;
    private BigInteger blockchainId;
    private BlsThresholdCryptoSystem algorithm;
    private Map<BigInteger, BigInteger> mySecretShares;
    private BigInteger myNodeAddress;
    private Set<BigInteger> nodesStillActiveInKeyGeneration;
    private Map<BigInteger, KeyGenFailureToCompleteReason> nodesNoLongerInKeyGeneration;
    private KeyGenFailureToCompleteReason failureReason;


    public Builder keyVersion(long keyVersion) {
      this.keyVersion = keyVersion;
      return this;
    }
    public Builder threshold(int threshold) {
      this.threshold = threshold;
      return this;
    }
    public Builder publicKey(BlsPoint publicKey) {
      this.publicKey = publicKey;
      return this;
    }
    public Builder blockchainId(BigInteger blockchainId) {
      this.blockchainId = blockchainId;
      return this;
    }
    public Builder algorithm(BlsThresholdCryptoSystem algorithm) {
      this.algorithm = algorithm;
      return this;
    }
    public Builder mySecretShares(Map<BigInteger, BigInteger> mySecretShares) {
      this.mySecretShares = mySecretShares;
      return this;
    }
    public Builder myNodeAddress(BigInteger myNodeAddress) {
      this.myNodeAddress = myNodeAddress;
      return this;
    }
    public Builder nodesStillActiveInKeyGeneration(Set<BigInteger> nodesStillActiveInKeyGeneration) {
      this.nodesStillActiveInKeyGeneration = nodesStillActiveInKeyGeneration;
      return this;
    }
    public Builder nodesNoLongerInKeyGeneration(Map<BigInteger, KeyGenFailureToCompleteReason> nodesNoLongerInKeyGeneration) {
      this.nodesNoLongerInKeyGeneration = nodesNoLongerInKeyGeneration;
      return this;
    }
    public Builder failureReason(KeyGenFailureToCompleteReason failureReason) {
      this.failureReason = failureReason;
      return this;
    }

    public BlsThresholdCredentials build() {
      return new BlsThresholdCredentials(
        keyVersion,
        threshold,
        publicKey,
        blockchainId,
        algorithm,
        mySecretShares,
        myNodeAddress,
        nodesStillActiveInKeyGeneration,
        nodesNoLongerInKeyGeneration,
        failureReason
      );
    }

  }
}
