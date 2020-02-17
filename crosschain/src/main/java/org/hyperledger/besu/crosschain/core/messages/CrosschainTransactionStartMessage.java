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
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;

public class CrosschainTransactionStartMessage extends AbstractThresholdSignedMessage {

  public CrosschainTransactionStartMessage(final CrosschainTransaction transaction) {
    super(transaction);
  }

  public CrosschainTransactionStartMessage(
      final CrosschainTransaction transaction, final long keyVersion, final BytesValue signature) {
    super(transaction, keyVersion, signature);
  }

  public CrosschainTransactionStartMessage(final RLPInput in) {
    super(in);
  }

  @Override
  public ThresholdSignedMessageType getType() {
    return ThresholdSignedMessageType.CROSSCHAIN_TRANSACTION_START;
  }

  @Override
  public boolean verifiedByCoordContract() {
    return true;
  }

  public BigInteger getTransactionTimeoutBlockNumber() {
    return this.transaction.getCrosschainTransactionTimeoutBlockNumber().get();
  }

  @Override
  public BytesValue getEncodedCoreMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          sharedEncoding(out);
          out.writeBigIntegerScalar(getTransactionTimeoutBlockNumber());
          out.endList();
        });
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

    BigInteger timeoutBlockNumber = getTransactionTimeoutBlockNumber();
    byte[] timeout1 = bigIntToUint256(timeoutBlockNumber);

    BytesValue result = BytesValue.wrap(messageTypeBytes);
    result = BytesValue.wrap(result, BytesValue.wrap(coordBcId1));
    result = BytesValue.wrap(result, coordAddr);
    result = BytesValue.wrap(result, BytesValue.wrap(orgBcId1));
    result = BytesValue.wrap(result, BytesValue.wrap(txId1));
    result = BytesValue.wrap(result, txHash);
    result = BytesValue.wrap(result, BytesValue.wrap(timeout1));
    return result;
  }

  private byte[] bigIntToUint256(final BigInteger bigInteger) {
    byte[] bVar = bigInteger.toByteArray();
    byte[] bFixed = new byte[32];
    System.arraycopy(bVar, 0, bFixed, bFixed.length - bVar.length, bVar.length);
    return bFixed;
  }

  @Override
  public BytesValue getEncodedMessage() {
    return RLP.encode(
        out -> {
          out.startList();
          sharedEncoding(out);
          out.writeBigIntegerScalar(getTransactionTimeoutBlockNumber());
          out.writeBytesValue(RLP.encode(this.transaction::writeTo));
          out.writeLongScalar(this.keyVersion);
          out.writeBytesValue(this.signature != null ? this.signature : BytesValue.EMPTY);
          out.endList();
        });
  }

  @Override
  protected void decode(final RLPInput in) {
    // Read the transaction timeout block number
    in.readBigIntegerScalar();
    final RLPInput inTrans = RLP.input(in.readBytesValue());
    this.transaction = CrosschainTransaction.readFrom(inTrans);
    this.keyVersion = in.readLongScalar();
    BytesValue sig = in.readBytesValue();
    if (sig.isZero()) {
      this.signature = null;
    } else {
      this.signature = sig;
    }
  }
}
