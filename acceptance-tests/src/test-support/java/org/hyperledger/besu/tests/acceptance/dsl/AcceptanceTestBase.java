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
package org.hyperledger.besu.tests.acceptance.dsl;

import org.hyperledger.besu.tests.acceptance.dsl.account.Accounts;
import org.hyperledger.besu.tests.acceptance.dsl.blockchain.Blockchain;
import org.hyperledger.besu.tests.acceptance.dsl.condition.admin.AdminConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.clique.CliqueConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.eth.EthConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.ibft2.Ibft2Conditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.login.LoginConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.net.NetConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.perm.PermissioningConditions;
import org.hyperledger.besu.tests.acceptance.dsl.condition.web3.Web3Conditions;
import org.hyperledger.besu.tests.acceptance.dsl.contract.ContractVerifier;
import org.hyperledger.besu.tests.acceptance.dsl.node.cluster.Cluster;
import org.hyperledger.besu.tests.acceptance.dsl.node.configuration.BesuNodeFactory;
import org.hyperledger.besu.tests.acceptance.dsl.node.configuration.permissioning.PermissionedNodeBuilder;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.account.AccountTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.admin.AdminTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.clique.CliqueTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.contract.ContractTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.crosschain.CrossTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.eth.EthTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.ibft2.Ibft2Transactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.miner.MinerTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.net.NetTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.perm.PermissioningTransactions;
import org.hyperledger.besu.tests.acceptance.dsl.transaction.web3.Web3Transactions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

public class AcceptanceTestBase {

  protected final Accounts accounts;
  protected final AccountTransactions accountTransactions;
  protected final AdminConditions admin;
  protected final AdminTransactions adminTransactions;
  protected final Blockchain blockchain;
  protected final CliqueConditions clique;
  protected final CliqueTransactions cliqueTransactions;
  protected final Cluster cluster;
  protected final ContractVerifier contractVerifier;
  protected final ContractTransactions contractTransactions;
  protected final EthConditions eth;
  protected final CrossTransactions crossTransactions;
  protected final EthTransactions ethTransactions;
  protected final Ibft2Transactions ibftTwoTransactions;
  protected final Ibft2Conditions ibftTwo;
  protected final LoginConditions login;
  protected final NetConditions net;
  protected final BesuNodeFactory besu;
  protected final PermissioningConditions perm;
  protected final PermissionedNodeBuilder permissionedNodeBuilder;
  protected final PermissioningTransactions permissioningTransactions;
  protected final MinerTransactions minerTransactions;
  protected final Web3Conditions web3;

  protected AcceptanceTestBase() {
    ethTransactions = new EthTransactions();
    crossTransactions = new CrossTransactions();
    accounts = new Accounts(ethTransactions);
    adminTransactions = new AdminTransactions();
    cliqueTransactions = new CliqueTransactions();
    ibftTwoTransactions = new Ibft2Transactions();
    accountTransactions = new AccountTransactions(accounts);
    permissioningTransactions = new PermissioningTransactions();
    contractTransactions = new ContractTransactions();
    minerTransactions = new MinerTransactions();

    blockchain = new Blockchain(ethTransactions);
    clique = new CliqueConditions(ethTransactions, cliqueTransactions);
    eth = new EthConditions(ethTransactions);
    ibftTwo = new Ibft2Conditions(ibftTwoTransactions);
    login = new LoginConditions();
    net = new NetConditions(new NetTransactions());
    cluster = new Cluster(net);
    perm = new PermissioningConditions(permissioningTransactions);
    admin = new AdminConditions(adminTransactions);
    web3 = new Web3Conditions(new Web3Transactions());
    besu = new BesuNodeFactory();
    contractVerifier = new ContractVerifier(accounts.getPrimaryBenefactor());
    permissionedNodeBuilder = new PermissionedNodeBuilder();
  }

  private static LoggerContext ctx;
  private static XmlConfiguration config;
  private static PatternLayout layout;
  // private static String pattern;

  static {
    System.setProperty("log4j2.isThreadContextMapInheritable", "true");
  }

  @Rule public final TestName name = new TestName();

  @BeforeClass
  public static void classSetUpAcceptanceTestBase() {
    // Store the initial Console Appender's layout
    if (layout == null) {
      ctx = (LoggerContext) LogManager.getContext(false);
      config = (XmlConfiguration) ctx.getConfiguration();
      var originalAppender =
          (ConsoleAppender)
              config.getAppenders().values().stream()
                  .filter(a -> a instanceof ConsoleAppender)
                  .collect(Collectors.toList())
                  .get(0);
      var originalLayout = (PatternLayout) originalAppender.getLayout();
      var pattern = originalLayout.getConversionPattern();
      layout = PatternLayout.newBuilder().withPattern(pattern).build();
    }
  }

  @Before
  public void setUpAcceptanceTestBase() {

    Collection<Appender> old_appenders = config.getAppenders().values();
    Collection<Appender> new_appenders = new ArrayList<Appender>();
    ConsoleAppender appender1 =
        ConsoleAppender.newBuilder()
            .setLayout(layout)
            .setConfiguration(config)
            .setIgnoreExceptions(false)
            .setTarget(Target.SYSTEM_OUT)
            .setFollow(true)
            .setName("stdout")
            .build();
    new_appenders.add(appender1);
    if (Boolean.getBoolean("acctests.savePerTestLogs")) {
      FileAppender appender2 =
          FileAppender.newBuilder()
              .setName("file")
              .withFileName("build/acceptanceTestLogs/" + name.getMethodName() + ".log")
              .withAppend(false)
              .setLayout(layout)
              .setConfiguration(config)
              .build();
      new_appenders.add(appender2);
    }
    for (var a : old_appenders) {
      config.removeAppender(a.getName());
    }
    for (var a : new_appenders) {
      a.start();
      config.addAppender(a);
      config.getRootLogger().addAppender(a, null, null);
    }

    ctx.updateLoggers();
    ThreadContext.put("test", name.getMethodName());
  }

  @After
  public void tearDownAcceptanceTestBase() {
    cluster.close();
  }
}
