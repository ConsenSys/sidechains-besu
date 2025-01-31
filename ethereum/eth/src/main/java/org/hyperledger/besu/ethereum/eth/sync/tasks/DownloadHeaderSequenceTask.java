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
package org.hyperledger.besu.ethereum.eth.sync.tasks;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;

import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.eth.manager.task.AbstractGetHeadersFromPeerTask;
import org.hyperledger.besu.ethereum.eth.manager.task.AbstractPeerTask.PeerTaskResult;
import org.hyperledger.besu.ethereum.eth.manager.task.AbstractRetryingPeerTask;
import org.hyperledger.besu.ethereum.eth.manager.task.GetHeadersFromPeerByHashTask;
import org.hyperledger.besu.ethereum.eth.sync.ValidationPolicy;
import org.hyperledger.besu.ethereum.eth.sync.tasks.exceptions.InvalidBlockException;
import org.hyperledger.besu.ethereum.mainnet.BlockHeaderValidator;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpec;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage.DisconnectReason;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.primitives.Ints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves a sequence of headers, sending out requests repeatedly until all headers are fulfilled.
 * Validates headers as they are received.
 *
 * @param <C> the consensus algorithm context
 */
public class DownloadHeaderSequenceTask<C> extends AbstractRetryingPeerTask<List<BlockHeader>> {
  private static final Logger LOG = LogManager.getLogger();
  private static final int DEFAULT_RETRIES = 3;

  private final EthContext ethContext;
  private final ProtocolContext<C> protocolContext;
  private final ProtocolSchedule<C> protocolSchedule;

  private final BlockHeader[] headers;
  private final BlockHeader referenceHeader;
  private final int segmentLength;
  private final long startingBlockNumber;
  private final ValidationPolicy validationPolicy;
  private final MetricsSystem metricsSystem;

  private int lastFilledHeaderIndex;

  private DownloadHeaderSequenceTask(
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final BlockHeader referenceHeader,
      final int segmentLength,
      final int maxRetries,
      final ValidationPolicy validationPolicy,
      final MetricsSystem metricsSystem) {
    super(ethContext, maxRetries, Collection::isEmpty, metricsSystem);
    this.protocolSchedule = protocolSchedule;
    this.protocolContext = protocolContext;
    this.ethContext = ethContext;
    this.referenceHeader = referenceHeader;
    this.segmentLength = segmentLength;
    this.validationPolicy = validationPolicy;
    this.metricsSystem = metricsSystem;

    checkArgument(segmentLength > 0, "Segment length must not be 0");
    startingBlockNumber = referenceHeader.getNumber() - segmentLength;
    headers = new BlockHeader[segmentLength];
    lastFilledHeaderIndex = segmentLength;
  }

  public static <C> DownloadHeaderSequenceTask<C> endingAtHeader(
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final BlockHeader referenceHeader,
      final int segmentLength,
      final int maxRetries,
      final ValidationPolicy validationPolicy,
      final MetricsSystem metricsSystem) {
    return new DownloadHeaderSequenceTask<>(
        protocolSchedule,
        protocolContext,
        ethContext,
        referenceHeader,
        segmentLength,
        maxRetries,
        validationPolicy,
        metricsSystem);
  }

  public static <C> DownloadHeaderSequenceTask<C> endingAtHeader(
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final BlockHeader referenceHeader,
      final int segmentLength,
      final ValidationPolicy validationPolicy,
      final MetricsSystem metricsSystem) {
    return new DownloadHeaderSequenceTask<>(
        protocolSchedule,
        protocolContext,
        ethContext,
        referenceHeader,
        segmentLength,
        DEFAULT_RETRIES,
        validationPolicy,
        metricsSystem);
  }

