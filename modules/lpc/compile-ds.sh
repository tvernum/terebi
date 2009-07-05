#!/bin/bash

JARS=$(echo lib/* | sed -e's/  */:/g' )

ROOT="samples/dsIIr8-lib"

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

TMP=/tmp/$(basename $0).$$.text

f_setup()
{
    touch $TMP
}

f_header()
{
    for FILE in $( find /tmp/lpc* \( \! -newer $TMP -prune \) -o \( -type f -name "*.java" -print \) )
    do
        echo " + $FILE"
        ex $FILE << ENDEX
1
i
/* 
 *  This file is a derived work of the Dead Souls II mudlib, which is in the public domain.
 */
.
wq
ENDEX
    done
}

f_cleanup()
{
    rm $TMP
}

f_setup

if [ $# -eq 0 ]
then
    for lpc in $( find ${ROOT}/ -type f -name "*.c" )
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

f_header
f_cleanup

