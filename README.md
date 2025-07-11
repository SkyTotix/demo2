# 🚀 Proyecto Integradora - Oracle Cloud + JavaFX

Una aplicación JavaFX moderna que demuestra la integración con **Oracle Autonomous Database** en la nube, utilizando **BootstrapFX** para estilos elegantes.

## 📋 Características

- ✅ **Conexión a Oracle Cloud** - Autonomous Database
- 🎨 **Interfaz moderna** - JavaFX + BootstrapFX  
- 🔧 **Pruebas automáticas** - Verificación de conectividad
- 📊 **Operaciones CRUD** - Ejemplos de base de datos
- 🔐 **Configuración segura** - Archivos de credenciales protegidos

## 🛠️ Requisitos

- **Java 21+**
- **Maven 3.8+**
- **Oracle Cloud Account** (Free Tier disponible)
- **Autonomous Database** configurada

## 📦 Configuración Inicial

### 1. Clonar y configurar el proyecto

```bash
git clone <tu-repositorio>
cd demo2
```

### 2. Crear Autonomous Database en Oracle Cloud

1. Ve a [Oracle Cloud Console](https://cloud.oracle.com)
2. Navega a **Database** → **Autonomous Database**
3. Haz clic en **Create Autonomous Database**
4. Configuración recomendada:
   - **Workload Type**: `Transaction Processing` 
   - **Deployment Type**: `Serverless`
   - **Configuration**: `Always Free` (si está disponible)
   - **Database Name**: `INTEGRADORA`
   - **Display Name**: `INTEGRADORA`
5. Configura una contraseña segura para el usuario `ADMIN`
6. Haz clic en **Create Autonomous Database**

### 3. Descargar el Wallet

1. Una vez creada la base de datos, haz clic en **DB Connection**
2. Haz clic en **Download Wallet**
3. Mantén **Wallet Type** como `Instance Wallet`
4. Ingresa una contraseña para el wallet
5. Descarga el archivo ZIP

### 4. Configurar el proyecto

1. **Descomprime el wallet**:
   ```bash
   # Crear carpeta wallet
   mkdir -p src/main/resources/wallet
   
   # Descomprimir el wallet descargado
   unzip wallet_INTEGRADORA.zip -d src/main/resources/wallet/
   ```

2. **Configurar credenciales**:
   ```bash
   # Copiar archivo de configuración de ejemplo
   cp src/main/resources/config/database.properties.example src/main/resources/config/database.properties
   ```

3. **Editar las credenciales** en `src/main/resources/config/database.properties`:
   ```properties
   db.cloud.tns_admin=src/main/resources/wallet
   db.cloud.service_name=integradora_medium  # Verificar en tnsnames.ora
   db.cloud.username=ADMIN
   db.cloud.password=TU_PASSWORD_REAL
   ```

## 🚀 Ejecutar la aplicación

```bash
# Compilar y ejecutar
mvn clean javafx:run
```

O usando Maven Wrapper:

```bash
# Windows
./mvnw.cmd clean javafx:run

# Linux/Mac
./mvnw clean javafx:run
```

## 🧪 Funcionalidades de la aplicación

### Panel de Control Principal
- **🔌 Estado de Conexión**: Verificación automática al iniciar
- **🧪 Probar Conexión**: Prueba manual de conectividad
- **ℹ️ Info Base de Datos**: Información detallada del servidor

### Operaciones de Base de Datos
- **🚀 Pruebas Completas**: Suite completa de pruebas
- **📋 Crear Tabla Demo**: Crea tabla de ejemplo
- **📄 Resultados**: Área de logs en tiempo real

## 📁 Estructura del Proyecto

```
demo2/
├── src/main/java/com/example/demo2/
│   ├── HelloApplication.java      # Aplicación principal
│   ├── HelloController.java       # Controlador UI
│   ├── config/
│   │   └── ConfigManager.java     # Gestor de configuración
│   ├── database/
│   │   └── DatabaseManager.java   # Gestor de base de datos
│   └── service/
│       └── DatabaseTestService.java # Servicios de prueba
├── src/main/resources/
│   ├── config/
│   │   ├── database.properties.example
│   │   └── database.properties    # Tu configuración (no en git)
│   ├── wallet/                    # Archivos del wallet (no en git)
│   │   └── README.md
│   └── com/example/demo2/
│       └── hello-view.fxml        # Interfaz JavaFX
└── pom.xml                        # Dependencias Maven
```

## 🔧 Dependencias Principales

- **JavaFX 21** - Framework de interfaz gráfica
- **BootstrapFX 0.4.0** - Estilos CSS tipo Bootstrap
- **Oracle JDBC 23.3.0** - Driver de base de datos
- **Oracle Security Libraries** - Para conexiones seguras

## 🔐 Seguridad

⚠️ **IMPORTANTE**: Los archivos sensibles están protegidos:

- `src/main/resources/wallet/` - Certificados Oracle
- `database.properties` - Credenciales de base de datos
- Todos los archivos `.jks`, `.p12`, `.sso` - Archivos de seguridad

Estos archivos están en `.gitignore` y NO se suben al repositorio.

## 🐛 Solución de Problemas

### Error de conexión TNS
```
💡 Verifica que el wallet esté en la carpeta correcta
💡 Verifica que el service_name sea correcto en database.properties
```

### Error de autenticación
```
💡 Verifica el usuario y contraseña en database.properties
```

### Error de red
```
💡 Verifica la conectividad de red y la configuración del wallet
```

## 📚 Próximos Pasos

Una vez que tengas la conexión funcionando, puedes:

1. **Desarrollar tu aplicación específica** - Reemplazar la UI de pruebas
2. **Agregar más tablas** - Expandir el modelo de datos
3. **Implementar autenticación** - Sistema de usuarios
4. **Agregar más funcionalidades** - Según tus requerimientos

## 🤝 Contribución

Este es un proyecto base para desarrollo. Puedes:

- Expandir las funcionalidades
- Mejorar la interfaz de usuario
- Agregar más pruebas
- Optimizar la configuración

## 📄 Licencia

Proyecto educativo - Uso libre para fines académicos.

---

