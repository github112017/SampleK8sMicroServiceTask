FROM openjdk:8

RUN mkdir service

COPY ./build/docker/public-service.jar /service/public-service.jar

CMD ["java", "-jar", "/service/public-service.jar"]