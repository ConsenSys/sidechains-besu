#!/bin/bash

# This script partitions the terminal window using tmux,
# running as many instances of CMD_LINE as params are in PARAMS.
# Press Escape to exit everything.

CMD_LINE="crosschain/run_node.js "
PARAMS="33 22 11"

STANDOUT=`tput smso`
OFFSTANDOUT=`tput rmso`

IS_FIRST=1

for CHAINID in $PARAMS
do
    FULL_CMD_LINE="$CMD_LINE $CHAINID"
    if (( $IS_FIRST == 1))
    then
      IS_FIRST=0
      tmux new-session -s multichain -d "bash -c \"echo $STANDOUT Running '$FULL_CMD_LINE' $OFFSTANDOUT ; $FULL_CMD_LINE\""
      tmux bind-key -n Escape kill-session
      tmux set remain-on-exit on
      tmux set -g mouse on
    else
      tmux split-window -b -t 0 -d "bash -c \"echo $STANDOUT Running '$FULL_CMD_LINE' $OFFSTANDOUT ; $FULL_CMD_LINE\""
    fi
done
tmux select-layout even-vertical
tmux attach
