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

import org.hyperledger.besu.crosschain.core.CoordContractManager;
import org.hyperledger.besu.crosschain.core.LinkedNodeManager;
import org.hyperledger.besu.crosschain.core.keys.BlsThresholdCredentials;
import org.hyperledger.besu.crosschain.core.keys.CrosschainKeyManager;
import org.hyperledger.besu.crosschain.core.keys.generation.ThresholdKeyGeneration;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorageTransaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

/**
 * This class persists the information of a node that is related to crosschain transactions. There
 * are 3 components to this information and a KeyValueStorage instance is used in the implementation
 * - 1) Linked nodes information - blockchainID and ipAddressAndPort, 2) Coordination Contract
 * information, 3) Key information. Zero key is used for bookkeeping the size information in the
 * KeyValueStorages
 */
public class CrosschainNodeStorage {

  private enum Component {
    LINKED_NODE((byte) 1),
    COORDINATION((byte) 2),
    KEY((byte) 3);

    private final byte[] id;

    Component(final byte... id) {
      this.id = id;
    }

    public byte[] getId() {
      return id;
    }
  }

  private static byte[] longToByteArray(final long x) {
    ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
    buf.putLong(x);
    return buf.array();
  }

  /**
   * This is an empty interface helps collaging all the pieces of data that needs to be persisted.
   */
  private static interface NodeData {}

  /** This class LinkedNodeData maintains information related to linked nodes. */
  private static class LinkedNodeData implements NodeData {
    final BigInteger chainId;
    final String ipAddressAndPort;

    LinkedNodeData(final BigInteger chainId, final String ipAddressAndPort) {
      this.chainId = chainId;
      this.ipAddressAndPort = ipAddressAndPort;
    }

    static byte[] serialize(final BigInteger chainId, final String ipAddressAndPort) {
      byte[] val =
          Bytes.concat(
              Component.LINKED_NODE.getId(),
              longToByteArray(chainId.longValue()),
              ipAddressAndPort.getBytes(Charset.defaultCharset()));
      return val;
    }

    static LinkedNodeData deserialize(final byte[] buf) {
      byte[] chainIdB = new byte[Long.BYTES];
      System.arraycopy(buf, 1, chainIdB, 0, Long.BYTES);
      BigInteger chainId = new BigInteger(chainIdB);
      byte[] ipAddrB = new byte[buf.length - Long.BYTES - 1];
      System.arraycopy(buf, 1 + Long.BYTES, ipAddrB, 0, ipAddrB.length);
      return new LinkedNodeData(chainId, new String(ipAddrB, Charset.defaultCharset()));
    }
  }

  /** This class CoordinationData maintains information related to coordination contracts. */
  private static class CoordinationData implements NodeData {
    public final BigInteger chainId;
    public final String ipAddressAndPort;
    public final Address coordCtrtAddr;

    CoordinationData(
        final BigInteger chainId, final String ipAddressAndPort, final Address coordCtrtAddr) {
      this.chainId = chainId;
      this.ipAddressAndPort = ipAddressAndPort;
      this.coordCtrtAddr = coordCtrtAddr;
    }

    static byte[] serialize(
        final BigInteger chainId, final String ipAddressAndPort, final Address coordCtrtAddr) {
      String ipAddrCtrtAddr = ipAddressAndPort + "#" + coordCtrtAddr.getHexString();
      return Bytes.concat(
          Component.COORDINATION.getId(),
          longToByteArray(chainId.longValue()),
          ipAddrCtrtAddr.getBytes(Charset.defaultCharset()));
    }

    static CoordinationData deserialize(final byte[] buf) {
      byte[] chainIdB = new byte[Long.BYTES];
      System.arraycopy(buf, 1, chainIdB, 0, Long.BYTES);
      BigInteger chainId = new BigInteger(chainIdB);
      byte[] strBuf = new byte[buf.length - Long.BYTES - 1];
      System.arraycopy(buf, 1 + Long.BYTES, strBuf, 0, strBuf.length);
      String[] data = new String(strBuf, Charset.defaultCharset()).split("#", 2);
      Address coordCtrtAddr = Address.fromHexString(data[1]);
      return new CoordinationData(chainId, data[0], coordCtrtAddr);
    }
  }

  /** This class KeyData maintains information related to keys of a crosschain node. */
  private static class KeyData implements NodeData {
    public final Map<Long, ThresholdKeyGeneration> activeKeyGenerations;
    public final Map<Long, BlsThresholdCredentials> credentials;
    public final long activeKeyVersion;

    KeyData(
        final Map<Long, ThresholdKeyGeneration> activeKeyGenerations,
        final Map<Long, BlsThresholdCredentials> credentials,
        final long activeKeyVersion) {
      this.activeKeyGenerations = activeKeyGenerations;
      this.credentials = credentials;
      this.activeKeyVersion = activeKeyVersion;
    }

