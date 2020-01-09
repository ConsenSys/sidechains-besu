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
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the org.web3j.codegen.CrosschainSolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/PegaSysEng/sidechains-web3j/tree/master/besucodegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.6.0-SNAPSHOT.
 */
@SuppressWarnings("rawtypes")
public class FooArgsCtrt extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b506000805561026a806100246000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80637e4ab77a14610051578063c0bbc9f114610129578063c8ba4142146101a2578063fd470b2d146101aa575b600080fd5b6101176004803603606081101561006757600080fd5b81019060208101813564010000000081111561008257600080fd5b82018360208201111561009457600080fd5b803590602001918460208302840111640100000000831117156100b657600080fd5b919390928235926040810190602001356401000000008111156100d857600080fd5b8201836020820111156100ea57600080fd5b8035906020019184600183028401116401000000008311171561010c57600080fd5b5090925090506101d6565b60408051918252519081900360200190f35b6101a06004803603604081101561013f57600080fd5b8135919081019060408101602082013564010000000081111561016157600080fd5b82018360208201111561017357600080fd5b8035906020019184600183028401116401000000008311171561019557600080fd5b5090925090506101fe565b005b610117610208565b6101a0600480360360408110156101c057600080fd5b50803590602001356001600160a01b031661020e565b60008184878760001981018181106101ea57fe5b905060200201350101905095945050505050565b9190910160005550565b60005481565b600191909155600280546001600160a01b0319166001600160a01b0390921691909117905556fea265627a7a72315820fb5adce6faea86ccbf17d1d6df3ca691994c23bac96f786b898d12c85773538764736f6c634300050c0032";

    public static final String FUNC_FOO = "foo";

    public static final String FUNC_FOOFLAG = "fooFlag";

    public static final String FUNC_SETPROPERTIESFORBAR = "setPropertiesForBar";

    public static final String FUNC_UPDATESTATE = "updateState";

    @Deprecated
    protected FooArgsCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected FooArgsCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<BigInteger> foo(List<BigInteger> arg1, byte[] a, String str) {
        final Function function = new Function(FUNC_FOO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(arg1, org.web3j.abi.datatypes.generated.Uint256.class)), 
                new org.web3j.abi.datatypes.generated.Bytes32(a), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] foo_AsSignedCrosschainSubordinateView(List<BigInteger> arg1, byte[] a, String str, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FOO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(arg1, org.web3j.abi.datatypes.generated.Uint256.class)), 
                new org.web3j.abi.datatypes.generated.Bytes32(a), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> fooFlag() {
        final Function function = new Function(FUNC_FOOFLAG, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] fooFlag_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FOOFLAG, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setPropertiesForBar(BigInteger _barChainId, String _barCtrtAddress) {
        final Function function = new Function(
                FUNC_SETPROPERTIESFORBAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_barChainId), 
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setPropertiesForBar_AsSignedCrosschainSubordinateTransaction(BigInteger _barChainId, String _barCtrtAddress, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETPROPERTIESFORBAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_barChainId), 
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setPropertiesForBar_AsCrosschainTransaction(BigInteger _barChainId, String _barCtrtAddress, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETPROPERTIESFORBAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_barChainId), 
                new org.web3j.abi.datatypes.Address(160, _barCtrtAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> updateState(BigInteger magicNum, String str) {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(magicNum), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] updateState_AsSignedCrosschainSubordinateTransaction(BigInteger magicNum, String str, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(magicNum), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> updateState_AsCrosschainTransaction(BigInteger magicNum, String str, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(magicNum), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static FooArgsCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new FooArgsCtrt(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static FooArgsCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new FooArgsCtrt(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<FooArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(FooArgsCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<FooArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(FooArgsCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<FooArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(FooArgsCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<FooArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(FooArgsCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
