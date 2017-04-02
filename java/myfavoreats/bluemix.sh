#!/bin/bash
export JAVA_HOME=~/java8
export PATH="$JAVA_HOME/bin:$PATH"
mvn -X -DskipTests -B package -rf :datamodel
