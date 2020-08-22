# rebasedInfo
rebased info api
## Prereq
maven,jdk 8
## Build
mvn clean install
## Run
java -jar target/rebasedInfo-1.0-SNAPSHOT.jar

## Test endpoints
    curl --request GET 'http://localhost:8080/mcap'
    curl --request GET 'http://localhost:8080/totalsupply'
