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
package tech.pegasys.pantheon.consensus.ibft.validation;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.TestHelpers;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.Proposal;
import tech.pegasys.pantheon.consensus.ibft.payload.MessageFactory;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FutureRoundProposalMessageValidatorTest {

  private final KeyPair proposerKey = KeyPair.generate();
  private final MessageFactory messageFactoy = new MessageFactory(proposerKey);
  private final ConsensusRoundIdentifier roundIdentifier = new ConsensusRoundIdentifier(1, 1);
  private final Block proposedBlock = TestHelpers.createProposalBlock(emptyList(), roundIdentifier);

  private FutureRoundProposalMessageValidator validator;

  private final MessageValidatorFactory messageValidatorFactory =
      mock(MessageValidatorFactory.class);
  private final MessageValidator messageValidator = mock(MessageValidator.class);

  @Before
  public void setup() {

    when(messageValidatorFactory.createMessageValidator(any(), any())).thenReturn(messageValidator);
    when(messageValidator.validateProposal(any())).thenReturn(true);

    final BlockHeader parentHeader = mock(BlockHeader.class);

    validator =
        new FutureRoundProposalMessageValidator(
            messageValidatorFactory, roundIdentifier.getSequenceNumber(), parentHeader);
  }

  @Test
  public void validProposalMatchingCurrentChainHeightPassesValidation() {
    final Proposal proposal =
        messageFactoy.createProposal(roundIdentifier, proposedBlock, Optional.empty());

    assertThat(validator.validateProposalMessage(proposal)).isTrue();
  }

  @Test
  public void proposalTargettingDifferentChainHeightFailsValidation() {
    final ConsensusRoundIdentifier futureChainIdentifier =
        TestHelpers.createFrom(roundIdentifier, 1, 0);
    final Proposal proposal =
        messageFactoy.createProposal(futureChainIdentifier, proposedBlock, Optional.empty());

    assertThat(validator.validateProposalMessage(proposal)).isFalse();
  }

  @Test
  public void proposalWhichFailsMessageValidationFailsFutureRoundValidation() {
    final Proposal proposal =
        messageFactoy.createProposal(roundIdentifier, proposedBlock, Optional.empty());
    when(messageValidator.validateProposal(any())).thenReturn(false);

    assertThat(validator.validateProposalMessage(proposal)).isFalse();
  }
}
