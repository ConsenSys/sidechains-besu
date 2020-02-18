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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.ethereum.core.Gas;
import org.hyperledger.besu.ethereum.vm.GasCalculator;
import org.hyperledger.besu.ethereum.vm.MessageFrame;
import org.hyperledger.besu.util.bytes.Bytes32;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Skeleton class for @{link PrecompileContract} implementations. */
public abstract class AbstractPrecompiledContract implements PrecompiledContract {

  protected static Logger LOG = LogManager.getLogger();

  private final GasCalculator gasCalculator;

  private final String name;

  protected int offset;

  public AbstractPrecompiledContract(final String name, final GasCalculator gasCalculator) {
    this.name = name;
    this.gasCalculator = gasCalculator;
  }

  protected static BigInteger extractParameter(
      final BytesValue input, final int offset, final int length) {
    if (offset > input.size() || length == 0) {
      return BigInteger.ZERO;
    }
    final byte[] raw = Arrays.copyOfRange(input.extractArray(), offset, offset + length);
    return new BigInteger(1, raw);
  }

  protected static Bytes32 toBytes32(final BigInteger val) {
    byte[] bytes = val.toByteArray();
    Bytes32 retval;
    if (bytes.length <= Bytes32.SIZE) {
      retval = Bytes32.leftPad(BytesValue.wrap(bytes));
    } else if ((bytes.length == Bytes32.SIZE + 1) && (bytes[Bytes32.SIZE] == 0)) {
      retval = Bytes32.wrap(bytes, Bytes32.SIZE - bytes.length);
    } else {
      String errorMessage = "Value too large to convert to Bytes32. Actual length: " + bytes.length;
      LOG.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
    return retval;
  }

  /**
   * Extracts a parameter from the input, starting from the currently accumulated offset
   *
   * @param input RLP input
   * @param length encoded size of the parameter being extracted
   * @return the extracted parameter
   */
  protected BigInteger extractParameter(final BytesValue input, final int length) {
    BigInteger result = extractParameter(input, this.offset, length);
    this.offset += length;
    return result;
  }

  protected static BigInteger extractParameter(
      final BytesValue input, final BigInteger offset, final int length) {
    if (BigInteger.valueOf(input.size()).compareTo(offset) <= 0) {
      return BigInteger.ZERO;
    }
    return extractParameter(input, offset.intValue(), length);
  }

  protected GasCalculator gasCalculator() {
    return gasCalculator;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public abstract Gas gasRequirement(BytesValue input);

  @Override
  public abstract BytesValue compute(BytesValue input, MessageFrame messageFrame);
}
