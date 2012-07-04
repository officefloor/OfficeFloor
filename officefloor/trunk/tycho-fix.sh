###########################################################
#
#  Tycho is used to build the Eclipse components and is a great tool.
#
#  This script fixes a small issue between Maven and Eclipse dependencies
#  of the build by building the eclipse components multiple times.  Run
#  this script on a new copy from SVN.
#
###########################################################

# Ensure have latest from SVN
echo "Updating from SVN ..."
svn update

# Clear Eclipse target files (to ensure it gets built)
echo "Clearing eclipse target ..."
rm -rf eclipse/target

# Initial build up to Eclipse
echo "Starting build ..."
mvn -DskipTests clean install
LAST_RESULT=$?

# Ensure got up to building Eclipse
if [ ! -d "eclipse/target" ]; then
  echo "ERROR: must build up to Eclipse"
  exit 1
fi

# Loop building the Eclipse components to overcome Maven/Eclipse(Tycho) dependency issues
echo "Fixing Eclipse ..."
while [ $LAST_RESULT != 0 ]
do

  # Should now just rebuild Eclipse until dependencies sort themselves out
  mvn -DskipTests clean install -rf :eclipse
  LAST_RESULT=$?

done

# Flag now complete
echo
echo "Eclipse(Tycho)/Maven dependencies should now be working"
echo