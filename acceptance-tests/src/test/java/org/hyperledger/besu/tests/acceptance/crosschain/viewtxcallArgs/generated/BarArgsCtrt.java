package org.hyperledger.besu.tests.acceptance.crosschain.viewtxcallArgs.generated;

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
public class BarArgsCtrt extends CrosschainContract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060006003556105b2806100256000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c806336447de71461006757806360d104281461008157806384a13fba146100af578063890eba68146100d3578063cefcc0a9146100db578063fc344e9914610152575b600080fd5b61006f61015a565b60408051918252519081900360200190f35b6100ad6004803603604081101561009757600080fd5b50803590602001356001600160a01b0316610160565b005b6100b7610187565b604080516001600160a01b039092168252519081900360200190f35b61006f610196565b6100ad600480360360408110156100f157600080fd5b8135919081019060408101602082013564010000000081111561011357600080fd5b82018360208201111561012557600080fd5b8035906020019184600183028401116401000000008311171561014757600080fd5b50909250905061019c565b6100ad6102c0565b60005481565b600091909155600180546001600160a01b0319166001600160a01b03909216919091179055565b6001546001600160a01b031681565b60035481565b6002805460018181018355600083815260037f405787fa12a823e0f2b7631cc41b3ba8828b3321ca811111fa75cd3aa3bb5ace9093019290925590549054604051604481018790526060602482019081528454608483018190526102b8956001600160a01b0390941693633f255bbd60e11b9390928a928a928a92918291606481019160a4909101908890801561025257602002820191906000526020600020905b81548152602001906001019080831161023e575b5050838103825284815260200185858082843760008184015260408051601f19601f9093018316909401848103909201845252506020810180516001600160e01b03199a909a166001600160e01b03909a1699909917909852506103b595505050505050565b600355505050565b60408051808201825260058152646d6167696360d81b60208201908152600080546001548551610100602482018181526044830198895287516064840152875191986103b19795966001600160a01b039095169563c0bbc9f160e01b958b958b95608490910192918190849084905b8381101561034757818101518382015260200161032f565b50505050905090810190601f1680156103745780820380516001836020036101000a031916815260200191505b5060408051601f198184030181529190526020810180516001600160e01b03166001600160e01b0319909716969096179095525061049192505050565b5050565b6000606084848460405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561041a578181015183820152602001610402565b50505050905090810190601f1680156104475780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905061047061055f565b602080828486600b600019fa61048557600080fd5b50519695505050505050565b606083838360405160200180848152602001836001600160a01b03166001600160a01b0316815260200180602001828103825283818151815260200191508051906020019080838360005b838110156104f45781810151838201526020016104dc565b50505050905090810190601f1680156105215780820380516001836020036101000a031916815260200191505b50945050505050604051602081830303815290604052905060006004825101905060008082846000600a600019f161055857600080fd5b5050505050565b6040518060200160405280600190602082028038833950919291505056fea265627a7a7231582082dc0844bbd42814a90279894166e492b5a6b7fe6112e50b6b4a7721d799ffd164736f6c634300050c0032";

    public static final String FUNC_BAR = "bar";

    public static final String FUNC_BARUPDATESTATE = "barUpdateState";

    public static final String FUNC_FLAG = "flag";

    public static final String FUNC_FOOCHAINID = "fooChainId";

    public static final String FUNC_FOOCTRT = "fooCtrt";

    public static final String FUNC_SETPROPERTIES = "setProperties";

    @Deprecated
    protected BarArgsCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected BarArgsCtrt(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> bar(byte[] a, String str) {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(a), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] bar_AsSignedCrosschainSubordinateTransaction(byte[] a, String str, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(a), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> bar_AsCrosschainTransaction(byte[] a, String str, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_BAR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(a), 
                new org.web3j.abi.datatypes.Utf8String(str)), 
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

    public RemoteFunctionCall<BigInteger> fooChainId() {
        final Function function = new Function(FUNC_FOOCHAINID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public byte[] fooChainId_AsSignedCrosschainSubordinateView(final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(FUNC_FOOCHAINID, 
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

    public RemoteFunctionCall<TransactionReceipt> setProperties(BigInteger _fooChainId, String _fooCtrtAaddr) {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_fooChainId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public byte[] setProperties_AsSignedCrosschainSubordinateTransaction(BigInteger _fooChainId, String _fooCtrtAaddr, final CrosschainContext crosschainContext) throws IOException {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_fooChainId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedSubordinateTransaction(function, crosschainContext);
    }

    public RemoteFunctionCall<TransactionReceipt> setProperties_AsCrosschainTransaction(BigInteger _fooChainId, String _fooCtrtAaddr, final CrosschainContext crosschainContext) {
        final Function function = new Function(
                FUNC_SETPROPERTIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_fooChainId), 
                new org.web3j.abi.datatypes.Address(160, _fooCtrtAaddr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallCrosschainTransaction(function, crosschainContext);
    }

    @Deprecated
    public static BarArgsCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new BarArgsCtrt(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static BarArgsCtrt load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new BarArgsCtrt(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<BarArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(BarArgsCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<BarArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(BarArgsCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<BarArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(BarArgsCtrt.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<BarArgsCtrt> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(BarArgsCtrt.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
