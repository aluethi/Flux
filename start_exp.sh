#!/bin/bash -e

echo -e "\033[31m Updating client and server ... \033[0m"
fab middleware update_server
fab client update_client

echo "\033[31m Cleaning up ... \033[0m"
fab database clean_db
fab middleware clean_up
fab client clean_up

echo "\033[31m Starting server ... \033[0m"
fab middleware start_server

echo "\033[31m Stating clients ... \033[0m"
fab client start_client_mixture:f1=18,f2=6,f3=3

echo "\033[31m Waiting for experiment to finish ... \033[0m"
sleep 2100

echo "\033[31m Killing server ... \033[0m"
fab middleware kill_all

echo "\033[31m Killing clients ... \033[0m"
fab client kill_all