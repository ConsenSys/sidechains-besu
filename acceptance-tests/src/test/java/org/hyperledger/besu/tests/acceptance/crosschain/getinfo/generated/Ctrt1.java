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
public class Ctrt1 extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b50610399806100206000396000f3fe608060405234801561001057600080fd5b506004361061007d5760003560e01c806379fe365f1161005b57806379fe365f146100dc5780637bf8febd146100e4578063a7ded535146100ec578063d34675aa146100f45761007d565b806301605925146100825780633fba51621461009c57806346c9da56146100bb575b600080fd5b61008a61011a565b60408051918252519081900360200190f35b6100b9600480360360208110156100b257600080fd5b5035610120565b005b6100c3610125565b6040805163ffffffff9092168252519081900360200190f35b61008a610131565b6100b9610137565b61008a6101bf565b6100b96004803603602081101561010a57600080fd5b50356001600160a01b03166101c5565b60055481565b600055565b60035463ffffffff1681565b60045481565b6000546001546040805160048152602481019091526020810180516001600160e01b031663055443d760e41b17905261017a92916001600160a01b0316906101e7565b6101826102b5565b60025561018d6102c6565b6003805463ffffffff191663ffffffff929092169190911790556101af6102fc565b6004556101ba610308565b600555565b60025481565b600180546001600160a01b0319166001600160a01b0392909216919091179055565b606083838360405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561024a578181015183820152602001610232565b50505050905090810190601f1680156102775780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905060008082846000600a600019f16102ae57600080fd5b5050505050565b60006102c16001610310565b905090565b600060046102d2610346565b600081526102de610346565b60048082858560fa600019fa6102f357600080fd5b50519392505050565b60006102c16002610310565b60006102c160045b6000602061031c610346565b838152610327610346565b6020808285856078600019fa61033c57600080fd5b5051949350505050565b6040518060200160405280600190602082028038833950919291505056fea265627a7a72315820fe4242734c4f239b1130839bd80f5c0a9ef585e01670ab8882f8b413dd531dad64736f6c634300050c0032";

    public static final String FUNC_CALLCTRT2 = "callCtrt2";

    public static final String FUNC_COORDCHAINID = "coordChainId";

    public static final String FUNC_MYCHAINID = "myChainId";

    public static final String FUNC_MYTXTYPE = "myTxType";

    public static final String FUNC_ORIGCHAINID = "origChainId";

    public static final String FUNC_SETCTRT2 = "setCtrt2";

    public static final String FUNC_SETCTRT2CHAINID = "setCtrt2ChainId";

    @Deprecated
    protected Ctrt1(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected Ctrt1(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> callCtrt2() {
        final Function function = new Function(
                FUNC_CALLCTRT2, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] callCtrt2_AsSignedCrosschainSubordinateTransaction(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_CALLCTRT2, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> callCtrt2_AsCrosschainTransaction(final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_CALLCTRT2, 
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

    public RemoteFunctionCall<TransactionReceipt> setCtrt2(String _ctrt2Addr) {
        final Function function = new Function(
                FUNC_SETCTRT2, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt2Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setCtrt2_AsSignedCrosschainSubordinateTransaction(String _ctrt2Addr, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETCTRT2, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt2Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt2_AsCrosschainTransaction(String _ctrt2Addr, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETCTRT2, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ctrt2Addr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt2ChainId(BigInteger _ctrt2ChainId) {
        final Function function = new Function(
                FUNC_SETCTRT2CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt2ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setCtrt2ChainId_AsSignedCrosschainSubordinateTransaction(BigInteger _ctrt2ChainId, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETCTRT2CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt2ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setCtrt2ChainId_AsCrosschainTransaction(BigInteger _ctrt2ChainId, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETCTRT2CHAINID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ctrt2ChainId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static Ctrt1 load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Ctrt1(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static Ctrt1 load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new Ctrt1(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<Ctrt1> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt1.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt1> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt1.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<Ctrt1> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt1.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt1> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt1.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
