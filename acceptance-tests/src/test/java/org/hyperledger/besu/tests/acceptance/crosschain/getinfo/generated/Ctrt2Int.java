package org.hyperledger.besu.tests.acceptance.crosschain.getinfo.generated;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
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
public class Ctrt2Int extends CrosschainContract {
    private static final String BINARY = "";

    public static final String FUNC_CALLCTRT3 = "callCtrt3";

    @Deprecated
    protected Ctrt2Int(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    protected Ctrt2Int(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
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

    @Deprecated
    public static Ctrt2Int load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Ctrt2Int(contractAddress, besu, crosschainTransactionManager, gasPrice, gasLimit);
    }

    public static Ctrt2Int load(String contractAddress, Besu besu, CrosschainTransactionManager crosschainTransactionManager, ContractGasProvider contractGasProvider) {
        return new Ctrt2Int(contractAddress, besu, crosschainTransactionManager, contractGasProvider);
    }

    public static RemoteCall<Ctrt2Int> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt2Int.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt2Int> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        CrosschainContext crosschainContext = null;
        return deployLockableContractRemoteCall(Ctrt2Int.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }

    public static RemoteCall<Ctrt2Int> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, ContractGasProvider contractGasProvider, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt2Int.class, besu, transactionManager, contractGasProvider, BINARY, "", crosschainContext);
    }

    @Deprecated
    public static RemoteCall<Ctrt2Int> deployLockable(Besu besu, CrosschainTransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, final CrosschainContext crosschainContext) {
        return deployLockableContractRemoteCall(Ctrt2Int.class, besu, transactionManager, gasPrice, gasLimit, BINARY, "", crosschainContext);
    }
}