  @Override
  protected CompletableFuture<List<BlockHeader>> executePeerTask(
      final Optional<EthPeer> assignedPeer) {
    LOG.debug(
        "Downloading headers from {} to {}.", startingBlockNumber, referenceHeader.getNumber());
    final CompletableFuture<List<BlockHeader>> task =
        downloadHeaders(assignedPeer).thenCompose(this::processHeaders);
    return task.whenComplete(
        (r, t) -> {
          // We're done if we've filled all requested headers
          if (lastFilledHeaderIndex == 0) {
            LOG.debug(
                "Finished downloading headers from {} to {}.",
                headers[0].getNumber(),
                headers[segmentLength - 1].getNumber());
            result.get().complete(Arrays.asList(headers));
          }
        });
  }

  private CompletableFuture<PeerTaskResult<List<BlockHeader>>> downloadHeaders(
      final Optional<EthPeer> assignedPeer) {
    // Figure out parameters for our headers request
    final boolean partiallyFilled = lastFilledHeaderIndex < segmentLength;
    final BlockHeader referenceHeaderForNextRequest =
        partiallyFilled ? headers[lastFilledHeaderIndex] : referenceHeader;
    final Hash referenceHash = referenceHeaderForNextRequest.getHash();
    final int count = partiallyFilled ? lastFilledHeaderIndex : segmentLength;

    return executeSubTask(
        () -> {
          // Ask for count + 1 because we'll retrieve the previous header as well
          final AbstractGetHeadersFromPeerTask headersTask =
              GetHeadersFromPeerByHashTask.endingAtHash(
                  protocolSchedule,
                  ethContext,
                  referenceHash,
                  referenceHeaderForNextRequest.getNumber(),
                  count + 1,
                  metricsSystem);
          assignedPeer.ifPresent(headersTask::assignPeer);
          return headersTask.run();
        });
  }

  private CompletableFuture<List<BlockHeader>> processHeaders(
      final PeerTaskResult<List<BlockHeader>> headersResult) {
    return executeWorkerSubTask(
        ethContext.getScheduler(),
        () -> {
          final CompletableFuture<List<BlockHeader>> future = new CompletableFuture<>();
          BlockHeader child = null;
          boolean firstSkipped = false;
          final int previousHeaderIndex = lastFilledHeaderIndex;
          for (final BlockHeader header : headersResult.getResult()) {
            final int headerIndex =
                Ints.checkedCast(
                    segmentLength - (referenceHeader.getNumber() - header.getNumber()));
            if (!firstSkipped) {
              // Skip over reference header
              firstSkipped = true;
              continue;
            }
            if (child == null) {
              child =
                  (headerIndex == segmentLength - 1) ? referenceHeader : headers[headerIndex + 1];
            }

            if (!validateHeader(child, header)) {
              // Invalid headers - disconnect from peer
              LOG.debug(
                  "Received invalid headers from peer, disconnecting from: {}",
                  headersResult.getPeer());
              headersResult.getPeer().disconnect(DisconnectReason.BREACH_OF_PROTOCOL);
              future.completeExceptionally(
                  new InvalidBlockException(
                      "Header failed validation.", child.getNumber(), child.getHash()));
              return future;
            }
            headers[headerIndex] = header;
            lastFilledHeaderIndex = headerIndex;
            child = header;
          }
          future.complete(asList(headers).subList(lastFilledHeaderIndex, previousHeaderIndex));
          return future;
        });
  }

  private boolean validateHeader(final BlockHeader child, final BlockHeader header) {
    final long finalBlockNumber = startingBlockNumber + segmentLength;
    final boolean blockInRange =
        header.getNumber() >= startingBlockNumber && header.getNumber() < finalBlockNumber;
    if (!blockInRange) {
      return false;
    }
    if (child == null) {
      return false;
    }

    final ProtocolSpec<C> protocolSpec = protocolSchedule.getByBlockNumber(child.getNumber());
    final BlockHeaderValidator<C> blockHeaderValidator = protocolSpec.getBlockHeaderValidator();
    return blockHeaderValidator.validateHeader(
        child, header, protocolContext, validationPolicy.getValidationModeForNextBlock());
  }
}
