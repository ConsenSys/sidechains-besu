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
package org.hyperledger.besu.crosschain.ethereum.storage.keyvalue;

import org.hyperledger.besu.crosschain.core.LinkedNodeManager;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorageTransaction;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import com.google.common.primitives.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrosschainNodeStorage {

  private static class NodeData {
    public BigInteger chainId;
    public String ipAddressAndPort;

    NodeData(final BigInteger chainId, final String ipAddressAndPort) {
      this.chainId = chainId;
      this.ipAddressAndPort = ipAddressAndPort;
    }
  }

  private static Logger LOG = LogManager.getLogger();
  private Map<BigInteger, NodeData> cache;
  private final KeyValueStorage keyValueStorage;
  private long maxKey;

  public CrosschainNodeStorage(final KeyValueStorage keyValueStorage) {
    this.keyValueStorage = keyValueStorage;
    // zero key is reserved for obtaining the size
    cache = new HashMap<BigInteger, NodeData>();
  }

  private static byte[] longToByteArray(final long x) {
    ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
    buf.putLong(x);
    return buf.array();
  }

  private static byte[] concatToKeyStorageValue(
      final BigInteger chainId, final String ipAddressAndPort) {
    byte[] chainIdB = longToByteArray(chainId.longValue());
    byte[] ipAddressAndPortB = ipAddressAndPort.getBytes(Charset.defaultCharset());
    return Bytes.concat(chainIdB, ipAddressAndPortB);
  }

  public void restoreLinkedNodes(final LinkedNodeManager linkedNodeManager) {
    OptionalLong size = getSize();
    if (size.isEmpty()) {
      CrosschainNodeStorage.Updater updater = updater();
      updater.putSize(0);
      updater.commit();
      maxKey = 0;
      return;
    }

    long num = size.getAsLong();
    long key = 0;
    for (long i = 0; i < num; i++, key++) {
      Optional<byte[]> val = keyValueStorage.get(longToByteArray(key + 1));
      if (val.isEmpty()) {
        continue;
      } else {
        ByteBuffer buf = ByteBuffer.wrap(val.get());
        NodeData nodeData =
            new NodeData(
                BigInteger.valueOf(buf.getLong()),
                new String(buf.array(), Charset.defaultCharset()));
        linkedNodeManager.addNode(nodeData.chainId, nodeData.ipAddressAndPort);
        cache.put(BigInteger.valueOf(key), nodeData);
      }
    }
    maxKey = key;
  }

  public OptionalLong getSize() {
    Optional<byte[]> numElements = keyValueStorage.get(longToByteArray(0));
    if (numElements.isEmpty()) {
      return OptionalLong.empty();
    }
    ByteBuffer buf = ByteBuffer.wrap(numElements.get());
    return OptionalLong.of(buf.getLong());
  }

  public Updater updater() {
    return new CrosschainNodeStorage.Updater(keyValueStorage.startTransaction());
  }

  public class Updater {

    private final KeyValueStorageTransaction transaction;

    public Updater(final KeyValueStorageTransaction transaction) {
      this.transaction = transaction;
    }

    public Updater removeLinkedNode(final BigInteger chainId) {
      for (Map.Entry<BigInteger, NodeData> entry : CrosschainNodeStorage.this.cache.entrySet()) {
        if (entry.getValue().chainId.equals(chainId)) {
          transaction.remove(longToByteArray(entry.getKey().longValue()));
          CrosschainNodeStorage.this.cache.remove(entry.getKey());
          if (CrosschainNodeStorage.this.maxKey == entry.getKey().longValue()) {
            CrosschainNodeStorage.this.maxKey--;
          }
          return this;
        }
      }
      LOG.info("This error should have been caught earlier. This chainId is not linked.");
      return this;
    }

    public Updater putLinkedNode(final BigInteger blockchainId, final String ipAddressAndPort) {
      // Increment the maxKey for the purposes of bookkeeping
      CrosschainNodeStorage.this.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(maxKey), concatToKeyStorageValue(blockchainId, ipAddressAndPort));

      // Simulate the adding in the cache
      NodeData nodeData = new NodeData(blockchainId, ipAddressAndPort);
      cache.put(BigInteger.valueOf(maxKey), nodeData);

      // Update the number of elements in the keyValueStorage
      OptionalLong size = CrosschainNodeStorage.this.getSize();
      long numElements = 0;
      if (!size.isEmpty()) {
        transaction.remove(longToByteArray(0));
        numElements = size.getAsLong();
      }
      this.putSize(numElements + 1);

      return this;
    }

    public Updater putSize(final long size) {
      transaction.put(longToByteArray(0), longToByteArray(size));
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
