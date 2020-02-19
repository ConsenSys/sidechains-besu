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

public class CrosschainTransactionCommitMessage extends AbstractThresholdSignedMessage {
  private BigInteger coordChainId, origChainId, txId;
  private Address coordAddress;

  // Always called with the originating transaction
  public CrosschainTransactionCommitMessage(final CrosschainTransaction transaction) {
    super(transaction);
    this.origChainId = transaction.getChainId().get();
    this.coordChainId = transaction.getCrosschainCoordinationBlockchainId().get();
    this.coordAddress = transaction.getCrosschainCoordinationContractAddress().get();
    this.txId = transaction.getCrosschainTransactionId().get();
  }

  // Always called with the originating transaction
  public CrosschainTransactionCommitMessage(
      final CrosschainTransaction transaction, final long keyVersion, final BytesValue signature) {
    super(transaction, keyVersion, signature);
    this.origChainId = transaction.getChainId().get();
    this.coordChainId = transaction.getCrosschainCoordinationBlockchainId().get();
    this.coordAddress = transaction.getCrosschainCoordinationContractAddress().get();
    this.txId = transaction.getCrosschainTransactionId().get();
  }

  public CrosschainTransactionCommitMessage(final RLPInput in) {
    super(in);
  }

  @Override
  public ThresholdSignedMessageType getType() {
    return ThresholdSignedMessageType.CROSSCHAIN_TRANSACTION_COMMIT;
  }

  @Override
  public BytesValue getEncodedCoreMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          out.writeLongScalar(ThresholdSignedMessageType.CROSSCHAIN_TRANSACTION_COMMIT.value);
          out.writeLongScalar(this.coordChainId.longValue());
          out.writeBytesValue(this.coordAddress);
          out.writeLongScalar(this.origChainId.longValue());
          out.writeBigIntegerScalar(this.txId);
          out.writeBytesValue(BytesValue.fromHexString(this.txHash.getHexString()));
          out.endList();
        });
  }

  private byte[] bigIntToUint256(final BigInteger bigInteger) {
    byte[] bVar = bigInteger.toByteArray();
    byte[] bFixed = new byte[32];
    System.arraycopy(bVar, 0, bFixed, bFixed.length - bVar.length, bVar.length);
    return bFixed;
  }

  @Override
  public BytesValue getEncodedMessageForCoordContract() {
    int messageType = getType().value;
    byte[] messageTypeBytes = new byte[32];
    messageTypeBytes[31] = (byte) messageType;

    BigInteger coordBcId = getCoordinationBlockchainId();
    byte[] coordBcId1 = bigIntToUint256(coordBcId);

    Address coordAddr = getCoordinationContractAddress();

    BigInteger orgBcId = getOriginatingBlockchainId();
    byte[] orgBcId1 = bigIntToUint256(orgBcId);

    BigInteger txId = getCrosschainTransactionId();
    byte[] txId1 = bigIntToUint256(txId);

    BytesValue txHash = getCrosschainTransactionHash();

    BytesValue result = BytesValue.wrap(messageTypeBytes);
    result = BytesValue.wrap(result, BytesValue.wrap(coordBcId1));
    result = BytesValue.wrap(result, coordAddr);
    result = BytesValue.wrap(result, BytesValue.wrap(orgBcId1));
    result = BytesValue.wrap(result, BytesValue.wrap(txId1));
    result = BytesValue.wrap(result, txHash);
    return result;
  }

  @Override
  public BytesValue getEncodedMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          out.writeLongScalar(ThresholdSignedMessageType.CROSSCHAIN_TRANSACTION_COMMIT.value);
          out.writeLongScalar(this.coordChainId.longValue());
          out.writeBytesValue(this.coordAddress);
          out.writeLongScalar(this.origChainId.longValue());
          out.writeBigIntegerScalar(this.txId);
          out.writeBytesValue(BytesValue.fromHexString(this.txHash.getHexString()));
          out.writeLongScalar(this.keyVersion);
          out.writeBytesValue(this.signature != null ? this.signature : BytesValue.EMPTY);
          out.endList();
        });
  }

  @Override
  protected void decode(final RLPInput in) {
    this.coordChainId = BigInteger.valueOf(in.readLongScalar());
    this.coordAddress = Address.wrap(in.readBytesValue());
    this.origChainId = BigInteger.valueOf(in.readLongScalar());
    this.txId = in.readBigIntegerScalar();
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
    return true;
  }
}
