if [ $# -ne 1 ]
then
    echo "Usage: $0 <config-file>"
    exit 1
fi

CONFIG="$1"

MAIN="us.terebi.engine.Main"

MODULES="engine net lpc"
DIR=`dirname $CONFIG`
CP=${DIR}

for MODULE in ${MODULES}
do
    CP="${CP}:../${MODULE}/output/eclipse/"
done

for MODULE in ${MODULES}
do
    CP="${CP}:../${MODULE}/source/resource/server/"
done

for MODULE in ${MODULES}
do
    for JAR in ../${MODULE}/lib/*.jar ../${MODULE}/lib/server/*.jar
    do
        if [ -r $JAR ]
        then
            CP="${CP}:${JAR}"
        fi
    done
done

echo "Main = ${MAIN}"
echo "Classpath = ${CP}"
echo "Config = ${CONFIG}"

set -x
java -cp ${CP} ${MAIN} ${CONFIG}
