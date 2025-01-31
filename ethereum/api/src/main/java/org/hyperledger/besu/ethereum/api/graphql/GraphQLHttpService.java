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
package org.hyperledger.besu.ethereum.api.graphql;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

import org.hyperledger.besu.ethereum.api.graphql.internal.response.GraphQLErrorResponse;
import org.hyperledger.besu.ethereum.api.graphql.internal.response.GraphQLJsonRequest;
import org.hyperledger.besu.ethereum.api.graphql.internal.response.GraphQLResponse;
import org.hyperledger.besu.ethereum.api.graphql.internal.response.GraphQLResponseType;
import org.hyperledger.besu.ethereum.api.graphql.internal.response.GraphQLSuccessResponse;
import org.hyperledger.besu.util.NetworkUtility;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.net.MediaType;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphQLHttpService {

  private static final Logger LOG = LogManager.getLogger();

  private static final InetSocketAddress EMPTY_SOCKET_ADDRESS = new InetSocketAddress("0.0.0.0", 0);
  private static final String APPLICATION_JSON = "application/json";
  private static final MediaType MEDIA_TYPE_JUST_JSON = MediaType.JSON_UTF_8.withoutParameters();
  private static final String EMPTY_RESPONSE = "";

  private static final TypeReference<Map<String, Object>> MAP_TYPE =
      new TypeReference<Map<String, Object>>() {};

  private final Vertx vertx;
  private final GraphQLConfiguration config;
  private final Path dataDir;

  private HttpServer httpServer;

  private final GraphQL graphQL;

  private final GraphQLDataFetcherContext dataFetcherContext;

  /**
   * Construct a GraphQLHttpService handler
   *
   * @param vertx The vertx process that will be running this service
   * @param dataDir The data directory where requests can be buffered
   * @param config Configuration for the rpc methods being loaded
   * @param graphQL GraphQL engine
   * @param dataFetcherContext DataFetcherContext required by GraphQL to finish it's job
   */
  public GraphQLHttpService(
      final Vertx vertx,
      final Path dataDir,
      final GraphQLConfiguration config,
      final GraphQL graphQL,
      final GraphQLDataFetcherContext dataFetcherContext) {
    this.dataDir = dataDir;

    validateConfig(config);
    this.config = config;
    this.vertx = vertx;
    this.graphQL = graphQL;
    this.dataFetcherContext = dataFetcherContext;
  }

  private void validateConfig(final GraphQLConfiguration config) {
    checkArgument(
        config.getPort() == 0 || NetworkUtility.isValidPort(config.getPort()),
        "Invalid port configuration.");
    checkArgument(config.getHost() != null, "Required host is not configured.");
  }

  public CompletableFuture<?> start() {
    LOG.info("Starting GraphQL HTTP service on {}:{}", config.getHost(), config.getPort());
    // Create the HTTP server and a router object.
    httpServer =
        vertx.createHttpServer(
            new HttpServerOptions().setHost(config.getHost()).setPort(config.getPort()));

    // Handle graphql http requests
    final Router router = Router.router(vertx);

    // Verify Host header to avoid rebind attack.
    router.route().handler(checkWhitelistHostHeader());

    router
        .route()
        .handler(
            CorsHandler.create(buildCorsRegexFromConfig())
                .allowedHeader("*")
                .allowedHeader("content-type"));
    router
        .route()
        .handler(
            BodyHandler.create()
                .setUploadsDirectory(dataDir.resolve("uploads").toString())
                .setDeleteUploadedFilesOnEnd(true));
    router.route("/").method(GET).handler(this::handleEmptyRequest);
    router
        .route("/graphql")
        .method(GET)
        .method(POST)
        .produces(APPLICATION_JSON)
        .handler(this::handleGraphQLRequest);

    final CompletableFuture<?> resultFuture = new CompletableFuture<>();
    httpServer
        .requestHandler(router)
        .listen(
            res -> {
              if (!res.failed()) {
                resultFuture.complete(null);
                LOG.info(
                    "GraphQL HTTP service started and listening on {}:{}",
                    config.getHost(),
                    httpServer.actualPort());
                return;
              }
              httpServer = null;
              final Throwable cause = res.cause();
              if (cause instanceof SocketException) {
                resultFuture.completeExceptionally(
                    new GraphQLServiceException(
                        String.format(
                            "Failed to bind Ethereum GraphQL HTTP listener to %s:%s: %s",
                            config.getHost(), config.getPort(), cause.getMessage())));
                return;
              }
              resultFuture.completeExceptionally(cause);
            });

    return resultFuture;
  }

  private Handler<RoutingContext> checkWhitelistHostHeader() {
    return event -> {
      final Optional<String> hostHeader = getAndValidateHostHeader(event);
      if (config.getHostsWhitelist().contains("*")
          || (hostHeader.isPresent() && hostIsInWhitelist(hostHeader.get()))) {
        event.next();
      } else {
        event
            .response()
            .setStatusCode(403)
            .putHeader("Content-Type", "application/json; charset=utf-8")
            .end("{\"message\":\"Host not authorized.\"}");
      }
    };
  }

  private Optional<String> getAndValidateHostHeader(final RoutingContext event) {
    final Iterable<String> splitHostHeader = Splitter.on(':').split(event.request().host());
    final long hostPieces = stream(splitHostHeader).count();
    if (hostPieces > 1) {
      // If the host contains a colon, verify the host is correctly formed - host [ ":" port ]
      if (hostPieces > 2 || !Iterables.get(splitHostHeader, 1).matches("\\d{1,5}+")) {
        return Optional.empty();
      }
    }
    return Optional.ofNullable(Iterables.get(splitHostHeader, 0));
  }

  private boolean hostIsInWhitelist(final String hostHeader) {
    return config.getHostsWhitelist().stream()
        .anyMatch(whitelistEntry -> whitelistEntry.toLowerCase().equals(hostHeader.toLowerCase()));
  }

  public CompletableFuture<?> stop() {
    if (httpServer == null) {
      return CompletableFuture.completedFuture(null);
    }

    final CompletableFuture<?> resultFuture = new CompletableFuture<>();
    httpServer.close(
        res -> {
          if (res.failed()) {
            resultFuture.completeExceptionally(res.cause());
          } else {
            httpServer = null;
            resultFuture.complete(null);
          }
        });
    return resultFuture;
  }

  public InetSocketAddress socketAddress() {
    if (httpServer == null) {
      return EMPTY_SOCKET_ADDRESS;
    }
    return new InetSocketAddress(config.getHost(), httpServer.actualPort());
  }

  @VisibleForTesting
  public String url() {
    if (httpServer == null) {
      return "";
    }
    return NetworkUtility.urlForSocketAddress("http", socketAddress());
  }

  // Facilitate remote health-checks in AWS, inter alia.
  private void handleEmptyRequest(final RoutingContext routingContext) {
    routingContext.response().setStatusCode(201).end();
  }

  private void handleGraphQLRequest(final RoutingContext routingContext) {
    try {
      final String query;
      final String operationName;
      final Map<String, Object> variables;
      final HttpServerRequest request = routingContext.request();

      switch (request.method()) {
        case GET:
          query = request.getParam("query");
          operationName = request.getParam("operationName");
          final String variableString = request.getParam("variables");
          if (variableString != null) {
            variables = Json.decodeValue(variableString, MAP_TYPE);
          } else {
            variables = Collections.emptyMap();
          }
          break;
        case POST:
          final String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
          if (contentType != null && MediaType.parse(contentType).is(MEDIA_TYPE_JUST_JSON)) {
            final String requestBody = routingContext.getBodyAsString().trim();
            final GraphQLJsonRequest jsonRequest =
                Json.decodeValue(requestBody, GraphQLJsonRequest.class);
            query = jsonRequest.getQuery();
            operationName = jsonRequest.getOperationName();
            Map<String, Object> jsonVariables = jsonRequest.getVariables();
            if (jsonVariables != null) {
              variables = jsonVariables;
            } else {
              variables = Collections.emptyMap();
            }
          } else {
            // treat all else as application/graphql
            query = routingContext.getBodyAsString().trim();
            operationName = null;
            variables = Collections.emptyMap();
          }
          break;
        default:
          routingContext
              .response()
              .setStatusCode(HttpResponseStatus.METHOD_NOT_ALLOWED.code())
              .end();
          return;
      }

      final HttpServerResponse response = routingContext.response();
      vertx.executeBlocking(
          future -> {
            try {
              final GraphQLResponse graphQLResponse = process(query, operationName, variables);
              future.complete(graphQLResponse);
            } catch (final Exception e) {
              future.fail(e);
            }
          },
          false,
          (res) -> {
            response.putHeader("Content-Type", MediaType.JSON_UTF_8.toString());
            if (res.failed()) {
              response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              response.end(
                  serialise(
                      new GraphQLErrorResponse(
                          Collections.singletonMap(
                              "errors",
                              Collections.singletonList(
                                  Collections.singletonMap(
                                      "message", res.cause().getMessage()))))));
            } else {
              final GraphQLResponse graphQLResponse = (GraphQLResponse) res.result();
              response.setStatusCode(status(graphQLResponse).code());
              response.end(serialise(graphQLResponse));
            }
          });

    } catch (final DecodeException ex) {
      handleGraphQLError(routingContext, ex);
    }
  }

  private HttpResponseStatus status(final GraphQLResponse response) {

    switch (response.getType()) {
      case UNAUTHORIZED:
        return HttpResponseStatus.UNAUTHORIZED;
      case ERROR:
        return HttpResponseStatus.BAD_REQUEST;
      case SUCCESS:
      case NONE:
      default:
        return HttpResponseStatus.OK;
    }
  }

  private String serialise(final GraphQLResponse response) {

    if (response.getType() == GraphQLResponseType.NONE) {
      return EMPTY_RESPONSE;
    }

    return Json.encodePrettily(response.getResult());
  }

  private GraphQLResponse process(
      final String requestJson, final String operationName, final Map<String, Object> variables) {
    final ExecutionInput executionInput =
        ExecutionInput.newExecutionInput()
            .query(requestJson)
            .operationName(operationName)
            .variables(variables)
            .context(dataFetcherContext)
            .build();
    final ExecutionResult result = graphQL.execute(executionInput);
    final Map<String, Object> toSpecificationResult = result.toSpecification();
    final List<GraphQLError> errors = result.getErrors();
    if (errors.size() == 0) {
      return new GraphQLSuccessResponse(toSpecificationResult);
    } else {
      return new GraphQLErrorResponse(toSpecificationResult);
    }
  }

  private void handleGraphQLError(final RoutingContext routingContext, final Exception ex) {
    LOG.debug("Error handling GraphQL request", ex);
    routingContext
        .response()
        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
        .end(Json.encode(new GraphQLErrorResponse(ex.getMessage())));
  }

  private String buildCorsRegexFromConfig() {
    if (config.getCorsAllowedDomains().isEmpty()) {
      return "";
    }
    if (config.getCorsAllowedDomains().contains("*")) {
      return "*";
    } else {
      final StringJoiner stringJoiner = new StringJoiner("|");
      config.getCorsAllowedDomains().stream().filter(s -> !s.isEmpty()).forEach(stringJoiner::add);
      return stringJoiner.toString();
    }
  }
}
