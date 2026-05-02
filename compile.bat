@echo off
echo Compiling source files...
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -cp "libs/*" src/com/example/fabricdefectdetection/**/*.java
echo Compilation finished.
pause