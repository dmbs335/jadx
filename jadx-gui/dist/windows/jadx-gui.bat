@echo off
setlocal EnableExtensions

set "APP_HOME=%~dp0.."
for %%I in ("%APP_HOME%") do set "APP_HOME=%%~fI"
set "JAVA_EXE="

if defined JADX_JAVA_HOME if exist "%JADX_JAVA_HOME%\bin\javaw.exe" set "JAVA_EXE=%JADX_JAVA_HOME%\bin\javaw.exe"
if not defined JAVA_EXE if exist "%APP_HOME%\jre\bin\javaw.exe" set "JAVA_EXE=%APP_HOME%\jre\bin\javaw.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA_EXE=%JAVA_HOME%\bin\javaw.exe"
if not defined JAVA_EXE where javaw.exe >NUL 2>&1 && set "JAVA_EXE=javaw.exe"

if not defined JAVA_EXE goto javaNotFound

set "CLASSPATH=%APP_HOME%\lib\*"
start "jadx-gui" /B "%JAVA_EXE%" -Xms128M -XX:MaxRAMPercentage=70.0 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Djava.util.Arrays.useLegacyMergeSort=true -Djdk.util.zip.disableZip64ExtraFieldValidation=true -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -Dsun.java2d.noddraw=true -Dsun.java2d.d3d=false -Dsun.java2d.ddforcevram=true -Dsun.java2d.ddblit=false -Dswing.useflipBufferStrategy=true %JAVA_OPTS% %JADX_GUI_OPTS% -cp "%CLASSPATH%" jadx.gui.JadxGUI %*
exit /b %ERRORLEVEL%

:javaNotFound
echo ERROR: No compatible Java runtime was found. 1>&2
echo Use the Windows package with a bundled JRE, set JADX_JAVA_HOME or JAVA_HOME, 1>&2
echo or add a 64-bit Java 11 or newer installation to PATH. 1>&2
pause
exit /b 1
