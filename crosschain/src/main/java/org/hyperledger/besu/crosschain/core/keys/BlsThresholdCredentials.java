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

import org.hyperledger.besu.crosschain.crypto.threshold.crypto.BlsPoint;
import org.hyperledger.besu.util.bytes.BytesValue;

/**
 * Holds the Blockchain Public Key, the node's BLS Private Key Share, the threshold and key version.
 * The key version is incremented each time a key generation is started.
 */
public class BlsThresholdCredentials implements BlsThresholdPublicKey {
  public static final long NO_PUBLIC_KEY_SET = 0;
  public static final BytesValue NO_PUBLIC_KEY_SET_BV = BytesValue.EMPTY;

  private long keyVersion;
  private long threshold;
  private BlsPoint publicKey;

  public BlsThresholdCredentials(
      final BlsPoint publicKey, final long keyVersion, final long threshold) {
    this.keyVersion = keyVersion;
    this.threshold = threshold;
    this.publicKey = publicKey;
  }

  public static BlsThresholdCredentials emptyCredentials() {
    return new BlsThresholdCredentials(null, NO_PUBLIC_KEY_SET, NO_PUBLIC_KEY_SET);
  }

  @Override
  public BytesValue getBlockchainPublicKey() {
    if (this.publicKey == null) {
      return NO_PUBLIC_KEY_SET_BV;
    }

    return BytesValue.wrap(this.publicKey.store());
  }

  @Override
  public long getBlockchainPublicKeyVersion() {
    return this.keyVersion;
  }

  @Override
  public long getBlockchainPublicKeyThreshold() {
    return this.threshold;
  }
}
