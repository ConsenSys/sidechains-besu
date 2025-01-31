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
package org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters;

import org.hyperledger.besu.util.uint.UInt256;

import com.fasterxml.jackson.annotation.JsonCreator;

public class UInt256Parameter {

  private final UInt256 value;

  @JsonCreator
  public UInt256Parameter(final String value) {
    this.value = UInt256.fromHexString(value);
  }

  public UInt256 getValue() {
    return value;
  }
}
