@echo off
chcp 65001 > nul
cd /d "%~dp0"
javac -encoding UTF-8 Snake.java
java -Dfile.encoding=UTF-8 Snake
pause
