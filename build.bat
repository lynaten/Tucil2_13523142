@echo off
echo [BUILD] Compiling Java files...
mkdir bin 2>nul
javac -d bin -sourcepath src src/app/App.java