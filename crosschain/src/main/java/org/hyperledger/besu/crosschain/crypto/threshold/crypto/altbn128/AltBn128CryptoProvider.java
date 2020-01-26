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
package org.hyperledger.besu.crosschain.crypto.threshold.crypto.altbn128;

import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsCryptoProvider;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crosschain.crypto.threshold.crypto.CryptoProviderBase;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.crypto.altbn128.AltBn128Fq12Pairer;
import org.hyperledger.besu.crypto.altbn128.AltBn128Fq2Point;
import org.hyperledger.besu.crypto.altbn128.AltBn128Point;
import org.hyperledger.besu.crypto.altbn128.Fq12;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.io.Serializable;
import java.math.BigInteger;

public class AltBn128CryptoProvider extends CryptoProviderBase implements BlsCryptoProvider, Serializable {
  public BlsCryptoProvider.DigestAlgorithm digestAlgorithm;

  public AltBn128CryptoProvider(final BlsCryptoProvider.DigestAlgorithm alg) {
    this.digestAlgorithm = alg;
  }

  @Override
  public BigInteger modPrime(final BigInteger val) {
    return val.mod(AltBn128Fq12Pairer.CURVE_ORDER);
  }

  @Override
  public BigInteger getPrimeModulus() {
    return AltBn128Fq12Pairer.CURVE_ORDER;
  }

  @Override
  public BlsPoint createPointE1(final BigInteger scalar) {
    AltBn128Point basedPoint = AltBn128Point.g1();
    return new AltBn128PointWrapper(basedPoint.multiply(scalar));
  }

  @Override
  public BlsPoint getBasePointE1() {
    AltBn128Point basedPoint = AltBn128Point.g1();
    return new AltBn128PointWrapper(basedPoint);
  }

  // TODO there is a lot of code duplicaiton between hashToCurveE1 and E2
  @Override
  public BlsPoint hashToCurveE1(final byte[] data) {
    BytesValue dataBV = BytesValue.wrap(data);
    BlsPoint P = null;

    switch (this.digestAlgorithm) {
      case KECCAK256:
        Bytes32 digestedData = Hash.keccak256(dataBV);
        P = mapToCurveE1(digestedData.extractArray());
        break;
      default:
        throw new Error("not implemented yet!" + this.digestAlgorithm);
    }

    return P;
  }

  private static final BigInteger MAX_LOOP_COUNT = BigInteger.TEN;

  // TODO Review this https://tools.ietf.org/html/draft-irtf-cfrg-hash-to-curve-04
  // TODO to work out whether this is a better approach.

  /**
   * Map a byte array to a point on the curve by converting the byte array to an integer and then
   * scalar multiplying the base point by the integer. Use this approach rather than those specified
   * in https://tools.ietf.org/html/draft-irtf-cfrg-hash-to-curve-04 as the map to curve function
   * needs to match something that can be implemented in Solidity.
   *
   * @data value to map to a point
   * @return point on E1
   */
  private BlsPoint mapToCurveE1(final byte[] data) {
    BigInteger q = getPrimeModulus();

    BigInteger ctr = BigInteger.ZERO;

    BlsPoint p;

    while (true) {
      BigInteger dc1 = new BigInteger(1, data);
      BigInteger x = dc1.add(ctr);
      x = x.mod(q);

      p = createPointE1(x); // map to point

      // if map is valid, we are done
      if (!p.isAtInfinity()) {
        break;
      }

      // bump counter for next round, if necessary
      ctr = ctr.add(BigInteger.ONE);
      if (ctr.equals(MAX_LOOP_COUNT)) {
        throw new Error("Failed to map to point");
      }
    }

    return (p);
  }

  @Override
  public BlsPoint createPointE2(final BigInteger scalar) {
    AltBn128Fq2Point basedPoint = AltBn128Fq2Point.g2();
    return new AltBn128Fq2PointWrapper(basedPoint.multiply(scalar));
  }

  @Override
  public BlsPoint getBasePointE2() {
    AltBn128Fq2Point basedPoint = AltBn128Fq2Point.g2();
    return new AltBn128Fq2PointWrapper(basedPoint);
  }

  @Override
  public BlsPoint hashToCurveE2(final byte[] data) {
    BytesValue dataBV = BytesValue.wrap(data);
    BlsPoint P = null;

    switch (this.digestAlgorithm) {
      case KECCAK256:
        Bytes32 digestedData = Hash.keccak256(dataBV);
        P = mapToCurveE2(digestedData.extractArray());
        break;
      default:
        throw new Error("not implemented yet!" + this.digestAlgorithm);
    }

    return P;
  }

  /**
   * Map a byte array to a point on the curve by converting the byte array to an integer and then
   * scalar multiplying the base point by the integer.
   *
   * @param data value to map to a point.
   * @return point on E2.
   */
  private BlsPoint mapToCurveE2(final byte[] data) {
    BigInteger q = getPrimeModulus();

    BigInteger ctr = BigInteger.ZERO;

    BlsPoint p;

    while (true) {
      BigInteger dc1 = new BigInteger(1, data);
      BigInteger x = dc1.add(ctr);
      x = x.mod(q);

      p = createPointE2(x); // map to point

      // if map is valid, we are done
      if (!p.isAtInfinity()) {
        break;
      }

      // bump counter for next round, if necessary
      ctr = ctr.add(BigInteger.ONE);
      if (ctr.equals(MAX_LOOP_COUNT)) {
        throw new Error("Failed to map to point");
      }
    }

    return (p);
  }

  /**
   * Create a signature as a point on the E1 curve.
   *
   * @param privateKey Private key to sign data with.
   * @param data Data to be signed.
   * @return signature.
   */
  @Override
  public BlsPoint sign(final BigInteger privateKey, final byte[] data) {
    BlsPoint hashOfData = hashToCurveE1(data);
    return hashOfData.scalarMul(privateKey);
  }

  /**
   * Verify a signature.
   *
   * @param publicKey Point on the E2 curve to verify the data with.
   * @param data Data to be verified.
   * @param signature Signature on E1 curve.
   * @return true if the signature is verified.
   */
  @Override
  public boolean verify(final BlsPoint publicKey, final byte[] data, final BlsPoint signature) {
    BlsPoint hashOfData = hashToCurveE1(data);
    BlsPoint basePointE2 = getBasePointE2();

    Fq12 pair1 =
        AltBn128Fq12Pairer.pair(
            ((AltBn128PointWrapper) hashOfData).point, ((AltBn128Fq2PointWrapper) publicKey).point);
    // System.out.println("Pair 1: " + pair1);
    Fq12 pair2 =
        AltBn128Fq12Pairer.pair(
            ((AltBn128PointWrapper) signature).point,
            ((AltBn128Fq2PointWrapper) basePointE2).point);
    // System.out.println("Pair 2: " + pair2);

    Fq12 exponent = Fq12.one();
    exponent = exponent.multiply(pair1);
    Fq12 result1 = AltBn128Fq12Pairer.finalize(exponent);
    // System.out.println("Result1: " + result1);

    exponent = Fq12.one();
    exponent = exponent.multiply(pair2);
    Fq12 result2 = AltBn128Fq12Pairer.finalize(exponent);
    // System.out.println("Result2: " + result2);

    return result1.equals(result2);
  }
}
