@echo off
setlocal EnableExtensions

set "APP_HOME=%~dp0.."
for %%I in ("%APP_HOME%") do set "APP_HOME=%%~fI"
set "JAVA_EXE="
set "LOG_FILE=%TEMP%\jadx-gui-launch.log"

if defined JADX_JAVA_HOME if exist "%JADX_JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JADX_JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE if exist "%APP_HOME%\jre\bin\java.exe" set "JAVA_EXE=%APP_HOME%\jre\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE where java.exe >NUL 2>&1 && set "JAVA_EXE=java.exe"

if not defined JAVA_EXE goto javaNotFound
if not exist "%APP_HOME%\lib\*.jar" goto applicationNotFound

set "CLASSPATH=%APP_HOME%\lib\*"
> "%LOG_FILE%" echo jadx GUI launcher diagnostics
>> "%LOG_FILE%" echo APP_HOME=%APP_HOME%
>> "%LOG_FILE%" echo JAVA_EXE=%JAVA_EXE%
>> "%LOG_FILE%" ver
>> "%LOG_FILE%" "%JAVA_EXE%" -version 2>&1

"%JAVA_EXE%" -Xms128M -XX:MaxRAMPercentage=70.0 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Djava.util.Arrays.useLegacyMergeSort=true -Djdk.util.zip.disableZip64ExtraFieldValidation=true -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -Dsun.java2d.noddraw=true -Dsun.java2d.d3d=false -Dsun.java2d.ddforcevram=true -Dsun.java2d.ddblit=false -Dswing.useflipBufferStrategy=true %JAVA_OPTS% %JADX_GUI_OPTS% -cp "%CLASSPATH%" jadx.gui.JadxGUI %*
set "JADX_EXIT_CODE=%ERRORLEVEL%"
if not "%JADX_EXIT_CODE%"=="0" (
	echo.
	echo jadx-gui exited with code %JADX_EXIT_CODE%.
	echo Launcher diagnostics: %LOG_FILE%
	pause
)
exit /b %JADX_EXIT_CODE%

:javaNotFound
echo ERROR: No compatible Java runtime was found. 1>&2
echo Use the Windows package with a bundled JRE, set JADX_JAVA_HOME or JAVA_HOME, 1>&2
echo or add a 64-bit Java 11 or newer installation to PATH. 1>&2
echo Launcher diagnostics will be written to %LOG_FILE% once Java is found. 1>&2
pause
exit /b 1

:applicationNotFound
echo ERROR: No jadx application JAR was found under "%APP_HOME%\lib". 1>&2
echo Extract the complete ZIP before running jadx; do not run files inside the ZIP preview. 1>&2
pause
exit /b 1
