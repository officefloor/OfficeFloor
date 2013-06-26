###########################################################
#
#  Runs the build wrapped with xvfb-run to have graphical tests
#  not use the current display and run in parallel to the user
#  doing other work.
#
###########################################################

xvfb-run mvn $@
