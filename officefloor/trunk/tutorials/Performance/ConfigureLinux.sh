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
ifconfig eth0 txqueuelen 20000
sysctl -w net.core.netdev_max_backlog=20000
sysctl -w net.ipv4.tcp_window_scaling=0
sysctl -w net.ipv4.tcp_no_metrics_save=1
sysctl -w net.ipv4.tcp_tw_recycle=1
sysctl -w net.ipv4.tcp_tw_reuse=1
sysctl -w net.ipv4.tcp_fin_timeout=1
sysctl -w net.ipv4.tcp_slow_start_after_idle=0

# For the Server
sysctl -w net.core.somaxconn=20000
sysctl -w net.core.message_burst=20000
sysctl -w net.ipv4.tcp_max_syn_backlog=20000
sysctl -w net.ipv4.tcp_moderate_rcvbuf=0

# For the Client
sysctl -w net.ipv4.tcp_keepalive_intvl=1000000


# Flush routes
sysctl -w net.ipv4.route.flush=1