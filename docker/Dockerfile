FROM openjdk:17-slim
ARG srcProgram
ARG srcXml
WORKDIR /
RUN mkdir /result
ADD ${srcProgram} AgnosCubeBuilder.jar
ADD ${srcXml} cube.xml
CMD java -jar -Dspring.profiles.active=prod AgnosCubeBuilder.jar --xml=cube.xml --table=rolled --name=${RESULTCUBE} --output=/result && date
