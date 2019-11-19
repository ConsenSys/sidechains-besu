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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsCryptoProvider;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crosschain.crypto.threshold.scheme.ThresholdScheme;
import org.hyperledger.besu.crosschain.p2p.CrosschainDevP2P;
import org.hyperledger.besu.crosschain.p2p.CrosschainPartSecretShareCallback;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.crypto.PRNGSecureRandom;
import org.hyperledger.besu.crypto.SECP256K1;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ThresholdKeyGeneration {
  protected static final Logger LOG = LogManager.getLogger();

  private int threshold;
  private SecureRandom prng = new PRNGSecureRandom();

  private Map<BigInteger, BigInteger> mySecretShares;
  private BlsPoint[] myCoeffsPublicValues;
  private Bytes32[] myCoeffsPublicValueCommitments;
  private BigInteger myNodeAddress;
  private List<BigInteger> nodesStillActiveInKeyGeneration;

  private Map<BigInteger, BigInteger> receivedSecretShares = new TreeMap<>();

  ThresholdKeyGenContract thresholdKeyGenContract;
  CrosschainDevP2P p2p;

  private Map<BigInteger, BlsPoint[]> otherNodeCoefficients;

  private BlsCryptoProvider cryptoProvider;

  private BigInteger privateKeyShare = null;

  private BlsPoint publicKey = null;

  private ThresholdScheme thresholdScheme;

  public ThresholdKeyGeneration(final int threshold, final SECP256K1.KeyPair nodeKeyPair, final ThresholdKeyGenContract thresholdKeyGenContract, final CrosschainDevP2P p2p) {
    this.threshold = threshold;
    this.thresholdKeyGenContract = thresholdKeyGenContract;
    this.p2p = p2p;
    this.cryptoProvider =
        BlsCryptoProvider.getInstance(
            BlsCryptoProvider.CryptoProviderTypes.LOCAL_ALT_BN_128,
            BlsCryptoProvider.DigestAlgorithm.KECCAK256);


    this.thresholdScheme = new ThresholdScheme(this.cryptoProvider, this.threshold, this.prng);


    // TODO want the address based on the public key.
    // Create a node id based on the public key.
    SECP256K1.PublicKey publicKey = nodeKeyPair.getPublicKey();
    this.myNodeAddress = new BigInteger(Address.extract(publicKey).toUnprefixedString(), 16);
  }



  public long startKeyGeneration() {
    long keyVersionNumber = this.thresholdKeyGenContract.getExpectedKeyGenerationVersion();

    this.p2p.setSecretShareCallback(new CrosschainPartSecretShareCallbackImpl());

    // TODO: Put the following in a "do later" clause
    try {
      this.thresholdKeyGenContract.startNewKeyGeneration(keyVersionNumber, this.threshold);
    } catch (Exception ex) {
      // TODO this will could fail when the transation executes.
    }

    // TODO: Put the following in a "do later" clause
    // Request all nodes start the process in parallel with this node.
    this.p2p.requestStartNewKeyGeneration(keyVersionNumber);

    // TODO: After some time-out, to allow other nodes to post their node ids.
    // TODO Use vertix
    try {
      // Probably have to wait multiple block times.
      Thread.sleep(2000);
    } catch (Exception e) {}

    int numberOfNodes = 0;
    try {
      numberOfNodes = this.thresholdKeyGenContract.getNumberOfNodes(keyVersionNumber);
    } catch (Exception ex) {

    }
    if (numberOfNodes < this.threshold) {
      // Key generation has failed. Not enough nodes participated by posting X values.
      // TODO indicate failure some how
    }

    // Post Commitments Round
    this.nodesStillActiveInKeyGeneration = new ArrayList<>();
    BigInteger[] nodeAddresses = new BigInteger[numberOfNodes];
    try {
      for (int i = 0; i < numberOfNodes; i++) {
        BigInteger address = this.thresholdKeyGenContract.getNodeAddress(keyVersionNumber, i);
        this.nodesStillActiveInKeyGeneration.add(address);
        nodeAddresses[i] = address;
      }
    } catch (Exception ex) {

    }

    generatePartsOfKeySharesPublicValueAndCommitments(nodeAddresses);
    try {
      this.thresholdKeyGenContract.setNodeCoefficientsCommitments(keyVersionNumber, this.myCoeffsPublicValueCommitments);
    } catch (Exception ex) {

    }

    // TODO wait for a period of time, to let other nodes post their commitments.
    try {
      // Probably have to wait multiple block times.
      Thread.sleep(2000);
    } catch (Exception e) {}


    // Post Public Values Round.
    // TODO only publish the public values after all of the commitments are posted.
    // Post public values of coefficient to threshold key gen contract.
    try {
      this.thresholdKeyGenContract.setNodeCoefficientsPublicValues(keyVersionNumber, this.myCoeffsPublicValues);
    } catch (Exception e) {

    }

    // Get all of the other node's coefficient public values.
    try {
      // Probably have to wait multiple block times.
      Thread.sleep(2000);
    } catch (Exception e) {}
    this.otherNodeCoefficients = new TreeMap<BigInteger, BlsPoint[]>();
    try {
      for (BigInteger nodeAddress: this.nodesStillActiveInKeyGeneration) {
        BlsPoint[] points = new BlsPoint[this.myCoeffsPublicValues.length];
        for (int j = 0; j < this.myCoeffsPublicValues.length; j++) {
          points[j] = this.thresholdKeyGenContract.getCoefficientPublicValue(keyVersionNumber, nodeAddress, j);
        }
        this.otherNodeCoefficients.put(nodeAddress, points);
      }
    } catch (Exception ex) {

    }

    try {
      // Probably have to wait multiple block times.
      Thread.sleep(2000);
    } catch (Exception e) {}


    // TODO send private values
    // TODO Note that the nodeAddresses will have had some purged for nodes that have not posted the commitments or public values.
    this.p2p.sendPrivateValues(this.myNodeAddress, this.nodesStillActiveInKeyGeneration, this.mySecretShares);

    // Calculate private key shares and public key round.
    // TODO need to account for some private key shares not being sent / nefarious actors.
    this.privateKeyShare = calculateMyPrivateKeyShare();

    this.publicKey = calculatePublicKey();

    return keyVersionNumber;
  }





  private void generatePartsOfKeySharesPublicValueAndCommitments(final BigInteger[] xValues) {
    // Generate random coefficients.
    BigInteger[] coeffs = thresholdScheme.generateRandomCoefficients();

    // Generate the secret share parts (the y values).
    BigInteger[] myPartSecretShares = thresholdScheme.generateShares(xValues, coeffs);
    this.mySecretShares = new TreeMap<>();
    for (int i=0; i<xValues.length; i++) {
      this.mySecretShares.put(xValues[i], myPartSecretShares[i]);
    }

    // Generate public values.
    this.myCoeffsPublicValues = new BlsPoint[coeffs.length];
    for (int i = 0; i < coeffs.length; i++) {
      this.myCoeffsPublicValues[i] = this.cryptoProvider.createPointE2(coeffs[i]);
    }

    // Create and post the commitments to the coefficient public values.
    this.myCoeffsPublicValueCommitments = new Bytes32[coeffs.length];
    for (int i = 0; i < coeffs.length; i++) {
      byte[] coefPubBytes = myCoeffsPublicValues[i].store();
      this.myCoeffsPublicValueCommitments[i] = Hash.keccak256(BytesValue.wrap(coefPubBytes));
    }
  }



  private BigInteger calculateMyPrivateKeyShare() {
    BigInteger privateKeyShareAcc = this.mySecretShares.get(this.myNodeAddress);;

    for (BigInteger nodeAddress: this.nodesStillActiveInKeyGeneration) {
      privateKeyShareAcc = privateKeyShareAcc.add(this.receivedSecretShares.get(nodeAddress));
      privateKeyShareAcc = this.cryptoProvider.modPrime(privateKeyShareAcc);
    }
    return privateKeyShareAcc;
  }

  public BlsPoint getPublicKey() {
    return this.publicKey;
  }


  /**
   * The public key is the sum of the constant coefficient for all curves.
   *
   * <p>That is, the public key is the point for X=0. Given equations y = a x^3 + b x^2 + c x + d,
   * the x = 0 value is d. Summing the d values for all curves gives the public key.
   */
  private BlsPoint calculatePublicKey() {
    final int numCoeffs = this.threshold - 1;
    BlsPoint yValue = this.myCoeffsPublicValues[numCoeffs];

    for (BigInteger nodeAddress: this.nodesStillActiveInKeyGeneration) {
      BlsPoint pubShare = this.otherNodeCoefficients.get(nodeAddress)[numCoeffs];
      yValue = yValue.add(pubShare);
    }

    return yValue;
  }



  class CrosschainPartSecretShareCallbackImpl implements CrosschainPartSecretShareCallback {
    @Override
    public void storePrivateSecretShareCallback(final BigInteger nodeId, final BigInteger secretShare) {
      synchronized (this) {
        // Check that the secret share corresponds to a public value which is on the curve
        // defined by the coefficients the node published to the ThresholdKeyGenContract.
        BlsPoint publicKeyShare = cryptoProvider.createPointE2(secretShare);

        BlsPoint calculatedPublicKeyShare =
            thresholdScheme.generatePublicKeyShare(myNodeAddress, otherNodeCoefficients.get(nodeId));

        if (!publicKeyShare.equals(calculatedPublicKeyShare)) {
          LOG.error("Private share from {} did not match public coefficients.", nodeId);
        }

        receivedSecretShares.put(nodeId, secretShare);
      }
    }
  }


  // JUST FOR TESTING
  public BigInteger getPrivateKeyShare() {
    return this.privateKeyShare;
  }

  public BigInteger getMyNodeAddress() {
    return this.myNodeAddress;
  }


}
