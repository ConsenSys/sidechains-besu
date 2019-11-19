package org.hyperledger.besu.crosschain.core.keys;

import java.util.Map;
import java.util.TreeMap;

public class CrosschainKeyManagerStorage {


  public static Map<Long, BlsThresholdCredentials> loadAllCredentials() {
    return new TreeMap<>();
  }

  public static void addCredentials(final BlsThresholdCredentials credentials) {

  }

  public static void switchActiveCredentials(final BlsThresholdCredentials credentials) {

  }


}
