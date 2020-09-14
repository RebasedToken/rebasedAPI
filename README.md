# Rebased Informational API

An API that displays the total and circulating supply of Rebased (v2).

## Prerequisites

maven,jdk 8

## Build

mvn clean install

## Run

java -jar target/rebasedInfo-1.0-SNAPSHOT.jar

## Test endpoints

    curl --request GET 'http://localhost:8080/mcap'
    curl --request GET 'http://localhost:8080/totalsupply'
