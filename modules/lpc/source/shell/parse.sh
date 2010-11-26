#!/bin/bash

JARS=$(echo lib/* | sed -e's/  */:/g' )
java -cp output/ant/classes/:${JARS} us.terebi.lang.lpc.parser.Main "$@"

