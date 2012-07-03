###########################################################
#
#  Tycho is used to build the Eclipse components and is a great tool.
#
#  This script fixes a small issue between Maven and Eclipse dependencies
#  of the build by building the eclipse components multiple times.  Run
#  this script on a release jobs from SVN (artifacts already in local 
#  repository from other builds).
#
###########################################################

# Ensure have latest from SVN
svn update

# Start with Eclipse (as other artifacts in local repository)
mvn -DskipTests install -rf :eclipse
LAST_RESULT=$?

# Loop building the Eclipse components to overcome Maven/Eclipse(Tycho) dependency issues
while [ $LAST_RESULT != 0 ]
do

  # Should now just rebuild Eclipse until dependencies sort themselves out
  mvn -DskipTests install -rf :eclipse
  LAST_RESULT=$?

done

# Flag now complete
echo
echo "Eclipse(Tycho)/Maven dependencies should now be working"
echo