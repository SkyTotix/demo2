@echo off
REM ========================================
REM CREADOR DE ACCESO DIRECTO AL ESCRITORIO
REM ========================================

echo ==========================================
echo   CREANDO ACCESO DIRECTO AL ESCRITORIO
echo ==========================================
echo.

REM Obtener la ruta actual
set "RUTA_ACTUAL=%~dp0"
set "RUTA_BAT=%RUTA_ACTUAL%ejecutar-biblioteca.bat"
set "RUTA_ICONO=%RUTA_ACTUAL%config\logo-app.png"
set "ESCRITORIO=%USERPROFILE%\Desktop"

REM Crear el script VBS para generar el acceso directo
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%TEMP%\CreateShortcut.vbs"
echo sLinkFile = "%ESCRITORIO%\Sistema Biblioteca.lnk" >> "%TEMP%\CreateShortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%TEMP%\CreateShortcut.vbs"
echo oLink.TargetPath = "%RUTA_BAT%" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.WorkingDirectory = "%RUTA_ACTUAL%" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Description = "Sistema de Gestión Bibliotecaria" >> "%TEMP%\CreateShortcut.vbs"
echo oLink.WindowStyle = 1 >> "%TEMP%\CreateShortcut.vbs"
echo oLink.Save >> "%TEMP%\CreateShortcut.vbs"

REM Ejecutar el script VBS
cscript "%TEMP%\CreateShortcut.vbs" >nul 2>&1

REM Limpiar archivo temporal
del "%TEMP%\CreateShortcut.vbs" >nul 2>&1

if exist "%ESCRITORIO%\Sistema Biblioteca.lnk" (
    echo ✅ Acceso directo creado exitosamente en el escritorio!
    echo.
    echo Nombre: "Sistema Biblioteca"
    echo Ubicación: %ESCRITORIO%
    echo.
    echo Ahora puedes ejecutar la aplicación haciendo doble clic
    echo en el acceso directo desde tu escritorio.
) else (
    echo ❌ Error al crear el acceso directo.
    echo.
    echo Puedes ejecutar la aplicación directamente con:
    echo ejecutar-biblioteca.bat
)

echo.
echo ==========================================
echo           PROCESO COMPLETADO
echo ==========================================
pause