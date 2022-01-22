#FROM openjdk:13
#RUN mkdir /tmp/limite-svc
#ADD . /tmp/limite-svc
#RUN chmod +x /tmp/limite-svc/gradlew
#WORKDIR /tmp/limite-svc
#RUN ls -lsah
#RUN ./gradlew clean build
#RUN mv /tmp/limite-svc/build/libs/*.jar /tmp/app.jar
#RUN rm -rf /tmp/limite-svc/

FROM adoptopenjdk:13.0.1_9-jre-openj9-0.17.0-bionic
#COPY --from=0 /tmp/app.jar /tmp
ADD build/libs/*.jar /tmp/app.jar
RUN ls /tmp
ENTRYPOINT ["java", "-jar", "/tmp/app.jar"]
EXPOSE 8083
