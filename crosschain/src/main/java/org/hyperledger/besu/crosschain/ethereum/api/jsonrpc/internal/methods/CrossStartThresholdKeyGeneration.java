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
package org.hyperledger.besu.crosschain.ethereum.api.jsonrpc.internal.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.crosschain.core.CrosschainController;
import org.hyperledger.besu.crosschain.core.keys.BlsThresholdCryptoSystem;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.Quantity;

public class CrossStartThresholdKeyGeneration implements JsonRpcMethod {

  private static final Logger LOG = LogManager.getLogger();

  private final CrosschainController crosschainController;
  private final JsonRpcParameter parameters;

  public CrossStartThresholdKeyGeneration(
      final CrosschainController crosschainController, final JsonRpcParameter parameters) {
    this.crosschainController = crosschainController;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return RpcMethod.CROSS_START_THRESHOLD_KEY_GENERATION.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != 2) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    Object[] params = request.getParams();
    final int threshold = parameters.required(params, 0, Integer.TYPE);
    final int algorithmInt = parameters.required(params, 1, Integer.TYPE);
    BlsThresholdCryptoSystem algorithm;
    try {
      algorithm = BlsThresholdCryptoSystem.create(algorithmInt);
    } catch (RuntimeException ex) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }
    LOG.info("JSON RPC Request to start key generation. Threshold {}, Algorithm {}", threshold, algorithm);

    long keyVersion = this.crosschainController.crossStartThresholdKeyGeneration(threshold, algorithm);

    return new JsonRpcSuccessResponse(request.getId(), Quantity.create(keyVersion));
  }

}
