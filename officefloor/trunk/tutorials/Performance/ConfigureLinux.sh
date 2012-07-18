#!/bin/bash

##############################################################
#  Configure Linux to be able to run the high load tests
##############################################################

# Flush all rules from iptables
sudo iptables -F

# For the Server
sysctl -w net.core.somaxconn=32768
sysctl -w net.core.netdev_max_backlog=32768
sysctl -w net.ipv4.tcp_max_syn_backlog=32768
#sysctl -w net.core.message_burst=20000
#sysctl -w net.unix.max_dgram_qlen=20000

# For the CLient
#sysctl -w net.ipv4.tcp_fin_timeout=1000