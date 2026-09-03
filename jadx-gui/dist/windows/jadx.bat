@echo off
setlocal EnableExtensions

set "APP_HOME=%~dp0.."
for %%I in ("%APP_HOME%") do set "APP_HOME=%%~fI"
set "JAVA_EXE="

if defined JADX_JAVA_HOME if exist "%JADX_JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JADX_JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE if exist "%APP_HOME%\jre\bin\java.exe" set "JAVA_EXE=%APP_HOME%\jre\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE where java.exe >NUL 2>&1 && set "JAVA_EXE=java.exe"

if not defined JAVA_EXE goto javaNotFound
if not exist "%APP_HOME%\lib\*.jar" goto applicationNotFound

set "CLASSPATH=%APP_HOME%\lib\*"
"%JAVA_EXE%" -XX:+IgnoreUnrecognizedVMOptions -Xms256M -XX:MaxRAMPercentage=70.0 -XX:ParallelGCThreads=3 -Djdk.util.zip.disableZip64ExtraFieldValidation=true --enable-native-access=ALL-UNNAMED %JAVA_OPTS% %JADX_OPTS% -cp "%CLASSPATH%" jadx.cli.JadxCLI %*
set "JADX_EXIT_CODE=%ERRORLEVEL%"
if "%~1"=="" pause
if not "%JADX_EXIT_CODE%"=="0" pause
exit /b %JADX_EXIT_CODE%

:javaNotFound
echo ERROR: No compatible Java runtime was found. 1>&2
echo Use the Windows package with a bundled JRE, set JADX_JAVA_HOME or JAVA_HOME, 1>&2
echo or add a 64-bit Java 11 or newer installation to PATH. 1>&2
pause
exit /b 1

:applicationNotFound
echo ERROR: No jadx application JAR was found under "%APP_HOME%\lib". 1>&2
echo Extract the complete ZIP before running jadx; do not run files inside the ZIP preview. 1>&2
pause
exit /b 1
