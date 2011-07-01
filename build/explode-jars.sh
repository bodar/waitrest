SPOOL_DIR=$1
WORKING_DIR=$2

cd $WORKING_DIR
for i in $SPOOL_DIR/*.jar ; do
    echo "Inflating $i into $WORKING_DIR"
    jar xvf $i > /dev/null
done


