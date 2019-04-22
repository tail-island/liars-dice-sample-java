@echo off

mvn package

copy target\liars-dice-sample-java-1.0-jar-with-dependencies.jar ..\liars-dice\dist\linux-x64\java
copy target\liars-dice-sample-java-1.0-jar-with-dependencies.jar ..\liars-dice\dist\win-x64\java
