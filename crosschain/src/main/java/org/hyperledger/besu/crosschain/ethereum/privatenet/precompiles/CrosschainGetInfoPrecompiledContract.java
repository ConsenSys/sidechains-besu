/*
 * Copyright 2018 ConsenSys AG.
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
package org.hyperledger.besu.crosschain.ethereum.privatenet.precompiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.crosschain.ethereum.crosschain.CrosschainThreadLocalDataHolder;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Gas;
import org.hyperledger.besu.ethereum.mainnet.AbstractPrecompiledContract;
import org.hyperledger.besu.ethereum.vm.GasCalculator;
import org.hyperledger.besu.ethereum.vm.MessageFrame;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;
import org.hyperledger.besu.util.bytes.BytesValues;
import org.hyperledger.besu.util.uint.UInt256;
import org.hyperledger.besu.util.uint.UInt256Value;

import java.math.BigInteger;
import java.util.Optional;

public class CrosschainGetInfoPrecompiledContract extends AbstractPrecompiledContract {
  protected static final Logger LOG = LogManager.getLogger();

  public static final int GET_INFO_CROSSCHAIN_TRANSACTION_TYPE = 0;
  public static final int GET_INFO_BLOCKCHAIN_ID = 1;
  public static final int GET_INFO_COORDINAITON_BLOCKHCAIN_ID = 2;
  public static final int GET_INFO_COORDINAITON_CONTRACT_ADDRESS = 3;
  public static final int GET_INFO_ORIGINATING_BLOCKCHAIN_ID = 4;
  public static final int GET_INFO_FROM_BLOCKCHAIN_ID = 5;
  public static final int GET_INFO_FROM_CONTRACT_ADDRESS = 6;
  public static final int GET_INFO_CROSSCHAIN_TRANSACTION_ID = 7;


  // TODO: Need to analyse what this should cost.
  private static final long FIXED_GAS_COST = 10L;

  public CrosschainGetInfoPrecompiledContract(final GasCalculator gasCalculator) {
    super("CrosschainGetInfo", gasCalculator);
  }

  @Override
  public Gas gasRequirement(final BytesValue input) {
    return Gas.of(FIXED_GAS_COST);
  }

  @Override
  public BytesValue compute(final BytesValue input, final MessageFrame messageFrame) {
    BigInteger optionBigInt = new BigInteger(input.extractArray());
    //int option = input.getInt(0);
    int option = optionBigInt.intValue();
    LOG.info("CrosschainGetInfo Precompile called with option: {}", option);

    CrosschainTransaction tx = CrosschainThreadLocalDataHolder.getCrosschainTransaction();
    if (tx == null) {
      LOG.error("CrosschainGetInfo called without a crosschain transaction context");
      // Indicate execution failed unexpectedly by returning null.
      return null;
    }

    Optional<BigInteger> maybeId;
    BigInteger id;

    switch (option) {
      case GET_INFO_CROSSCHAIN_TRANSACTION_TYPE:
        // TODO work out more efficient way of doing this. The byte ordering needs to be Bytes32 ordering, not uint256...
        return Bytes32.fromHexString(BigInteger.valueOf(tx.getType().value).toString(16));
      case GET_INFO_BLOCKCHAIN_ID:
        maybeId = tx.getChainId();
        id = maybeId.orElse(BigInteger.ZERO);
        // TODO work out more efficient way of doing this. The byte ordering needs to be Bytes32 ordering, not uint256...
        return Bytes32.fromHexString(id.toString(16));
      case GET_INFO_COORDINAITON_BLOCKHCAIN_ID:
        maybeId = tx.getCrosschainCoordinationBlockchainId();
        id = maybeId.orElse(BigInteger.ZERO);
        return Bytes32.fromHexString(id.toString(16));
      case GET_INFO_COORDINAITON_CONTRACT_ADDRESS:
        return tx.getCrosschainCoordinationContractAddress().orElse(Address.ZERO);
      case GET_INFO_ORIGINATING_BLOCKCHAIN_ID:
        maybeId = tx.getOriginatingSidechainId();
        id = maybeId.orElse(BigInteger.ZERO);
        return Bytes32.fromHexString(id.toString(16));
      case GET_INFO_FROM_BLOCKCHAIN_ID:
        maybeId = tx.getCrosschainFromSidechainId();
        id = maybeId.orElse(BigInteger.ZERO);
        return Bytes32.fromHexString(id.toString(16));
      case GET_INFO_FROM_CONTRACT_ADDRESS:
        return tx.getCrosschainFromAddress().orElse(Address.ZERO);
      case GET_INFO_CROSSCHAIN_TRANSACTION_ID:
        maybeId = tx.getCrosschainTransactionId();
        id = maybeId.orElse(BigInteger.ZERO);
        return Bytes32.fromHexString(id.toString(16));
      default:
        LOG.error("CrosschainGetInfo called unknown option: {}", option);
        return null;
    }
  }
}
