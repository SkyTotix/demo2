@echo off
echo.
echo ===============================================
echo DIAGNOSTICO DE LOGOS - BiblioSystem
echo ===============================================
echo.

echo Verificando archivos de logo...
echo.

echo ARCHIVOS DE LOGO:
echo ----------------------------------------
if exist "config\logo-app.png" (
    echo OK - config\logo-app.png existe
) else (
    echo ERROR - config\logo-app.png NO existe
)

if exist "config\logo-login.png" (
    echo OK - config\logo-login.png existe
) else (
    echo ERROR - config\logo-login.png NO existe
)

echo.
echo CONFIGURACION:
echo ----------------------------------------
if exist "config\app-config.properties" (
    echo OK - config\app-config.properties existe
    echo.
    echo Contenido relevante:
    findstr /C:"logo." "config\app-config.properties" 2>nul
else (
    echo ERROR - config\app-config.properties NO existe
)

echo.
echo PROCESOS JAVA:
echo ----------------------------------------
tasklist /FI "IMAGENAME eq java.exe" /FO TABLE 2>nul | findstr /V "INFO:"

echo.
echo ===============================================
echo Diagnostico completado
echo ===============================================
echo.
pause