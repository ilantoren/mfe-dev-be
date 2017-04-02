#!/bin/bash

set -o errexit

mvn  -DskipTests install -rf :parser
pushd rest
mvn install:install-file -Dfile=../parser/target/parser-0.8.jar
mvn deploy:deploy-file  -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true  \
	-DgroupId=com.mfe -DartifactId=parser -Dversion=0.8 -Dfile=../parser/target/parser-0.8.jar

