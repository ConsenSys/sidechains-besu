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
package org.hyperledger.besu.crosschain.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.consensus.common.network.PeerConnectionTracker;
import org.hyperledger.besu.crosschain.messagedata.CrosschainMessageCodes;
import org.hyperledger.besu.crosschain.messagedata.PingMessageData;
import org.hyperledger.besu.crosschain.messagedata.PongMessageData;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.PeerConnection;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Capability;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.Message;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage.DisconnectReason;

import java.util.Arrays;
import java.util.List;

public class CrosschainProtocolManager implements ProtocolManager {
  private static final Logger LOG = LogManager.getLogger();

  private final PeerConnectionTracker peers;

  /**
   * Constructor for the crosschain protocol manager
   *
   * @param peers Used to track all connected IBFT peers.
   */
  public CrosschainProtocolManager(final PeerConnectionTracker peers) {
    this.peers = peers;
  }

  @Override
  public String getSupportedProtocol() {
    return CrosschainSubProtocol.get().getName();
  }

  @Override
  public List<Capability> getSupportedCapabilities() {
    return Arrays.asList(CrosschainSubProtocol.CCH);
  }

  @Override
  public void stop() {}

  @Override
  public void awaitStop() throws InterruptedException {}

  /**
   * This function is called by the P2P framework when an "CCH" message has been received.
   *
   * @param cap The capability under which the message was transmitted.
   * @param message The message to be decoded.
   */
  @Override
  public void processMessage(final Capability cap, final Message message) {
    LOG.info("Received CCH message: cap {}, msg {}", cap.toString(), message.getData().getData());
    int code = message.getData().getCode();
    switch (code){
      case CrosschainMessageCodes.PING:
        try{
          LOG.info("PING!");
          Thread.sleep(5000);
          LOG.info("Sending PONG...");
          message.getConnection().send(CrosschainSubProtocol.CCH, PingMessageData.create());
        } catch (Exception e){
          LOG.error("Exception",e);
        }
        break;
      case CrosschainMessageCodes.PONG:
        try{
          LOG.info("PONG!");
          Thread.sleep(3000);
          LOG.info("Sending PiNG...");
          message.getConnection().send(CrosschainSubProtocol.CCH, PongMessageData.create());
        } catch (Exception e){
          LOG.error("Exception",e);
        }
        break;
      default:
        LOG.error("Received CCH message with unexpected code {}",code);
    }
  }

  @Override
  public void handleNewConnection(final PeerConnection peerConnection) {
    peers.add(peerConnection);
    String ps = peerConnection.getPeerInfo().toString();
    LOG.info("Added new peer: {}", ps);
    try {
      Thread.sleep(3000);
      LOG.info("Pinging peer {}", ps);
      peerConnection.send(CrosschainSubProtocol.CCH, PingMessageData.create());
    } catch (Exception e) {
      LOG.error("Exception",e);
    }
    //todo react to ping with log, small delay and pong. React to pong with log, small delay and ping
  }

  @Override
  public void handleDisconnect(
      final PeerConnection peerConnection,
      final DisconnectReason disconnectReason,
      final boolean initiatedByPeer) {
    String ps = peerConnection.getPeerInfo().toString();
    LOG.info("Disconnected peer {}, reason {}, by peer? {}", ps, disconnectReason.toString(), initiatedByPeer);
    peers.remove(peerConnection);
  }
}
