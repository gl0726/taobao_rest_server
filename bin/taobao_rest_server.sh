#!/bin/bash
NAME=taobao_rest_server
JAR=$NAME-0.0.1-SNAPSHOT.jar
LOG=/datalog/$NAME/$NAME.log

case "$1" in
    start)
        java -jar  -Xms512m -Xmx512m -XX:SurvivorRatio=4 -XX:+UseG1GC -XX:+PrintGCDetails $JAR  > $LOG 2>&1 &
        echo "Started $NAME"
    ;;
    stop)
        PROCESS=`ps -ef|grep ${JAR}|grep -v grep|grep -v PPID|awk '{ print $2}'`
        for i in ${PROCESS}
        do
            echo "终止进程 ${NAME} process id = [ ${i} ]"
            kill -9 ${i}
        done
    ;;
    *)
    #echo "Usage: {start|stop|restart|reload|force-reload|status}"
    echo "Usage: {start|stop}" >&2
    exit 1
    ;;
esac

exit 0