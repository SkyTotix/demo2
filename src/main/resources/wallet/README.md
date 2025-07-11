# Wallet Oracle Cloud

Esta carpeta debe contener los archivos del wallet descargado de Oracle Cloud:

## Archivos requeridos:
- `tnsnames.ora` - Configuración de servicios de base de datos
- `sqlnet.ora` - Configuración de red SQL*Net
- `cwallet.sso` - Wallet auto-abierto SSO
- `ewallet.p12` - Archivo PKCS12 del wallet
- `keystore.jks` - Java KeyStore 
- `truststore.jks` - Java TrustStore
- `ojdbc.properties` - Propiedades de conexión JDBC

## Cómo obtener estos archivos:
1. Ve a Oracle Cloud Console
2. Selecciona tu Autonomous Database
3. Haz clic en "DB Connection"
4. Descarga el Wallet
5. Descomprime el archivo ZIP aquí

⚠️ **IMPORTANTE**: No subas estos archivos a control de versiones por seguridad. 