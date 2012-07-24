#!/bin/bash

##############################################################
#  Configure Linux to be able to run the high load tests.
#
#  Configuration is temporary and should reset on a restart.
#
##############################################################

# fail on error
set -e

# Flush all rules from iptables
iptables -F

# For both Server/Client
sysctl -w vm.swappiness=10
ifconfig eth0 txqueuelen 10000

# For the Server
sysctl -w net.core.somaxconn=4096
sysctl -w net.core.message_burst=4096
sysctl -w net.core.netdev_max_backlog=4096
sysctl -w net.ipv4.tcp_max_syn_backlog=4096

# For the Client
# sysctl -w net.ipv4.tcp_syn_retries=100
sysctl -w net.ipv4.tcp_keepalive_intvl=1000000
sysctl -w net.ipv4.tcp_tw_recycle=1
sysctl -w net.ipv4.tcp_tw_reuse=1
sysctl -w net.ipv4.tcp_fin_timeout=2
