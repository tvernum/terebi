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

[ -d ${WORK_DIR} ] || mkdir ${WORK_DIR}

set -x
java -cp ${CP} ${MAIN} ${CONFIG_FILE}
