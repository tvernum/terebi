#!/bin/bash

cd $(dirname $0)/..
ROOT_DIR="${PWD}"
SCRIPTS_DIR="./scripts"
ETC_DIR="./etc"
DRIVER_DIR="./driver"
WORK_DIR="./work"

CONFIG_FILE="${ETC_DIR}/ds.terebi.config"

MAIN="us.terebi.engine.Main"

CP="${ETC_DIR}"
for jar in ${DRIVER_DIR}/*.jar
do
    CP="${CP}:${jar}"
done

echo "Main = ${MAIN}"
echo "Classpath = ${CP}"
echo "Config = ${CONFIG_FILE}"

DEBUG=0
CLEAN=0

while getopts "dcv" option
do
    case $option in
        d)
            DEBUG=1
            ;;
        c)
            CLEAN=1
            ;;
    esac
done
            
[ -d ${WORK_DIR} ] || mkdir ${WORK_DIR}
if [ $CLEAN -eq 1 ]
then
    rm -rf ${WORK_DIR}/*
fi

if [ $DEBUG -eq 1 ]
then
    JAVA_OPTS="-Xshare:off -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=9800,server=y,suspend=n"
else
    JAVA_OPTS=""
fi

JAVA_OPTS="${JAVA_OPTS} -Xmx512M"

set -x
java -cp ${CP} ${JAVA_OPTS} ${MAIN} ${CONFIG_FILE}

