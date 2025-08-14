@echo off
echo ========================================
echo PRUEBA DE ACTUALIZACION DE LOGOS
echo ========================================
echo.
echo Este script simula la actualizacion de logos para verificar
echo que los cambios se reflejen inmediatamente en la aplicacion.
echo.
echo 1. Verificando archivos de logo existentes...
dir config\logo-*.png
echo.
echo 2. Creando copia de respaldo temporal...
copy config\logo-app.png config\logo-app-backup.png >nul 2>&1
copy config\logo-login.png config\logo-login-backup.png >nul 2>&1
echo Respaldos creados.
echo.
echo 3. Para probar la actualizacion:
echo    - Reemplaza config\logo-app.png con una nueva imagen
echo    - Reemplaza config\logo-login.png con una nueva imagen
echo    - Los cambios deberian reflejarse INMEDIATAMENTE
echo.
echo 4. Para restaurar los logos originales:
echo    copy config\logo-app-backup.png config\logo-app.png
echo    copy config\logo-login-backup.png config\logo-login.png
echo.
echo ========================================
echo VERIFICACION COMPLETADA
echo ========================================
pause