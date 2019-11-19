package org.hyperledger.besu.crosschain.p2p;

import java.math.BigInteger;

public interface CrosschainPartSecretShareCallback {

  void storePrivateSecretShareCallback(BigInteger nodeId, BigInteger share);


}
