@echo off
chcp 65001 >nul
echo ========================================
echo PRUEBA DE LOGOS - BiblioSystem
echo ========================================
echo.

echo Estado actual de archivos de logo:
echo.
if exist "config\logo-app.png" (
    echo [OK] logo-app.png: EXISTE
    for %%A in ("config\logo-app.png") do echo     Tamaño: %%~zA bytes
) else (
    echo [ERROR] logo-app.png: NO EXISTE
)

if exist "config\logo-login.png" (
    echo [OK] logo-login.png: EXISTE
    for %%A in ("config\logo-login.png") do echo     Tamaño: %%~zA bytes
) else (
    echo [ERROR] logo-login.png: NO EXISTE
)

echo.
echo Configuracion actual:
echo.
if exist "config\app-config.properties" (
    findstr "logo" "config\app-config.properties"
) else (
    echo [ERROR] Archivo de configuracion no encontrado
)

echo.
echo ========================================
echo INSTRUCCIONES PARA PROBAR:
echo ========================================
echo.
echo 1. VERIFICAR LOGOS ACTUALES:
echo    - La aplicacion ya esta ejecutandose
echo    - Verificar que el logo personalizado aparece en login
echo    - Hacer login y verificar logo en header principal
echo.
echo 2. PROBAR RESTAURAR CONFIGURACION:
echo    - Ir a Configuraciones del Sistema
echo    - Hacer clic en "Restaurar configuracion por defecto"
echo    - Verificar que aparece el icono de libro por defecto
echo.
echo 3. PROBAR CAMBIO DE LOGOS:
echo    - Ir a Configuraciones del Sistema
echo    - Subir nuevo logo para aplicacion
echo    - Subir nuevo logo para login
echo    - Verificar cambios inmediatos
echo.
echo 4. VERIFICAR LOGS:
echo    - Buscar mensajes: "Logo ... actualizado en JavaFX Thread"
echo    - Confirmar que no hay errores
echo.
echo ========================================
echo SOLUCION APLICADA:
echo ========================================
echo.
echo [OK] Threading corregido: Platform.runLater() para toda la UI
echo [OK] Cache deshabilitado: setCache(false)
echo [OK] Calidad mejorada: setSmooth(true)
echo [OK] Carga sincrona: new Image(url, false)
echo [OK] Manejo de errores: try-catch con fallback
echo [OK] URLs unicas: cache_bust + timestamp
echo.
echo RESULTADO ESPERADO:
echo - Los logos personalizados deben mostrarse inmediatamente
echo - Al restaurar configuracion, debe aparecer el icono de libro
echo - Los cambios deben reflejarse sin reiniciar la aplicacion
echo - No debe haber errores en los logs
echo.
echo ========================================
echo Prueba lista - Verificar en la aplicacion
echo ========================================
pause