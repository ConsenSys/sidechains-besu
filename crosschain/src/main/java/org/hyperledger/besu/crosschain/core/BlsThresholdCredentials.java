package org.hyperledger.besu.crosschain.core;

import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.util.bytes.BytesValue;
import org.hyperledger.besu.util.uint.UInt256;

public class BlsThresholdCredentials {



//
//  BytesValue getEncoded() {
//    RLP.encode(
//        out -> {
//          out.startList();
//          out.writeLongScalar(type.value);
//          if (crosschainCoordinationBlockchainId != null) {
//            out.writeBigIntegerScalar(crosschainCoordinationBlockchainId);
//            out.writeBytesValue(crosschainCoordinationContractAddress);
//            out.writeBigIntegerScalar(crosschainTransactionTimeoutBlockNumber);
//            out.writeBigIntegerScalar(crosschainTransactionId);
//            if (crosschainFromSidechainId != null) {
//              out.writeBigIntegerScalar(originatingSidechainId);
//              out.writeBigIntegerScalar(crosschainFromSidechainId);
//              out.writeBytesValue(crosschainFromAddress);
//            }
//          }
//          out.writeLongScalar(nonce);
//          out.writeUInt256Scalar(gasPrice);
//          out.writeLongScalar(gasLimit);
//          out.writeBytesValue(to == null ? BytesValue.EMPTY : to);
//          out.writeUInt256Scalar(value);
//          out.writeBytesValue(payload);
//          if (crosschainCoordinationBlockchainId != null) {
//            out.writeList(
//                subordinateTransactionsAndViews,
//                ((crosschainTransaction, rlpOutput) -> {
//                  BytesValueRLPOutput temp = new BytesValueRLPOutput();
//                  crosschainTransaction.writeTo(temp);
//                  rlpOutput.writeBytesValue(temp.encoded());
//                }));
//          }
//          if (chainId.isPresent()) {
//            out.writeBigIntegerScalar(chainId.get());
//            out.writeUInt256Scalar(UInt256.ZERO);
//            out.writeUInt256Scalar(UInt256.ZERO);
//          }
//          out.endList();
//        }));
//  }
}
