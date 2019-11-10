package org.hyperledger.besu.crosschain.ethereum.api.jsonrpc.internal.methods;

import org.hyperledger.besu.crosschain.core.CrosschainProcessor;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class CrossBlockchainPublicKey  implements JsonRpcMethod {

  private final JsonRpcParameter parameters;

  private final CrosschainProcessor crosschainProcessor;

  public CrossBlockchainPublicKey(
      final CrosschainProcessor crosschainProcessor, final JsonRpcParameter parameters) {
    this.parameters = parameters;
    this.crosschainProcessor = crosschainProcessor;
  }

  @Override
  public String getName() {
    return RpcMethod.CROSS_CHECK_UNLOCK.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    if (request.getParamLength() != 0) {
      return new JsonRpcErrorResponse(request.getId(), JsonRpcError.INVALID_PARAMS);
    }

    // TODO Get the public key  this.crosschainProcessor.checkUnlock(address);
    // TODO return the public key.

    //
    return new JsonRpcSuccessResponse(request.getId());
  }
}
