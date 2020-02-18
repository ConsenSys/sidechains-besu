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

import org.hyperledger.besu.crosschain.ethereum.crosschain.CrosschainThreadLocalDataHolder;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Gas;
import org.hyperledger.besu.ethereum.mainnet.AbstractPrecompiledContract;
import org.hyperledger.besu.ethereum.vm.GasCalculator;
import org.hyperledger.besu.ethereum.vm.MessageFrame;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrosschainIsLockedPrecompiledContract extends AbstractPrecompiledContract {
  protected static final Logger LOG = LogManager.getLogger();

  // TODO: Not sure what this should really be
  private static final long FIXED_GAS_COST = 10L;

  public CrosschainIsLockedPrecompiledContract(final GasCalculator gasCalculator) {
    super("CrosschainIsLocked", gasCalculator);
  }

  @Override
  public Gas gasRequirement(final BytesValue input) {
    return Gas.of(FIXED_GAS_COST);
  }

  @Override
  public BytesValue compute(final BytesValue input, final MessageFrame messageFrame) {
    // BigInteger contractAddress = extractParameter(input, Address.SIZE);
    // LOG.info("CrosschainIsLocked Precompile called for address {}", contractAddress);

    boolean result = CrosschainThreadLocalDataHolder.controller.isLocked(Address.wrap(input));

    return toBytes32(BigInteger.valueOf(result ? 1 : 0));
  }
}
