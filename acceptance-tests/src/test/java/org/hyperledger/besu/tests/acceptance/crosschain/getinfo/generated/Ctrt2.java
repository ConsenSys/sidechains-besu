package org.hyperledger.besu.tests.acceptance.crosschain.getinfo.generated;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
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
public class Ctrt2 extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060006002556103c9806100256000396000f3fe608060405234801561001057600080fd5b50600436106100885760003560e01c806379fe365f1161005b57806379fe365f146100da578063a7ded535146100e2578063e669439b146100ea578063f4ac46931461011057610088565b8063016059251461008d5780633e2ae2f8146100a757806346c9da56146100af57806355443d70146100d0575b600080fd5b61009561012d565b60408051918252519081900360200190f35b610095610133565b6100b7610139565b6040805163ffffffff9092168252519081900360200190f35b6100d8610145565b005b6100956101d8565b6100956101de565b6100d86004803603602081101561010057600080fd5b50356001600160a01b03166101e4565b6100d86004803603602081101561012657600080fd5b5035610206565b60075481565b60045481565b60055463ffffffff1681565b6000546001546040805160048152602481019091526020810180516001600160e01b0316631494356b60e31b17905261018892916001600160a01b03169061020b565b6101906102d9565b60035561019b6102ea565b6005805463ffffffff191663ffffffff929092169190911790556101bd610320565b6006556101c861032c565b6007556101d3610338565b600455565b60065481565b60035481565b600180546001600160a01b0319166001600160a01b0392909216919091179055565b600055565b606083838360405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561026e578181015183820152602001610256565b50505050905090810190601f16801561029b5780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905060008082846000600a600019f16102d257600080fd5b5050505050565b60006102e56001610340565b905090565b600060046102f6610376565b60008152610302610376565b60048082858560fa600019fa61031757600080fd5b50519392505050565b60006102e56002610340565b60006102e56004610340565b60006102e560055b6000602061034c610376565b838152610357610376565b6020808285856078600019fa61036c57600080fd5b5051949350505050565b6040518060200160405280600190602082028038833950919291505056fea265627a7a72315820dbfb271e4f51ca4a7fcaea0698bc5e2bf0ac06079fd3f3d571dbac286922fa0064736f6c634300050c0032";

    public static final String FUNC_CALLCTRT3 = "callCtrt3";

    public static final String FUNC_COORDCHAINID = "coordChainId";

    public static final String FUNC_FROMCHAINID = "fromChainId";

    public static final String FUNC_MYCHAINID = "myChainId";

    public static final String FUNC_MYTXTYPE = "myTxType";

    public static final String FUNC_ORIGCHAINID = "origChainId";

    public static final String FUNC_SETCTRT3 = "setCtrt3";

    public static final String FUNC_SETCTRT3CHAINID = "setCtrt3ChainId";

    @Deprecated
    protected Ctrt2(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected Ctrt2(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> callCtrt3() {
        final Function function = new Function(
                FUNC_CALLCTRT3, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] callCtrt3_AsSignedCrosschainSubordinateTransaction(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_CALLCTRT3, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> callCtrt3_AsCrosschainTransaction(final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_CALLCTRT3, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> coordChainId() {
        final Function function = new Function(FUNC_COORDCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] coordChainId_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_COORDCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> fromChainId() {
        final Function function = new Function(FUNC_FROMCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] fromChainId_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FROMCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> myChainId() {
        final Function function = new Function(FUNC_MYCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] myChainId_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_MYCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> myTxType() {
        final Function function = new Function(FUNC_MYTXTYPE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] myTxType_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_MYTXTYPE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<BigInteger> origChainId() {
        final Function function = new Function(FUNC_ORIGCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] origChainId_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_ORIGCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return createSignedSubordinateView(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt3(String _ctrt3Addr) {
        final Function function = new Function(
                FUNC_SETCTRT3, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt3Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setCtrt3_AsSignedCrosschainSubordinateTransaction(String _ctrt3Addr, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETCTRT3, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt3Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt3_AsCrosschainTransaction(String _ctrt3Addr, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETCTRT3, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt3Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt3ChainId(BigInteger _ctrt3ChainId) {
        final Function function = new Function(
                FUNC_SETCTRT3CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt3ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setCtrt3ChainId_AsSignedCrosschainSubordinateTransaction(BigInteger _ctrt3ChainId, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETCTRT3CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt3ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt3ChainId_AsCrosschainTransaction(BigInteger _ctrt3ChainId, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETCTRT3CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt3ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static Ctrt2 load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Ctrt2(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static Ctrt2 load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new Ctrt2(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<Ctrt2> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt2.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt2> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt2.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<Ctrt2> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt2.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt2> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt2.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
