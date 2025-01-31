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
package org.hyperledger.besu.cli.custom;

import org.hyperledger.besu.ethereum.p2p.peers.EnodeURL;

import java.net.URI;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine.ITypeConverter;

public class EnodeToURIPropertyConverter implements ITypeConverter<URI> {

  private final Function<String, URI> converter;

  EnodeToURIPropertyConverter() {
    this.converter = (s) -> EnodeURL.fromString(s).toURI();
  }

  @VisibleForTesting
  EnodeToURIPropertyConverter(final Function<String, URI> converter) {
    this.converter = converter;
  }

  @Override
  public URI convert(final String value) {
    return converter.apply(value);
  }
}
