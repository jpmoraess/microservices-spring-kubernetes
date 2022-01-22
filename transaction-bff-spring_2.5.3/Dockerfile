#FROM openjdk:13
#RUN mkdir /tmp/transaction-svc
#ADD . /tmp/transaction-svc
#RUN chmod +x /tmp/transaction-svc/gradlew
#WORKDIR /tmp/transaction-svc
#RUN ls -lsah
#RUN ./gradlew clean build
#RUN mv /tmp/transaction-svc/build/libs/*.jar /tmp/app.jar

FROM adoptopenjdk:13.0.1_9-jre-openj9-0.17.0-bionic
#COPY --from=0 /tmp/app.jar /tmp
ADD build/libs/*.jar /tmp/app.jar
RUN ls /tmp
ENTRYPOINT ["java", "-jar", "/tmp/app.jar"]
EXPOSE 8082
