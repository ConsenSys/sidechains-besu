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
package org.hyperledger.besu.services.kvstore;

import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorageTransaction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

public class CrosschainNodeStorage {

  private final KeyValueStorage keyValueStorage;

  public CrosschainNodeStorage(final KeyValueStorage keyValueStorage) {
    this.keyValueStorage = keyValueStorage;
  }

  public Optional<String> getIPAddressAndPort(final BigInteger chainId) {
    Optional<byte[]> val = keyValueStorage.get(chainId.toByteArray());
    if (val.isEmpty()) {
      return Optional.empty();
    }
    ByteBuffer buffer = ByteBuffer.wrap(val.get());
    byte[] ippBytes = new byte[val.get().length];
    buffer.get(ippBytes, 0, val.get().length);
    return Optional.of(Base64.getEncoder().encodeToString(ippBytes));
  }

  public Updater updater() {
    return new Updater(keyValueStorage.startTransaction());
  }

  public static class Updater {

    private final KeyValueStorageTransaction transaction;

    public Updater(final KeyValueStorageTransaction transaction) {
      this.transaction = transaction;
    }

    public Updater removeLinkedNode(final BigInteger chainId) {
      transaction.remove(chainId.toByteArray());
      return this;
    }

    public Updater putLinkedNode(final BigInteger blockchainId, final String ipAddressAndPort) {
      transaction.put(
          blockchainId.toByteArray(), ipAddressAndPort.getBytes(Charset.defaultCharset()));
      return this;
    }

    public void commit() {
      transaction.commit();
    }

    public void rollback() {
      transaction.rollback();
    }
  }
}
