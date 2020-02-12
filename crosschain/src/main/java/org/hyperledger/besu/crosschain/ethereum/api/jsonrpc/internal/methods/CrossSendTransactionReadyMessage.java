/*
 * Copyright 2020 ConsenSys AG.
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
package org.hyperledger.besu.crosschain.ethereum.api.jsonrpc.internal.methods;

import org.hyperledger.besu.crosschain.core.CrosschainController;
import org.hyperledger.besu.crosschain.core.messages.SubordinateTransactionReadyMessage;
import org.hyperledger.besu.crosschain.core.messages.ThresholdSignedMessage;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.util.bytes.BytesValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrossSendTransactionReadyMessage implements JsonRpcMethod {

  private static final Logger LOG = LogManager.getLogger();
  private static final int EXPECTED_NUM_PARAMS = 1;

  private final CrosschainController crosschainController;
  private final JsonRpcParameter parameters;

  public CrossSendTransactionReadyMessage(
      final CrosschainController crosschainController, final JsonRpcParameter parameters) {
    this.crosschainController = crosschainController;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return RpcMethod.CROSS_SEND_TRANSACTION_READY_MESSAGE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != EXPECTED_NUM_PARAMS) {
      LOG.error(
          "JSON RPC {}: Expected {} parameters. Called with {} parameters",
          getName(),
          EXPECTED_NUM_PARAMS,
          request.getParamLength());
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    Object[] params = request.getParams();
    final String subTxReadyMsgEncodedHexString = parameters.required(params, 0, String.class);
    final SubordinateTransactionReadyMessage subTxReadyMsg =
        (SubordinateTransactionReadyMessage)
            ThresholdSignedMessage.decodeEncodedMessage(
                BytesValue.fromHexString(subTxReadyMsgEncodedHexString));

    boolean txReadyMsgError = this.crosschainController.receiveSubTxReadyMsg(subTxReadyMsg);
    if (txReadyMsgError) {
      LOG.error("Error in processing subordinate transaction ready message.");
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    } else {
      LOG.info(
          "JSON RPC {}: SubordinateTransactionReadyMessage received form chain {}",
          subTxReadyMsg.getSubChainId().longValue());
      return new JsonRpcSuccessResponse(
          request.getId(), subTxReadyMsg.getEncodedMessage().getHexString());
    }
  }
}
