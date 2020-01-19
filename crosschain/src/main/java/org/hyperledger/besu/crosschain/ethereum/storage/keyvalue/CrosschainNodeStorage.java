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
import org.hyperledger.besu.ethereum.core.Address;
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

/**
 * This class persists the information of a node that is related to crosschain transactions. There
 * are 3 components to this information and 3 separate KeyValueStorage instances are used in the
 * implementation - 1) Linked nodes information - blockchainID and ipAddressAndPort, 2) Coordination
 * Contract information, 3) Key information. Zero key is used for bookkeeping the size information
 * in the KeyValueStorages
 */
public class CrosschainNodeStorage {

  /** This nested class LinkedNodeData maintains information related to linked nodes. */
  public static class LinkedNodeData {
    public BigInteger chainId;
    public String ipAddressAndPort;
    public static long maxKey;
    public static Map<BigInteger, LinkedNodeData> cache;
    public static KeyValueStorage linkedNodesKV;

    LinkedNodeData(final BigInteger chainId, final String ipAddressAndPort) {
      this.chainId = chainId;
      this.ipAddressAndPort = ipAddressAndPort;
    }
  }

  /** This nested class CoordinationData maintains information related to coordination contracts. */
  public static class CoordinationData {
    public BigInteger chainId;
    public String ipAddressAndPort;
    public Address coordCtrtAddr;
    public static long maxKey;
    public static Map<BigInteger, CoordinationData> cache;
    public static KeyValueStorage coordinationKV;

    CoordinationData(
        final BigInteger chainId, final String ipAddressAndPort, final Address coordCtrtAddr) {
      this.chainId = chainId;
      this.ipAddressAndPort = ipAddressAndPort;
      this.coordCtrtAddr = coordCtrtAddr;
    }
  }

  private static Logger LOG = LogManager.getLogger();

  public CrosschainNodeStorage(
      final KeyValueStorage linkedNodesKV, final KeyValueStorage coordinationKV) {
    LinkedNodeData.linkedNodesKV = linkedNodesKV;
    LinkedNodeData.cache = new HashMap<BigInteger, LinkedNodeData>();
    CoordinationData.coordinationKV = coordinationKV;
    CoordinationData.cache = new HashMap<BigInteger, CoordinationData>();
  }

  private static byte[] longToByteArray(final long x) {
    ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
    buf.putLong(x);
    return buf.array();
  }

  private static byte[] serializeLinkedNodeData(
      final BigInteger chainId, final String ipAddressAndPort) {
    byte[] chainIdB = longToByteArray(chainId.longValue());
    byte[] ipAddressAndPortB = ipAddressAndPort.getBytes(Charset.defaultCharset());
    return Bytes.concat(chainIdB, ipAddressAndPortB);
  }

  private static byte[] serializeCoordinationData(
      final BigInteger chainId, final String ipAddressAndPort, final Address coordCtrtAddr) {
    byte[] chainIdB = longToByteArray(chainId.longValue());
    byte[] ipAddressAndPortB = ipAddressAndPort.getBytes(Charset.defaultCharset());
    byte[] coordCtrtAddrB =
        Bytes.concat("#".getBytes(Charset.defaultCharset()), coordCtrtAddr.getByteArray());
    return Bytes.concat(Bytes.concat(chainIdB, ipAddressAndPortB), coordCtrtAddrB);
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
      final LinkedNodeManager linkedNodeManager, final CoordContractManager coordContractManager) {
    restoreLinkedNodes(linkedNodeManager);
    restoreCoordinationData(coordContractManager);
  }

  private void restoreLinkedNodes(final LinkedNodeManager linkedNodeManager) {
    KeyValueStorage store = LinkedNodeData.linkedNodesKV;
    OptionalLong size = getSize(store);
    if (size.isEmpty()) {
      CrosschainNodeStorage.Updater updater = updater(store);
      updater.putSize(0);
      updater.commit();
      LinkedNodeData.maxKey = 0;
      return;
    }

    long num = size.getAsLong();
    long key = 0;
    for (long i = 0; i < num; i++, key++) {
      Optional<byte[]> val = store.get(longToByteArray(key + 1));
      if (val.isEmpty()) {
        continue;
      } else {
        ByteBuffer buf = ByteBuffer.wrap(val.get());
        LinkedNodeData nodeData =
            new LinkedNodeData(
                BigInteger.valueOf(buf.getLong()),
                new String(buf.array(), Charset.defaultCharset()));
        linkedNodeManager.addNode(nodeData.chainId, nodeData.ipAddressAndPort);
        LinkedNodeData.cache.put(BigInteger.valueOf(key), nodeData);
      }
    }
    LinkedNodeData.maxKey = key;
  }