    static byte[] serialize(
        final Map<Long, ThresholdKeyGeneration> activeKeyGenerations,
        final Map<Long, BlsThresholdCredentials> credentials,
        final long activeKeyVersion) {

      try {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(activeKeyGenerations);
        out.writeObject(credentials);
        byte[] maps = byteOut.toByteArray();

        return Bytes.concat(Component.KEY.getId(), longToByteArray(activeKeyVersion), maps);
      } catch (Exception e) {
        LOG.error("Unexpected exception while serializing crosschain key data: {}", e.toString());
        return null;
      }
    }

    static KeyData deserialize(final byte[] buf) {
      byte[] keyVersionB = new byte[Long.BYTES];
      System.arraycopy(buf, 1, keyVersionB, 0, Long.BYTES);
      long keyVersion = new BigInteger(keyVersionB).longValue();

      byte[] strBuf = new byte[buf.length - Long.BYTES - 1];
      System.arraycopy(buf, 1 + Long.BYTES, strBuf, 0, strBuf.length);

      KeyData keyData = null;

      try {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(strBuf);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        Map<Long, ThresholdKeyGeneration> activeKeyGenerations =
            (Map<Long, ThresholdKeyGeneration>) in.readObject();
        @SuppressWarnings("unchecked")
        Map<Long, BlsThresholdCredentials> credentials =
            (Map<Long, BlsThresholdCredentials>) in.readObject();
        keyData = new KeyData(activeKeyGenerations, credentials, keyVersion);
      } catch (Exception e) {
        LOG.error("Exception while reading from the crosschain node store: {}", e);
      }

      return keyData;
    }
  }

  private static Logger LOG = LogManager.getLogger();
  private long maxKey;
  private Map<BigInteger, NodeData> cache;
  private KeyValueStorage nodeStore;

  public CrosschainNodeStorage(final KeyValueStorage nodeStore) {
    this.nodeStore = nodeStore;
    this.cache = new HashMap<BigInteger, NodeData>();
  }

  /**
   * Restores from the persisted database, the information related to the setup for crosschain
   * transactions.
   *
   * @param linkedNodeManager LinkedNodeManager instance needed to add linked nodes.
   * @param coordContractManager CoordContractManager instance needed to add coordination contract
   *     information.
   */
  public void restoreNodeData(
      final LinkedNodeManager linkedNodeManager,
      final CoordContractManager coordContractManager,
      final CrosschainKeyManager keyManager) {

    OptionalLong size = getSize();

    // If the store is untouched, then explicitly initialize the size to 0.
    if (size.isEmpty()) {
      CrosschainNodeStorage.Updater updater = updater();
      updater.putSize(0);
      updater.commit();
      maxKey = 0;
      return;
    }

    // If the store has some elements, then restore them.
    long num = size.getAsLong();
    long key = 0;
    for (long i = 0; i < num; i++, key++) {
      Optional<byte[]> val = nodeStore.get(longToByteArray(key + 1));
      if (val.isEmpty()) {
        continue;
      } else {
        byte component = val.get()[0];
        if (component == Component.LINKED_NODE.getId()[0]) {
          LinkedNodeData nodeData = LinkedNodeData.deserialize(val.get());
          linkedNodeManager.addNode(nodeData.chainId, nodeData.ipAddressAndPort);
          cache.put(BigInteger.valueOf(key), nodeData);
        } else if (component == Component.COORDINATION.getId()[0]) {
          CoordinationData coordinationData = CoordinationData.deserialize(val.get());
          coordContractManager.addCoordinationContract(
              coordinationData.chainId,
              coordinationData.coordCtrtAddr,
              coordinationData.ipAddressAndPort);
          cache.put(BigInteger.valueOf(key), coordinationData);
        } else if (component == Component.KEY.getId()[0]) {
          KeyData keyData = KeyData.deserialize(val.get());
          keyManager.restore(
              keyData.activeKeyGenerations, keyData.credentials, keyData.activeKeyVersion);
          cache.put(BigInteger.valueOf(key), keyData);
        }
      }
    }
    maxKey = key;
  }

  /**
   * Returns the size of the given store. This function relies on the assumption that key = 0,
   * always stores the size.
   *
   * @return OptionalLong.empty() when the store is empty, otherwise the size.
   */
  private OptionalLong getSize() {
    Optional<byte[]> numElements = nodeStore.get(longToByteArray(0));
    if (numElements.isEmpty()) {
      return OptionalLong.empty();
    }
    ByteBuffer buf = ByteBuffer.wrap(numElements.get());
    return OptionalLong.of(buf.getLong());
  }

  public Updater updater() {
    return new CrosschainNodeStorage.Updater(nodeStore.startTransaction());
  }

  public class Updater {

    private final KeyValueStorageTransaction transaction;

    public Updater(final KeyValueStorageTransaction transaction) {
      this.transaction = transaction;
    }

