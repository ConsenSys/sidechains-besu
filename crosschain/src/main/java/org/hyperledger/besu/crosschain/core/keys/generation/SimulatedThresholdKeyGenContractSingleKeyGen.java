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

import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

// Simulates a contract which sits on the sidechain.
class SimulatedThresholdKeyGenContractSingleKeyGen {
  private ArrayList<BigInteger> nodeIdArray = new ArrayList<>();
  private Map<BigInteger, BlsPoint[]> coefficientPublicValues = new TreeMap<>();
  private Map<BigInteger, Bytes32[]> coefPublicPointCommitments = new TreeMap<>();
  private int threshold;

  // TODO use this to change when values can be posted.
//  private int roundDurationInBlocks;


  SimulatedThresholdKeyGenContractSingleKeyGen(final int threshold, final int roundDurationInBlocks) {
    this.threshold = threshold;
  //  this.roundDurationInBlocks = roundDurationInBlocks;
  }

  void setNodeId(final BigInteger  msgSender) {
    this.nodeIdArray.add(msgSender);
  }

  void setNodeCoefficientsCommitments(
      final BigInteger msgSender, final Bytes32[] coefPublicPointCommitments) throws Exception {
    if (coefPublicPointCommitments.length != this.threshold) {
      throw new Exception(
          "Number of coefficient public value commitments did not match expected number of coefficients");
    }
    this.coefPublicPointCommitments.put(msgSender, coefPublicPointCommitments);
  }

  void setNodeCoefficientsPublicValues(
      final BigInteger msgSender, final BlsPoint[] coefPublicPoints) throws Exception {
    if (coefPublicPoints.length != this.threshold) {
      throw new Exception(
          "Number of coefficient public values did not match expected number of coefficients");
    }

    // Check that the coefficient public points match what was committed to.
    // Reject requests to upload points which don't match the commitment.
    for (int i = 0; i < coefPublicPoints.length; i++) {
      byte[] coefPubBytes = coefPublicPoints[i].store();
      Bytes32 commitment = Hash.keccak256(BytesValue.wrap(coefPubBytes));
      Bytes32[] commitments = this.coefPublicPointCommitments.get(msgSender);

      if (!commitments[i].equals(commitment)) {
        throw new Exception("Public value did not match commitment");
      }
    }

    this.coefficientPublicValues.put(msgSender, coefPublicPoints);
  }

  int getNumberOfNodes() {
    return this.nodeIdArray.size();
  }

  BigInteger getNodeAddress(final int index) {
    return this.nodeIdArray.get(index);
  }


  public BlsPoint getCoefficientPublicValue(final BigInteger fromAddress, final int coefNumber) {
    return this.coefficientPublicValues.get(fromAddress)[coefNumber];
  }
}
