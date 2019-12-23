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

pragma solidity >=0.4.0 <0.6.0;
import "../common/Crosschain.sol";
import "./Ctrt3Int.sol";

contract Ctrt3 is Crosschain, Ctrt3Int {
    uint256 public myChainId;
    uint256 public fromChainId;
    uint256 public myTxType;
    uint256 public consTxType;
    uint256 public coordChainId;
    uint256 public origChainId;
    uint256 public txId;
    address public coordCtrtAddr;
    address public fromAddr;

    constructor () public {
        consTxType = crosschainGetInfoTransactionType();
    }

    function txfn() external {
        myChainId = crosschainGetInfoBlockchainId();
        myTxType = crosschainGetInfoTransactionType();
        coordChainId = crosschainGetInfoCoordinationBlockchainId();
        origChainId = crosschainGetInfoOriginatingBlockchainId();
        fromChainId = crosschainGetInfoFromBlockchainId();
        txId = crosschainGetInfoCrosschainTransactionId();
        coordCtrtAddr = crosschainGetInfoCoordinationContractAddress();
        fromAddr = crosschainGetInfoFromAddress();
    }
}