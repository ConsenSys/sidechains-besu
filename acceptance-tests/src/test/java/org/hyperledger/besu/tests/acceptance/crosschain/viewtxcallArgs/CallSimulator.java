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
package org.hyperledger.besu.tests.acceptance.crosschain.viewtxcallArgs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

class CallSimulator {
  List<BigInteger> arg;
  BigInteger magicNum;
  BigInteger fooFlag;
  String str;

  void bar() {
    arg = new ArrayList<BigInteger>();
    arg.add(BigInteger.valueOf(3));
    System.out.println("ARG is " + arg + " Length is " + arg.size());
    foo(arg);
  }

  BigInteger foo(final List<BigInteger> args1) {
    return args1.get((args1.size() - 1));
  }

  void barUpdateState() {
    BigInteger num = BigInteger.valueOf(256);
    magicNum = num;
    str = "magic";
    updateState(num, str);
  }

  void updateState(final BigInteger num, final String str) {
    fooFlag = BigInteger.valueOf(num.longValue() + str.length());
  }
}
