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
package org.hyperledger.besu.crosschain.ethereum.privatenet.precompiles;

import org.hyperledger.besu.ethereum.core.CrosschainTransaction;
import org.hyperledger.besu.ethereum.core.Gas;
import org.hyperledger.besu.ethereum.core.MutableAccount;
import org.hyperledger.besu.ethereum.vm.GasCalculator;
import org.hyperledger.besu.ethereum.vm.MessageFrame;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrossChainIsLockedPrecompiledContract extends AbstractCrossChainPrecompiledContract {
  protected static final Logger LOG = LogManager.getLogger();
  private static final int addressLength = 20;

  // TODO: Not sure what this should really be
  private static final long FIXED_GAS_COST = 10L;

  public CrossChainIsLockedPrecompiledContract(final GasCalculator gasCalculator) {
    super("CrosschainIsLocked", gasCalculator);
  }

  @Override
  public Gas gasRequirement(final BytesValue input) {
    return Gas.of(FIXED_GAS_COST);
  }

  @Override
  public BytesValue compute(final BytesValue input, final MessageFrame messageFrame) {
    LOG.info(
        "CrosschainIsLocked Precompile called with " + input.size() + "bytes:" + input.toString());

    BigInteger contractAddress = extractAddress(input, 0, addressLength);
    if (contractAddress.equals(BigInteger.ZERO)) {
      LOG.error("Invalid contract address");
      return (BytesValue.of(0));
    }

    // MutableAccount contract = WorldUpdater.getMutable(contractAddress)null;
    MutableAccount contract = null;
    if (contract.isLocked()) {
      return BytesValue.of(1);
    } else {
      return (BytesValue.of(0));
    }
  }

  @Override
  protected boolean isMatched(final CrosschainTransaction ct) {
    return ct.getType().isOriginatingTransaction() || ct.getType().isSubordinateTransaction();
  }

  private static BigInteger extractAddress(
      final BytesValue input, final int offset, final int length) {
    if (offset > input.size() || length == 0) {
      return BigInteger.ZERO;
    }
    final byte[] raw = Arrays.copyOfRange(input.extractArray(), offset, offset + length);
    return new BigInteger(1, raw);
  }
}
