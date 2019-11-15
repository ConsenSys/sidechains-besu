/*
 * Copyright 2019 ConsenSys AG.
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
package org.hyperledger.besu.crosschain.core.keys;

import java.util.ArrayList;
import java.util.List;

public class BlockchainCrosschainKeyManager {

  List<BlsThresholdCredentials> credentials = new ArrayList<>();

  public BlsThresholdCredentials getCurrentCredentials() {
    if (this.credentials.isEmpty()) {
      return BlsThresholdCredentials.emptyCredentials();
    }

    return this.credentials.get(this.credentials.size());
  }
}