    /**
     * This function removes the linked node identified by the chainId from the persistent store.
     *
     * @param chainId BlockchainID of the linked node to be removed.
     * @return Updater object used for such removal.
     */
    public Updater removeLinkedNode(final BigInteger chainId) {
      for (Map.Entry<BigInteger, NodeData> entry : CrosschainNodeStorage.this.cache.entrySet()) {
        if (entry.getValue() instanceof LinkedNodeData) {
          LinkedNodeData nodeData = (LinkedNodeData) entry.getValue();
          if (nodeData.chainId.equals(chainId)) {
            transaction.remove(longToByteArray(entry.getKey().longValue()));
            CrosschainNodeStorage.this.cache.remove(entry.getKey());
            if (CrosschainNodeStorage.this.maxKey == entry.getKey().longValue()) {
              CrosschainNodeStorage.this.maxKey--;
            }
            break;
          }
        } else {
          LOG.error(
              "Crosschain Persistence Storage corrupted. Value does not contain LinkedNode Data");
        }
      }
      return this;
    }

    /**
     * This function removes the specified coordination contract linked with the current node.
     *
     * @param chainId BlockchainID identifying the blockchain on which the coordination contract is
     *     deployed.
     * @param coordCtrtAddr Coordination contract's address.
     * @return Updater instance used for such removal
     */
    public Updater removeCoordCtrt(final BigInteger chainId, final Address coordCtrtAddr) {
      for (Map.Entry<BigInteger, NodeData> entry : CrosschainNodeStorage.this.cache.entrySet()) {
        if (entry.getValue() instanceof CoordinationData) {
          CoordinationData coordData = (CoordinationData) entry.getValue();
          if (coordData.chainId.equals(chainId) && coordData.coordCtrtAddr.equals(coordCtrtAddr)) {
            transaction.remove(longToByteArray(entry.getKey().longValue()));
            CrosschainNodeStorage.this.cache.remove(entry.getKey());
            if (CrosschainNodeStorage.this.maxKey == entry.getKey().longValue()) {
              CrosschainNodeStorage.this.maxKey--;
            }
            break;
          } else {
            LOG.error(
                "Crosschain Persistence Storage corrupted. Value does not contain Coordination Data");
          }
        }
      }
      return this;
    }

    /**
     * This method persists the linked node information of the newly linked node.
     *
     * @param blockchainId BlockchainID of the new linked node.
     * @param ipAddressAndPort The IP Address and Port of the new linked node.
     * @return Updater instance used.
     */
    public Updater putLinkedNode(final BigInteger blockchainId, final String ipAddressAndPort) {
      // Increment the maxKey for the purposes of bookkeeping
      CrosschainNodeStorage.this.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(CrosschainNodeStorage.this.maxKey),
          LinkedNodeData.serialize(blockchainId, ipAddressAndPort));

      // Simulate the adding in the cache
      cache.put(
          BigInteger.valueOf(CrosschainNodeStorage.this.maxKey),
          new LinkedNodeData(blockchainId, ipAddressAndPort));

      return incrementSize();
    }

    /**
     * This method persists the coordination contract information of the newly linked coordination
     * contract.
     *
     * @param blockchainId BlockchainID of the blockchain where the coordination contract is
     *     deployed.
     * @param coordCtrtAddr Address of the Coordination contract.
     * @param ipAddressAndPort The IP Address and Port of the new linked node through which
     *     coordination chain can be accessed.
     * @return Updater instance used.
     */
    public Updater putCoordCtrt(
        final BigInteger blockchainId, final Address coordCtrtAddr, final String ipAddressAndPort) {
      // Increment the maxKey for the purposes of bookkeeping
      CrosschainNodeStorage.this.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(CrosschainNodeStorage.this.maxKey),
          CoordinationData.serialize(blockchainId, ipAddressAndPort, coordCtrtAddr));

      // Simulate the adding in the cache
      cache.put(
          BigInteger.valueOf(CrosschainNodeStorage.this.maxKey),
          new CoordinationData(blockchainId, ipAddressAndPort, coordCtrtAddr));

      return incrementSize();
    }

    /**
     * This method persists the key information.
     *
     * @param keyVersion Current active key version.
     * @param activeKeyGenerations Current active key data being used.
     * @param credentials Stored credentials.
     * @return Updater instance used.
     */
    public Updater putKeyData(
        final long keyVersion,
        final Map<Long, ThresholdKeyGeneration> activeKeyGenerations,
        final Map<Long, BlsThresholdCredentials> credentials) {

      // Increment the maxKey for the purposes of bookkeeping
      CrosschainNodeStorage.this.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(CrosschainNodeStorage.this.maxKey),
          KeyData.serialize(activeKeyGenerations, credentials, keyVersion));

      // Simulate the adding in the cache
      cache.put(
          BigInteger.valueOf(CrosschainNodeStorage.this.maxKey),
          new KeyData(activeKeyGenerations, credentials, keyVersion));

      return incrementSize();
    }

    public Updater putSize(final long size) {
      transaction.put(longToByteArray(0), longToByteArray(size));
      return this;
    }

    public Updater incrementSize() {
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

    public void commit() {
      transaction.commit();
    }

    public void rollback() {
      transaction.rollback();
    }
  }
}
