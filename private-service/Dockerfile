FROM openjdk:8

RUN mkdir service

COPY ./build/docker/private-service.jar /service/private-service.jar

CMD ["java", "-jar", "/service/private-service.jar"]