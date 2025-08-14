@echo off
chcp 65001 > nul
echo ===============================================
echo DIAGNOSTICO DE IMAGENES JAVAFX - BiblioSystem
echo ===============================================
echo.

echo Verificando archivos de imagen...
echo ----------------------------------------
if exist "config\logo-app.png" (
    echo [OK] config\logo-app.png existe
    for %%A in ("config\logo-app.png") do echo     Tamaño: %%~zA bytes
) else (
    echo [ERROR] config\logo-app.png NO existe
)

if exist "config\logo-login.png" (
    echo [OK] config\logo-login.png existe
    for %%A in ("config\logo-login.png") do echo     Tamaño: %%~zA bytes
) else (
    echo [ERROR] config\logo-login.png NO existe
)

echo.
echo Verificando configuracion de JavaFX...
echo ----------------------------------------
echo Propiedades criticas para ImageView:
echo - visible: debe ser true
echo - fitWidth/fitHeight: deben estar configurados
echo - preserveRatio: recomendado true
echo - smooth: por defecto true (mejora calidad)
echo - cache: por defecto true (puede causar problemas)
echo.

echo Verificando procesos Java activos...
echo ----------------------------------------
tasklist /FI "IMAGENAME eq java.exe" /FO TABLE 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo No se encontraron procesos Java ejecutandose
) else (
    echo Procesos Java encontrados
)

echo.
echo POSIBLES CAUSAS DEL PROBLEMA:
echo ----------------------------------------
echo 1. Cache de JavaFX: Las imagenes pueden estar en cache
echo 2. Propiedades de Image: smooth=false o cache=true
echo 3. Visibilidad: ImageView.visible=false
echo 4. Tamaño: fitWidth/fitHeight incorrectos
echo 5. Threading: Actualizacion fuera del JavaFX Thread
echo 6. URL: Problemas con la URL del archivo
echo.

echo SOLUCIONES RECOMENDADAS:
echo ----------------------------------------
echo 1. Verificar que setVisible(true) se ejecute
echo 2. Usar Platform.runLater() para actualizaciones UI
echo 3. Limpiar cache con setImage(null) antes de cargar
echo 4. Usar URLs unicas con timestamp
echo 5. Verificar que fitWidth/fitHeight sean > 0
echo.

echo ===============================================
echo Diagnostico completado
echo ===============================================
pause