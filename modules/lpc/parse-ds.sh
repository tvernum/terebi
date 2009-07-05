#!/bin/bash

JARS=$(echo lib/* | sed -e's/  */:/g' )

ROOT="samples/dsIIr8-lib"

f_parse()
{
    java -cp output/ant/classes/:${JARS} us.terebi.lang.lpc.parser.Main -r${ROOT} -Isecure/include -Iinclude/ -Ilib/include/ -i/ -asecure/include/global.h "${@}"
}

f_root()
{
    typeset FILE="$1"

    if [[ ${FILE} = ${ROOT}/* ]]
    then
        FILE=${FILE#$ROOT}
        if [[ ${FILE} = /* ]]
        then
            FILE=${FILE#/}
        fi
    fi

    echo $FILE
}

if [ $# -eq 0 ]
then
    for lpc in $( find ${ROOT}/ -type f -name "*.c" )
    do
        ERR_FILE=errors/${lpc}
        mkdir  -p $( dirname ${ERR_FILE} )
        
        printf " %s ..." "${lpc}"
        f_parse $(f_root ${lpc}) > /dev/null 2> ${ERR_FILE}
        printf "\n"

        if [ -s $ERR_FILE ]
        then
            echo "## POSSIBLE PARSER FAILURE ${lpc}"
            ls -l ${ERR_FILE}
            # read
        fi

    done
else
    OPTS=""
    for FILE in "$@"
    do
        if [[ $FILE = -* ]]
        then
            OPTS="${OPTS} ${FILE}"
            continue
        fi
        set -x
        f_parse ${OPTS} $(f_root ${FILE} )
        set +x
    done
fi

