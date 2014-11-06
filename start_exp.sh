#!/bin/bash -e

echo " Cleaning up ... "
fab database clean_db
fab middleware clean_up
fab client clean_up

echo " Starting server ... "
fab middleware start_server

echo " Stating clients ... "
fab client start_client_mixture:f1=6,f2=0,f3=0

#echo " Waiting for experiment to finish ... "
#sleep 2100

#echo " Killing server ... "
#fab middleware kill_all

#echo " Killing clients ... "
#fab client kill_all