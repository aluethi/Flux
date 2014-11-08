#!/bin/bash -e

#
# 30 Minute Trace Experiment
#

f1_def=54
f2_def=18
f3_def=9
msg_def=0
experimentId="30-min-trace"

echo -e "\033[31m Updating client and server ... \033[0m"
fab middleware update_server
fab client update_client

echo -e "\033[31m Cleaning up ... \033[0m"
fab database clean_db
fab middleware clean_up
fab client clean_up

echo -e "\033[31m Starting server ... \033[0m"
fab middleware start_server

echo -e "\033[31m Starting clients ... \033[0m"
fab client start_client_mixture:f1=$f1_def,f2=$f2_def,f3=$f3_def,message_size=$msg_def

echo -e "\033[31m Waiting for experiment to finish ... \033[0m"
sleep 2100

echo -e "\033[31m Killing clients ... \033[0m"
fab client kill_all

echo -e "\033[31m Killing server ... \033[0m"
fab middleware kill_all

echo -e "\033[31m Copying logs ... \033[0m"
fab middleware get_server_logs:experiment_id=$experimentId
fab client get_client_logs:experiment_id=$experimentId

echo -e "\033[31m Pre-processing and concatenating logs ... \033[0m"
fab middleware pre_process_logs:experiment_id=$experimentId
fab client pre_process_logs:experiment_id=$experimentId

echo -e "\033[31m Processing graph ... \033[0m"
./trace.py log/$experimentId/allclients.log