@echo off
echo Construyendo y ejecutando el backend...
call mvn clean package -DskipTests
java -jar target\proyectocitas-0.0.1-SNAPSHOT.jar
