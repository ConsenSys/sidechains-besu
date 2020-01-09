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
package org.hyperledger.besu.tests.acceptance.crosschain.viewtxcallArgs.generated;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.CrosschainContext;
import org.web3j.tx.CrosschainContract;
import org.web3j.tx.CrosschainTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the org.web3j.codegen.CrosschainSolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/PegaSysEng/sidechains-web3j/tree/master/besucodegen">codegen module</a>
 * to update.
 *
 * <p>Generated with web3j version 4.6.0-SNAPSHOT.
 */
@SuppressWarnings("rawtypes")
public class FooArgsCtrt extends CrosschainContract {
  private static final String BINARY =
      "608060405234801561001057600080fd5b506000805561020d806100246000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80638b44cef114610051578063c0bbc9f1146100d3578063c8ba41421461014c578063fd470b2d14610154575b600080fd5b6100c16004803603602081101561006757600080fd5b81019060208101813564010000000081111561008257600080fd5b82018360208201111561009457600080fd5b803590602001918460208302840111640100000000831117156100b657600080fd5b509092509050610180565b60408051918252519081900360200190f35b61014a600480360360408110156100e957600080fd5b8135919081019060408101602082013564010000000081111561010b57600080fd5b82018360208201111561011d57600080fd5b8035906020019184600183028401116401000000008311171561013f57600080fd5b5090925090506101a1565b005b6100c16101ab565b61014a6004803603604081101561016a57600080fd5b50803590602001356001600160a01b03166101b1565b60008282600019810181811061019257fe5b90506020020135905092915050565b9190910160005550565b60005481565b600191909155600280546001600160a01b0319166001600160a01b0390921691909117905556fea265627a7a72315820c7c090f8ef69d66a564395ca356610623f30c895519c8ad4d791ca2ef97568dc64736f6c634300050c0032";

  public static final String FUNC_FOO = "foo";

  public static final String FUNC_FOOFLAG = "fooFlag";

  public static final String FUNC_SETPROPERTIESFORBAR = "setPropertiesForBar";

  public static final String FUNC_UPDATESTATE = "updateState";

  @Deprecated
  protected FooArgsCtrt(
      String contractAddress,
      Besu besu,
      CrosschainTransactionManager crosschainTransactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
  }

  protected FooArgsCtrt(
      String contractAddress,
      Besu besu,
      CrosschainTransactionManager crosschainTransactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
  }

