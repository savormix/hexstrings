FROM openjdk:8-alpine

ADD ./pom.xml /usr/local/hexstrings/pom.xml
WORKDIR /usr/local/hexstrings

ADD ./.mvn ./.mvn
ADD ./mvnw ./mvnw
ADD ./src ./src

RUN chmod +x ./mvnw
RUN ./mvnw -v
