#!/bin/bash

##############################################################
#  Configure Linux to be able to run the high load tests
##############################################################

# Flush all rules from iptables
sudo iptables -F

# For both Server/Client
sysctl -w fs.file-max=1048576

# For the Server
sysctl -w net.core.somaxconn=32768
sysctl -w net.core.message_burst=32768
sysctl -w net.core.netdev_max_backlog=32768
sysctl -w net.ipv4.tcp_max_syn_backlog=32768

# For the Client
sysctl -w net.ipv4.tcp_syn_retries=100
sysctl -w net.ipv4.tcp_keepalive_intvl=1000000
sysctl -w net.ipv4.tcp_tw_recycle=1
sysctl -w net.ipv4.tcp_tw_reuse=1
sysctl -w net.ipv4.tcp_fin_timeout=2