  public RemoteFunctionCall<BigInteger> foo(List<BigInteger> arg1) {
    final Function function =
        new Function(
            FUNC_FOO,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                    org.web3j.abi.datatypes.generated.Uint256.class,
                    org.web3j.abi.Utils.typeMap(
                        arg1, org.web3j.abi.datatypes.generated.Uint256.class))),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public byte[] foo_AsSignedCrosschainSubordinateView(
      List<BigInteger> arg1, final CrosschainContext crosschainContext) throws IOException {
    final Function function =
        new Function(
            FUNC_FOO,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                    org.web3j.abi.datatypes.generated.Uint256.class,
                    org.web3j.abi.Utils.typeMap(
                        arg1, org.web3j.abi.datatypes.generated.Uint256.class))),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return createSignedSubordinateView(function, crosschainContext);
  }

  public RemoteFunctionCall<BigInteger> fooFlag() {
    final Function function =
        new Function(
            FUNC_FOOFLAG,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public byte[] fooFlag_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext)
      throws IOException {
    final Function function =
        new Function(
            FUNC_FOOFLAG,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return createSignedSubordinateView(function, crosschainContext);
  }

  public RemoteFunctionCall<TransactionReceipt> setPropertiesForBar(
      BigInteger _barChainId, String _barCtrtAddress) {
    final Function function =
        new Function(
            FUNC_SETPROPERTIESFORBAR,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(_barChainId),
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public byte[] setPropertiesForBar_AsSignedCrosschainSubordinateTransaction(
      BigInteger _barChainId, String _barCtrtAddress, final CrosschainContext crosschainContext)
      throws IOException {
    final Function function =
        new Function(
            FUNC_SETPROPERTIESFORBAR,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(_barChainId),
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)),
            Collections.<TypeReference<?>>emptyList());
    return createSignedSubordinateTransaction(function, crosschainContext);
  }

  public RemoteFunctionCall<TransactionReceipt> setPropertiesForBar_AsCrosschainTransaction(
      BigInteger _barChainId, String _barCtrtAddress, final CrosschainContext crosschainContext) {
    final Function function =
        new Function(
            FUNC_SETPROPERTIESFORBAR,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(_barChainId),
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallCrosschainTransaction(function, crosschainContext);
  }

  public RemoteFunctionCall<TransactionReceipt> updateState(BigInteger magicNum, String str) {
    final Function function =
        new Function(
            FUNC_UPDATESTATE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(magicNum),
                new org.web3j.abi.datatypes.Utf8String(str)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public byte[] updateState_AsSignedCrosschainSubordinateTransaction(
      BigInteger magicNum, String str, final CrosschainContext crosschainContext)
      throws IOException {
    final Function function =
        new Function(
            FUNC_UPDATESTATE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(magicNum),
                new org.web3j.abi.datatypes.Utf8String(str)),
            Collections.<TypeReference<?>>emptyList());
    return createSignedSubordinateTransaction(function, crosschainContext);
  }

  public RemoteFunctionCall<TransactionReceipt> updateState_AsCrosschainTransaction(
      BigInteger magicNum, String str, final CrosschainContext crosschainContext) {
    final Function function =
        new Function(
            FUNC_UPDATESTATE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(magicNum),
                new org.web3j.abi.datatypes.Utf8String(str)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallCrosschainTransaction(function, crosschainContext);
  }

  @Deprecated
  public static FooArgsCtrt load(
      String contractAddress,
      Besu besu,
      CrosschainTransactionManager crosschainTransactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new FooArgsCtrt(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
  }

  public static FooArgsCtrt load(
      String contractAddress,
      Besu besu,
      CrosschainTransactionManager crosschainTransactionManager,
      ContractGasProvider contractGasProvider) {
    return new FooArgsCtrt(
        contractAddress, besu, crosschainTransactionManager, contractGasProvider);
  }

  public static RemoteCall<FooArgsCtrt> deployLockable(
      Besu besu,
      CrosschainTransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    CrosschainContext crosschainContext = null;
    return deployLockableContractRemoteCall(
        FooArgsCtrt.class,
        besu,
        transactionManager,
        contractGasProvider,
        BINARY,
        "",
        crosschainContext);
  }

  @Deprecated
  public static RemoteCall<FooArgsCtrt> deployLockable(
      Besu besu,
      CrosschainTransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    CrosschainContext crosschainContext = null;
    return deployLockableContractRemoteCall(
        FooArgsCtrt.class,
        besu,
        transactionManager,
        gasPrice,
        gasLimit,
        BINARY,
        "",
        crosschainContext);
  }

  public static RemoteCall<FooArgsCtrt> deployLockable(
      Besu besu,
      CrosschainTransactionManager transactionManager,
      ContractGasProvider contractGasProvider,
      final CrosschainContext crosschainContext) {
    return deployLockableContractRemoteCall(
        FooArgsCtrt.class,
        besu,
        transactionManager,
        contractGasProvider,
        BINARY,
        "",
        crosschainContext);
  }

  @Deprecated
  public static RemoteCall<FooArgsCtrt> deployLockable(
      Besu besu,
      CrosschainTransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit,
      final CrosschainContext crosschainContext) {
    return deployLockableContractRemoteCall(
        FooArgsCtrt.class,
        besu,
        transactionManager,
        gasPrice,
        gasLimit,
        BINARY,
        "",
        crosschainContext);
  }
}