  private void restoreCoordinationData(final CoordContractManager coordContractManager) {
    KeyValueStorage store = CoordinationData.coordinationKV;
    OptionalLong size = getSize(store);
    if (size.isEmpty()) {
      CrosschainNodeStorage.Updater updater = updater(store);
      updater.putSize(0);
      updater.commit();
      CoordinationData.maxKey = 0;
      return;
    }

    long num = size.getAsLong();
    long key = 0;
    for (long i = 0; i < num; i++, key++) {
      Optional<byte[]> val = store.get(longToByteArray(key + 1));
      if (val.isEmpty()) {
        continue;
      } else {
        ByteBuffer buf = ByteBuffer.wrap(val.get());
        BigInteger chainId = BigInteger.valueOf(buf.getLong());
        String ipAndCtrt = new String(buf.array(), Charset.defaultCharset());
        String[] str = ipAndCtrt.split("#", 2);
        CoordinationData coordinationData =
            new CoordinationData(chainId, str[0], Address.fromHexString(str[1]));
        coordContractManager.addCoordinationContract(
            coordinationData.chainId,
            coordinationData.coordCtrtAddr,
            coordinationData.ipAddressAndPort);
        CoordinationData.cache.put(BigInteger.valueOf(key), coordinationData);
      }
    }
    CoordinationData.maxKey = key;
  }

  /**
   * Returns the size of the given store. This function relies on the assumption that key = 0,
   * always stores the size.
   *
   * @param store
   * @return OptionalLong.empty() when the store is empty, otherwise the size.
   */
  private OptionalLong getSize(final KeyValueStorage store) {
    Optional<byte[]> numElements = store.get(longToByteArray(0));
    if (numElements.isEmpty()) {
      return OptionalLong.empty();
    }
    ByteBuffer buf = ByteBuffer.wrap(numElements.get());
    return OptionalLong.of(buf.getLong());
  }

  public Updater updater(final KeyValueStorage store) {
    return new CrosschainNodeStorage.Updater(store.startTransaction());
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
      for (Map.Entry<BigInteger, LinkedNodeData> entry :
          CrosschainNodeStorage.LinkedNodeData.cache.entrySet()) {
        if (entry.getValue().chainId.equals(chainId)) {
          transaction.remove(longToByteArray(entry.getKey().longValue()));
          CrosschainNodeStorage.LinkedNodeData.cache.remove(entry.getKey());
          if (CrosschainNodeStorage.LinkedNodeData.maxKey == entry.getKey().longValue()) {
            CrosschainNodeStorage.LinkedNodeData.maxKey--;
          }
          return this;
        }
      }
      LOG.error("This error should have been caught earlier. This chainId is not linked.");
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
      for (Map.Entry<BigInteger, CoordinationData> entry :
          CrosschainNodeStorage.CoordinationData.cache.entrySet()) {
        if (entry.getValue().chainId.equals(chainId)
            && entry.getValue().coordCtrtAddr.equals(coordCtrtAddr)) {
          transaction.remove(longToByteArray(entry.getKey().longValue()));
          CrosschainNodeStorage.CoordinationData.cache.remove(entry.getKey());
          if (CrosschainNodeStorage.CoordinationData.maxKey == entry.getKey().longValue()) {
            CrosschainNodeStorage.CoordinationData.maxKey--;
          }
          return this;
        }
      }
      LOG.error(
          "This error should have been caught earlier. No coordination contract at this chainId and address.");
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
      CrosschainNodeStorage.LinkedNodeData.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(CrosschainNodeStorage.LinkedNodeData.maxKey),
          serializeLinkedNodeData(blockchainId, ipAddressAndPort));

      // Simulate the adding in the cache
      LinkedNodeData nodeData = new LinkedNodeData(blockchainId, ipAddressAndPort);
      LinkedNodeData.cache.put(
          BigInteger.valueOf(CrosschainNodeStorage.LinkedNodeData.maxKey), nodeData);

      // Update the number of elements in the keyValueStorage
      OptionalLong size = CrosschainNodeStorage.this.getSize(LinkedNodeData.linkedNodesKV);
      long numElements = 0;
      if (!size.isEmpty()) {
        transaction.remove(longToByteArray(0));
        numElements = size.getAsLong();
      }
      this.putSize(numElements + 1);

      return this;
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
      CrosschainNodeStorage.CoordinationData.maxKey++;

      // Add the element to the transaction
      transaction.put(
          longToByteArray(CrosschainNodeStorage.LinkedNodeData.maxKey),
          serializeCoordinationData(blockchainId, ipAddressAndPort, coordCtrtAddr));

      // Simulate the adding in the cache
      CoordinationData nodeData =
          new CoordinationData(blockchainId, ipAddressAndPort, coordCtrtAddr);
      CoordinationData.cache.put(
          BigInteger.valueOf(CrosschainNodeStorage.CoordinationData.maxKey), nodeData);

      // Update the number of elements in the keyValueStorage
      OptionalLong size = CrosschainNodeStorage.this.getSize(CoordinationData.coordinationKV);
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
