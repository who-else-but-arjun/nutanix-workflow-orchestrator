@echo off
setlocal

set MVN_VERSION=3.9.9
set WRAPPER_DIR=%~dp0.mvn\wrapper
set MVN_HOME=%WRAPPER_DIR%\apache-maven-%MVN_VERSION%
set MVN_BIN=%MVN_HOME%\bin\mvn.cmd

if not exist "%MVN_BIN%" (
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$url='https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip';" ^
    "$zip='%WRAPPER_DIR%\apache-maven-%MVN_VERSION%-bin.zip';" ^
    "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
    "Expand-Archive -LiteralPath $zip -DestinationPath '%WRAPPER_DIR%' -Force"
)

call "%MVN_BIN%" %*
