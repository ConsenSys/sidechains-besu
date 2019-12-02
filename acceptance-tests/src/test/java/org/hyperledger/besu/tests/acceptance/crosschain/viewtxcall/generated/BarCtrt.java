package org.hyperledger.besu.tests.acceptance.crosschain.viewtxcall.generated;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
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
public class BarCtrt extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b50600060025561039e806100256000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c806360d104281461005c57806384a13fba1461008a578063890eba68146100ae578063fc344e99146100c8578063febb0f7e146100d0575b600080fd5b6100886004803603604081101561007257600080fd5b50803590602001356001600160a01b03166100d8565b005b6100926100ff565b604080516001600160a01b039092168252519081900360200190f35b6100b661010e565b60408051918252519081900360200190f35b610088610114565b610088610159565b600091909155600180546001600160a01b0319166001600160a01b03909216919091179055565b6001546001600160a01b031681565b60025481565b6000546001546040805160048152602481019091526020810180516001600160e01b0316631d8557d760e01b17905261015792916001600160a01b0316906101a1565b565b6000546001546040805160048152602481019091526020810180516001600160e01b03166318530aaf60e31b17905261019c92916001600160a01b03169061026f565b600255565b606083838360405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b838110156102045781810151838201526020016101ec565b50505050905090810190601f1680156102315780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905060008082846000600a600019f161026857600080fd5b5050505050565b6000606084848460405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b838110156102d45781810151838201526020016102bc565b50505050905090810190601f1680156103015780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905061032a61034b565b602080828486600b600019fa61033f57600080fd5b50519695505050505050565b6040518060200160405280600190602082028038833950919291505056fea265627a7a72315820823c01345f086e1495710cc4c4ddc4fed3767da2e5903a658796d0918f2b8e9c64736f6c634300050c0032";

    public static final String FUNC_BAR = "bar";

    public static final String FUNC_BARUPDATESTATE = "barUpdateState";

    public static final String FUNC_FLAG = "flag";

    public static final String FUNC_FOOCTRT = "fooCtrt";

    public static final String FUNC_SETPROPERTIES = "setProperties";

    @Deprecated
    protected BarCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected BarCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> bar() {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] bar_AsSignedCrosschainSubordinateTransaction(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> bar_AsCrosschainTransaction(final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> barUpdateState() {
        final Function function = new Function(
                FUNC_BARUPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] barUpdateState_AsSignedCrosschainSubordinateTransaction(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_BARUPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> barUpdateState_AsCrosschainTransaction(final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_BARUPDATESTATE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> flag() {
        final Function function = new Function(FUNC_FLAG, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] flag_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FLAG, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<String> fooCtrt() {
        final Function function = new Function(FUNC_FOOCTRT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public byte[] fooCtrt_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FOOCTRT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setProperties(BigInteger _calleeId, String _fooCtrtAaddr) {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_calleeId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setProperties_AsSignedCrosschainSubordinateTransaction(BigInteger _calleeId, String _fooCtrtAaddr, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_calleeId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setProperties_AsCrosschainTransaction(BigInteger _calleeId, String _fooCtrtAaddr, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_calleeId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static BarCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new BarCtrt(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static BarCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new BarCtrt(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<BarCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(BarCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<BarCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(BarCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<BarCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(BarCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<BarCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(BarCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
