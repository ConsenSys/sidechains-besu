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
package org.hyperledger.besu.crosschain.core.messages;

import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;

public class SubordinateTransactionReadyMessage extends AbstractThresholdSignedMessage {

  private BigInteger origChainId, subChainId, coordChainId;
  private Address coordAddress;

  public SubordinateTransactionReadyMessage(final CrosschainTransaction transaction) {
    super(transaction);
    // Because the transaction is well-formed, we can assume that all optional
    // fields are set.
    this.origChainId = transaction.getOriginatingSidechainId().get();
    this.subChainId = transaction.getChainId().get();
    this.coordChainId = transaction.getCrosschainCoordinationBlockchainId().get();
    this.coordAddress = transaction.getCrosschainCoordinationContractAddress().get();
  }

  public SubordinateTransactionReadyMessage(
      final CrosschainTransaction transaction, final long keyVersion, final BytesValue signature) {
    super(transaction, keyVersion, signature);
    this.origChainId = transaction.getOriginatingSidechainId().get();
    this.subChainId = transaction.getChainId().get();
    this.coordChainId = transaction.getCrosschainCoordinationBlockchainId().get();
    this.coordAddress = transaction.getCrosschainCoordinationContractAddress().get();
  }

  public SubordinateTransactionReadyMessage(final RLPInput in) {
    super(in);
  }

  @Override
  public ThresholdSignedMessageType getType() {
    return ThresholdSignedMessageType.SUBORDINATE_TRANSACTION_READY;
  }

  public BigInteger getOrigChainId() {
    return this.origChainId;
  }

  public BigInteger getSubChainId() {
    return this.subChainId;
  }

  public BigInteger getCoordChainId() {
    return this.coordChainId;
  }

  public Address getCoordAddress() {
    return this.coordAddress;
  }

  @Override
  public BytesValue getEncodedCoreMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          out.writeLongScalar(ThresholdSignedMessageType.SUBORDINATE_TRANSACTION_READY.value);
          out.writeLongScalar(this.origChainId.longValue());
          out.writeLongScalar(this.subChainId.longValue());
          out.writeLongScalar(this.coordChainId.longValue());
          out.writeBytesValue(this.coordAddress);
          out.writeBytesValue(BytesValue.fromHexString(this.txHash.getHexString()));
          out.endList();
        });
  }

  @Override
  public BytesValue getEncodedMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          out.writeLongScalar(ThresholdSignedMessageType.SUBORDINATE_TRANSACTION_READY.value);
          out.writeLongScalar(this.origChainId.longValue());
          out.writeLongScalar(this.subChainId.longValue());
          out.writeLongScalar(this.coordChainId.longValue());
          out.writeBytesValue(this.coordAddress);
          out.writeBytesValue(BytesValue.fromHexString(this.txHash.getHexString()));
          out.writeLongScalar(this.keyVersion);
          out.writeBytesValue(this.signature != null ? this.signature : BytesValue.EMPTY);
          out.endList();
        });
  }

  @Override
  protected void decode(final RLPInput in) {
    this.origChainId = BigInteger.valueOf(in.readLongScalar());
    this.subChainId = BigInteger.valueOf(in.readLongScalar());
    this.coordChainId = BigInteger.valueOf(in.readLongScalar());
    this.coordAddress = Address.wrap(in.readBytesValue());
    String hashHexString = in.readBytesValue().getHexString();
    this.txHash = Hash.fromHexString(hashHexString);
    this.keyVersion = in.readLongScalar();
    BytesValue sig = in.readBytesValue();
    if (sig.isZero()) {
      this.signature = null;
    } else {
      this.signature = sig;
    }
  }

  @Override
  public boolean verifiedByCoordContract() {
    return false;
  }
}
