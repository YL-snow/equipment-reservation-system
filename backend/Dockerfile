FROM 10.160.199.150:8443/32301163/openjdk:21

LABEL author="32301163"
WORKDIR /app

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8082

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]