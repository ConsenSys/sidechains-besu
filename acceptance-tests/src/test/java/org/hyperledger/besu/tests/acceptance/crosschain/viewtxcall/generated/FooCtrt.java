package org.hyperledger.besu.tests.acceptance.crosschain.viewtxcall.generated;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
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
public class FooCtrt extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b506000805560ae806100236000396000f3fe6080604052348015600f57600080fd5b5060043610603c5760003560e01c80631d8557d7146041578063c2985578146049578063c8ba4142146061575b600080fd5b60476067565b005b604f606e565b60408051918252519081900360200190f35b604f6073565b6001600055565b600190565b6000548156fea265627a7a7231582071d4b5f514ccaf594e951e5b85384a49fc972b64c9e498196422f17bfce07e4064736f6c634300050c0032";

    public static final String FUNC_FOO = "foo";

    public static final String FUNC_FOOFLAG = "fooFlag";

    public static final String FUNC_UPDATESTATE = "updateState";

    @Deprecated
    protected FooCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected FooCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<BigInteger> foo() {
        final Function function = new Function(FUNC_FOO, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] foo_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FOO, 
                Arrays.<Type>asList(), 
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

    public RemoteFunctionCall<TransactionReceipt> updateState() {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] updateState_AsSignedCrosschainSubordinateTransaction(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> updateState_AsCrosschainTransaction(final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_UPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static FooCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new FooCtrt(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static FooCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new FooCtrt(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<FooCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(FooCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<FooCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(FooCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<FooCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(FooCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<FooCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(FooCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
