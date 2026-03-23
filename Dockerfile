FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

LABEL authors="tapia"

ADD ./target/msvc-category-0.0.1-SNAPSHOT.jar msvc-category.jar

ENTRYPOINT ["java", "-jar", "msvc-category.jar"]