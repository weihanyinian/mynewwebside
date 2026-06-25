@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM ----------------------------------------------------------------------------

@if "%DEBUG%"=="" @echo off
setlocal enabledelayedexpansion

set "MAVEN_HOME=%~dp0\.mvn\wrapper"
set "MAVEN_OPTS=-Xmx1024m"

if not "%JAVA_HOME%"=="" (
  set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXEC=java"
)

set "WRAPPER_JAR=%MAVEN_HOME%\maven-wrapper.jar"
set "MAVEN_CONFIG=%USERPROFILE%\.m2"

if not exist "%WRAPPER_JAR%" (
  echo ERROR: Maven Wrapper JAR not found at %WRAPPER_JAR%
  exit /b 1
)

%JAVA_EXEC% ^
  %MAVEN_OPTS% ^
  -classpath "%WRAPPER_JAR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*

endlocal
