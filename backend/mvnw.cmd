@REM Maven Wrapper for Windows
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

for /f "tokens=1,* delims==" %%a in (%WRAPPER_PROPERTIES%) do (
    if "%%a"=="distributionUrl" set DIST_URL=%%b
)

if not defined DIST_URL set DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9

if not exist "%MAVEN_HOME%\apache-maven-3.9.9\bin\mvn.cmd" (
    echo Downloading Maven...
    powershell -Command "& {Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TEMP%\maven.zip'}"
    powershell -Command "& {Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%MAVEN_HOME%' -Force}"
    del "%TEMP%\maven.zip"
)

"%MAVEN_HOME%\apache-maven-3.9.9\bin\mvn.cmd" %*
