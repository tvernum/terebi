#!/bin/bash

ASM=$( echo $(dirname $0)/../../lib/compiler/asm* | tr ' ' ':' )

f_process_dis()
{
    declare FILE="$1"
    declare CLASS=$( strings - ${FILE} | head -1)
    declare CP="${FILE%${CLASS}*}"
    [ -z "${CP}" ] && CP="."
    javap -c -private -s -verbose -classpath ${CP} "${CLASS}" > "${2}"
}

f_process_asm()
{
    java -cp ${ASM} org.objectweb.asm.util.ASMifierClassVisitor -debug "${1}" > "${2}"
}

f_process_trace()
{
    java -cp ${ASM} org.objectweb.asm.util.TraceClassVisitor -debug "${1}" > "${2}"
}

f_process_decomp()
{
    echo "Sorry, no decompiler yet..."
}

f_process()
{
    declare FILE="$1"
    declare MODE="$2"
    declare OUT="./$( basename $FILE .class).${MODE}"
    eval f_process_${MODE} "'${FILE}'" "'${OUT}'"
}

MODE=dis

for ARG in "$@"
do
       case $ARG in
        -d)
            MODE="dis"
            ;;
        -a)
            MODE="asm"
            ;;
        -t)
            MODE="trace"
            ;;
        -c)
            MODE="decomp"
            ;;
        *)
            f_process "${ARG}" "${MODE}"
            ;;
        esac
done

