#!/bin/bash

JARS=$(echo lib/* | sed -e's/  */:/g' )

ROOT="samples/ds-2.8.4-lib"

f_compile()
{
    java -cp output/eclipse:output/ant/classes/:source/resource/server:source/resource/test:${JARS} \
          us.terebi.lang.lpc.compiler.java.L2JCompiler -r${ROOT} \
          -Isecure/include -Iinclude/ -Ilib/include/ -i/ -asecure/include/global.h \
          -DMUD_NAME='"Nona Me"' -D__PORT__=7777 -D__LARGEST_PRINTABLE_STRING__=9999 -D__ARCH__='"'"$(uname)"'"' \
         "${@}"
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
    for lpc in $( find samples/ds-2.8.4-lib/ -type f -name "*.c" )
    do
        ERR_FILE=errors/compile/${lpc}
        mkdir  -p $( dirname ${ERR_FILE} )
        
        printf " %s ..." "${lpc}"
        f_compile $(f_root ${lpc}) > /dev/null 2> ${ERR_FILE}
        printf "\n"

        if [ -s $ERR_FILE ]
        then
            echo "## POSSIBLE COMPILER FAILURE ${lpc}"
            ls -l ${ERR_FILE}
            # read
        fi

    done
else
    OPTS=""
    FILES=""
    for FILE in "$@"
    do
        if [[ $FILE = -* ]]
        then
            OPTS="${OPTS} ${FILE}"
        else
            FILES="${FILES} $(f_root ${FILE})"
        fi
    done
    f_compile ${OPTS} ${FILES}
fi

