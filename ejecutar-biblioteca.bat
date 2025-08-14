@echo off
REM ========================================
REM SCRIPT DE EJECUCIÓN PARA BIBLIOTECA SYSTEM
REM ========================================
REM
REM Este archivo .bat permite ejecutar la aplicación
REM de biblioteca desde el explorador de archivos
REM con doble clic, configurando correctamente
REM el entorno de JavaFX.

echo ==========================================
echo    SISTEMA DE GESTIÓN BIBLIOTECARIA
echo ==========================================
echo.
echo Iniciando aplicación...
echo.

REM Cambiar al directorio donde está el JAR
cd /d "%~dp0"

REM Verificar que existe el archivo JAR
if not exist "target\biblioteca-sistema-ejecutable.jar" (
    echo ERROR: No se encontró el archivo biblioteca-sistema-ejecutable.jar
    echo.
    echo Asegúrate de que el proyecto esté compilado ejecutando:
    echo mvn clean package
    echo.
    pause
    exit /b 1
)

REM Ejecutar la aplicación
echo Ejecutando biblioteca-sistema-ejecutable.jar...
echo.
java -jar "target\biblioteca-sistema-ejecutable.jar"

REM Si hay error, mostrar mensaje
if errorlevel 1 (
    echo.
    echo ==========================================
    echo ERROR AL EJECUTAR LA APLICACIÓN
    echo ==========================================
    echo.
    echo Posibles causas:
    echo 1. Java no está instalado o no está en el PATH
    echo 2. Versión de Java incompatible (se requiere Java 11+)
    echo 3. Problema con las dependencias del JAR
    echo.
    echo Para verificar Java, ejecuta: java -version
    echo.
    pause
)

echo.
echo Aplicación finalizada.
pause