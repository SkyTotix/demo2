# Historias de Usuario - BiblioSystem

## Información del Proyecto
**Nombre:** BiblioSystem  
**Descripción:** Sistema integral de gestión bibliotecaria con conexión a Oracle Cloud Infrastructure  
**Versión:** 2.0  
**Fecha:** 2024  

---

## Índice de Contenidos
1. [Roles del Sistema](#roles-del-sistema)
2. [Épicas Principales](#épicas-principales)
3. [Historias de Usuario por Rol](#historias-de-usuario-por-rol)
   - [Historias Generales](#historias-generales)
   - [Super Administrador](#super-administrador)
   - [Administrador](#administrador)
   - [Bibliotecario](#bibliotecario)
4. [Criterios de Aceptación Generales](#criterios-de-aceptación-generales)

---

## Roles del Sistema

### 🔴 Super Administrador
- **Descripción:** Usuario con control total del sistema
- **Nivel de Acceso:** 1 (Máximo)
- **Permisos:** Gestión completa del sistema, creación de administradores, configuración global

### 🟡 Administrador  
- **Descripción:** Usuario encargado de la gestión operativa
- **Nivel de Acceso:** 2
- **Permisos:** Gestión de usuarios, libros, préstamos, reportes y multas

### 🟢 Bibliotecario
- **Descripción:** Usuario operativo para atención directa
- **Nivel de Acceso:** 3
- **Permisos:** Gestión de préstamos, devoluciones, consultas de catálogo

---

## Épicas Principales

### 📚 EP-01: Gestión de Libros y Catálogo
Administración completa del inventario bibliográfico

### 👥 EP-02: Gestión de Usuarios
Control de usuarios del sistema y sus permisos

### 📖 EP-03: Sistema de Préstamos
Funcionalidades de préstamo y devolución de libros

### 💰 EP-04: Sistema de Multas
Control de sanciones por retrasos

### 📊 EP-05: Reportes y Estadísticas
Generación de informes del sistema

### ⚙️ EP-06: Configuración del Sistema
Administración de parámetros globales

### 🔐 EP-07: Autenticación y Seguridad
Control de acceso y permisos

### 🔔 EP-08: Notificaciones
Sistema de alertas y comunicaciones

### 📊 EP-09: Dashboard y Estadísticas
Panel de control con métricas en tiempo real

### 🎨 EP-10: Experiencia de Usuario
Interfaz avanzada y animaciones

---

## Historias de Usuario por Rol

### Historias Generales

#### HU-001: Inicio de Sesión
**Como** cualquier usuario del sistema,  
**Quiero** poder iniciar sesión con mis credenciales,  
**Para** acceder a las funcionalidades según mi rol y mantener la seguridad de mi información.

**Valor de Negocio:** Garantiza la seguridad del sistema y proporciona acceso personalizado a cada tipo de usuario.

**Criterios de Aceptación:**
**Given** que soy un usuario registrado,  
**When** ingreso mi username y contraseña correctos,  
**Then** el sistema me autentica y me redirige al dashboard correspondiente a mi rol.

**Given** que ingreso credenciales incorrectas,  
**When** intento iniciar sesión,  
**Then** el sistema muestra un mensaje de error claro y registra el intento fallido.

**Given** que he fallado 3 intentos consecutivos,  
**When** intento iniciar sesión nuevamente,  
**Then** el sistema bloquea temporalmente mi cuenta por 15 minutos.

**Tamaño Estimado:** 3 puntos  
**Prioridad:** Alta  
**Épica:** EP-07

---

#### HU-002: Navegación Principal
**Como** usuario autenticado,  
**Quiero** acceder a un dashboard principal con mis opciones disponibles,  
**Para** navegar fácilmente por el sistema.

**Criterios de Aceptación:**
- [ ] Muestra menú personalizado según rol
- [ ] Permite acceso rápido a funciones principales
- [ ] Muestra información de sesión actual
- [ ] Incluye opción de cerrar sesión
- [ ] Responsive y compatible con diferentes resoluciones

**Prioridad:** Alta  
**Épica:** EP-07

---

### Super Administrador

#### HU-003: Gestión Completa del Sistema
**Como** Super Administrador,  
**Quiero** tener acceso completo a todas las funcionalidades del sistema,  
**Para** administrar y supervisar toda la operación.

**Criterios de Aceptación:**
- [ ] Acceso a todas las secciones del sistema
- [ ] Capacidad de crear usuarios administradores
- [ ] Control de configuración global
- [ ] Acceso a respaldos del sistema
- [ ] Visualización de logs de auditoria

**Prioridad:** Alta  
**Épica:** EP-06

---

#### HU-004: Creación de Administradores
**Como** Super Administrador,  
**Quiero** crear cuentas de administradores,  
**Para** delegar funciones operativas manteniendo control.

**Criterios de Aceptación:**
- [ ] Formulario de creación con datos completos
- [ ] Validación de datos únicos (email, username)
- [ ] Asignación automática de permisos de administrador
- [ ] Notificación al nuevo administrador
- [ ] Registro de la acción en logs

**Prioridad:** Alta  
**Épica:** EP-02

---

#### HU-005: Configuración Global del Sistema
**Como** Super Administrador,  
**Quiero** configurar parámetros globales del sistema,  
**Para** adaptar el comportamiento según las necesidades institucionales.

**Criterios de Aceptación:**
- [ ] Configuración de días de préstamo por defecto
- [ ] Configuración de multas (monto y período)
- [ ] Configuración de límites de préstamos por usuario
- [ ] Configuración de categorías de libros
- [ ] Respaldo automático de configuraciones

**Prioridad:** Media  
**Épica:** EP-06

---

### Administrador

#### HU-006: Gestión de Bibliotecarios
**Como** Administrador,  
**Quiero** crear y gestionar cuentas de bibliotecarios,  
**Para** asegurar el personal operativo adecuado.

**Criterios de Aceptación:**
- [ ] Crear nuevos usuarios bibliotecarios
- [ ] Editar información de bibliotecarios existentes
- [ ] Activar/desactivar cuentas de bibliotecarios
- [ ] Visualizar lista de bibliotecarios activos
- [ ] Historial de actividades por bibliotecario

**Prioridad:** Alta  
**Épica:** EP-02

---

#### HU-007: Gestión del Catálogo de Libros
**Como** Administrador,  
**Quiero** administrar el catálogo completo de libros,  
**Para** mantener actualizado el inventario bibliográfico y facilitar la búsqueda a usuarios y bibliotecarios.

**Valor de Negocio:** Mantiene el inventario actualizado, mejora la eficiencia operativa y facilita el servicio a los usuarios.

**Criterios de Aceptación:**
**Given** que tengo permisos de administrador,  
**When** accedo a la gestión de catálogo,  
**Then** puedo ver la lista completa de libros con opciones de agregar, editar y eliminar.

**Given** que estoy agregando un nuevo libro,  
**When** completo todos los campos obligatorios (ISBN, título, autor, categoría),  
**Then** el sistema valida la información y guarda el libro en el catálogo.

**Given** que un libro ya no está disponible,  
**When** selecciono la opción de dar de baja,  
**Then** el sistema marca el libro como inactivo pero conserva el historial de préstamos.

**Tamaño Estimado:** 8 puntos  
**Prioridad:** Alta  
**Épica:** EP-01

---

#### HU-008: Gestión de Préstamos
**Como** Administrador,  
**Quiero** supervisar y gestionar todos los préstamos del sistema,  
**Para** asegurar el control adecuado del inventario.

**Criterios de Aceptación:**
- [ ] Visualizar todos los préstamos activos
- [ ] Gestionar préstamos vencidos
- [ ] Registrar devoluciones especiales
- [ ] Modificar fechas de devolución
- [ ] Aplicar multas cuando corresponda

**Prioridad:** Alta  
**Épica:** EP-03

---

#### HU-009: Gestión de Multas
**Como** Administrador,  
**Quiero** administrar el sistema de multas,  
**Para** asegurar el cumplimiento de las políticas de devolución.

**Criterios de Aceptación:**
- [ ] Visualizar todas las multas pendientes
- [ ] Registrar pagos de multas
- [ ] Exonerar multas cuando sea justificado
- [ ] Configurar montos de multas
- [ ] Generar reportes de multas

**Prioridad:** Media  
**Épica:** EP-04

---

#### HU-010: Generación de Reportes
**Como** Administrador,  
**Quiero** generar reportes estadísticos del sistema,  
**Para** tomar decisiones informadas sobre la operación.

**Criterios de Aceptación:**
- [ ] Reporte de libros más prestados
- [ ] Reporte de usuarios más activos
- [ ] Reporte de préstamos por período
- [ ] Reporte de multas recaudadas
- [ ] Reporte de inventario actualizado
- [ ] Exportar reportes en PDF/Excel

**Prioridad:** Media  
**Épica:** EP-05

---

### Bibliotecario

#### HU-011: Registro de Préstamos
**Como** Bibliotecario,  
**Quiero** registrar préstamos de libros,  
**Para** controlar la salida de material bibliográfico y proporcionar un servicio eficiente a los usuarios.

**Valor de Negocio:** Automatiza el proceso de préstamos, reduce errores manuales y mantiene control del inventario en tiempo real.

**Criterios de Aceptación:**
**Given** que un usuario quiere solicitar un libro,  
**When** busco el libro por ISBN, título o autor,  
**Then** el sistema muestra la información completa del libro y su disponibilidad actual.

**Given** que el libro está disponible y el usuario no tiene multas pendientes,  
**When** registro el préstamo,  
**Then** el sistema actualiza automáticamente el inventario, establece la fecha de devolución (14 días por defecto) y genera un comprobante.

**Given** que el usuario tiene multas pendientes,  
**When** intento registrar un préstamo,  
**Then** el sistema me informa sobre las multas y no permite el préstamo hasta que sean saldadas.

**Tamaño Estimado:** 5 puntos  
**Prioridad:** Alta  
**Épica:** EP-03

---

#### HU-012: Registro de Devoluciones
**Como** Bibliotecario,  
**Quiero** procesar devoluciones de libros,  
**Para** reintegrar material al inventario disponible.

**Criterios de Aceptación:**
- [ ] Buscar préstamo por ID o datos del usuario
- [ ] Verificar estado del libro devuelto
- [ ] Calcular multas por retraso automáticamente
- [ ] Actualizar estado del préstamo
- [ ] Reintegrar libro al inventario
- [ ] Registrar observaciones si es necesario

**Prioridad:** Alta  
**Épica:** EP-03

---

#### HU-013: Consulta de Catálogo
**Como** Bibliotecario,  
**Quiero** consultar el catálogo de libros,  
**Para** ayudar a los usuarios a encontrar material bibliográfico.

**Criterios de Aceptación:**
- [ ] Búsqueda por título, autor, ISBN o categoría
- [ ] Visualizar disponibilidad en tiempo real
- [ ] Mostrar ubicación física del libro
- [ ] Historial de préstamos del libro
- [ ] Reservar libros para usuarios

**Prioridad:** Alta  
**Épica:** EP-01

---

#### HU-014: Gestión de Multas Básica
**Como** Bibliotecario,  
**Quiero** consultar y registrar pagos de multas,  
**Para** facilitar el proceso a los usuarios.

**Criterios de Aceptación:**
- [ ] Consultar multas de un usuario específico
- [ ] Registrar pagos parciales o totales
- [ ] Imprimir recibos de pago
- [ ] Notificar al usuario sobre multas pendientes
- [ ] Bloquear nuevos préstamos si hay multas vencidas

**Prioridad:** Media  
**Épica:** EP-04

---

#### HU-015: Búsqueda Avanzada de Libros
**Como** Bibliotecario,  
**Quiero** realizar búsquedas avanzadas en el catálogo,  
**Para** atender solicitudes específicas de los usuarios.

**Criterios de Aceptación:**
- [ ] Búsqueda por múltiples criterios simultáneos
- [ ] Filtros por año, editorial, categoría
- [ ] Ordenamiento de resultados
- [ ] Búsqueda de libros similares
- [ ] Sugerencias automáticas mientras escribe

**Prioridad:** Media  
**Épica:** EP-01

---

#### HU-016: Historial de Préstamos
**Como** Bibliotecario,  
**Quiero** consultar el historial de préstamos de usuarios,  
**Para** brindar mejor servicio y resolver consultas.

**Criterios de Aceptación:**
- [ ] Visualizar préstamos actuales de un usuario
- [ ] Historial completo de préstamos pasados
- [ ] Estadísticas de uso por usuario
- [ ] Identificar usuarios frecuentes
- [ ] Detectar patrones de uso sospechosos

**Prioridad:** Baja  
**Épica:** EP-03

---

## Historias Técnicas

#### HT-001: Conexión a Oracle Cloud
**Como** desarrollador del sistema,  
**Quiero** mantener conexión estable con Oracle Cloud Infrastructure,  
**Para** asegurar disponibilidad y rendimiento de datos.

**Criterios de Aceptación:**
- [ ] Connection pooling optimizado
- [ ] Manejo de reconexiones automáticas
- [ ] Monitoreo de estado de conexión
- [ ] Backup automático diario
- [ ] Logs de actividad detallados

**Prioridad:** Alta  
**Épica:** EP-06

---

#### HT-002: Notificaciones del Sistema
**Como** usuario del sistema,  
**Quiero** recibir notificaciones relevantes,  
**Para** estar informado sobre eventos importantes.

**Criterios de Aceptación:**
- [ ] Notificaciones de préstamos próximos a vencer
- [ ] Alertas de multas generadas
- [ ] Confirmaciones de operaciones realizadas
- [ ] Notificaciones de sistema para administradores
- [ ] Panel centralizado de notificaciones

**Prioridad:** Media  
**Épica:** EP-08

---

## Criterios de Aceptación Generales

### Usabilidad
- [ ] Interfaz intuitiva y fácil de usar
- [ ] Tiempo de respuesta menor a 3 segundos
- [ ] Compatible con resoluciones estándar
- [ ] Navegación consistente en todo el sistema

### Seguridad
- [ ] Autenticación segura de usuarios
- [ ] Autorización basada en roles
- [ ] Encriptación de contraseñas
- [ ] Logs de auditoría de operaciones críticas

### Rendimiento
- [ ] Soporte para múltiples usuarios concurrentes
- [ ] Búsquedas optimizadas en catálogo
- [ ] Carga rápida de reportes
- [ ] Backup automático sin afectar rendimiento

### Compatibilidad
- [ ] Funcionamiento en Windows 10+
- [ ] Compatible con JavaFX y BootstrapFX
- [ ] Integración estable con Oracle Cloud
- [ ] Soporte para múltiples resoluciones

---

## Definición de Completado (Definition of Done)

Para que una historia de usuario se considere completada, debe cumplir:

- [ ] Todos los criterios de aceptación están implementados
- [ ] Código revisado y aprobado por el equipo
- [ ] Pruebas unitarias e integración ejecutadas exitosamente
- [ ] Documentación técnica actualizada
- [ ] Funcionalidad probada en entorno de desarrollo
- [ ] Integración con Oracle Cloud verificada
- [ ] Interfaz de usuario validada según estándares
- [ ] Sin errores críticos o bloqueoantes identificados

---

---

## Nuevas Historias de Usuario - Funcionalidades Implementadas

### EP-09: Dashboard y Estadísticas

#### HU-017: Dashboard con Estadísticas Dinámicas
**Como** usuario autenticado,  
**Quiero** ver un dashboard con estadísticas en tiempo real según mi rol,  
**Para** tener una visión clara del estado actual del sistema y mi área de trabajo.

**Valor de Negocio:** Proporciona información relevante al usuario según su rol, mejorando la eficiencia operativa y toma de decisiones.

**Criterios de Aceptación:**
**Given** que soy un Super Administrador,  
**When** accedo al dashboard,  
**Then** veo estadísticas de administradores, bibliotecarios, total de usuarios y estado de la base de datos.

**Given** que soy un Administrador,  
**When** accedo al dashboard,  
**Then** veo estadísticas de bibliotecarios, libros, usuarios y libros activos.

**Given** que soy un Bibliotecario,  
**When** accedo al dashboard,  
**Then** veo estadísticas de libros totales, disponibles, prestados y usuarios registrados.

**Tamaño Estimado:** 8 puntos  
**Prioridad:** Alta  
**Épica:** EP-09

---

#### HU-018: Animaciones de Carga de Estadísticas
**Como** usuario,  
**Quiero** ver animaciones de carga mientras se obtienen las estadísticas,  
**Para** tener retroalimentación visual de que el sistema está procesando la información.

**Valor de Negocio:** Mejora la experiencia de usuario proporcionando retroalimentación visual y percepción de rendimiento.

**Criterios de Aceptación:**
**Given** que las estadísticas están cargando,  
**When** accedo al dashboard,  
**Then** veo indicadores de carga con animaciones suaves y progresivas.

**Given** que las estadísticas se han cargado,  
**When** se completa la carga,  
**Then** las tarjetas se actualizan con animaciones de transición suave.

**Tamaño Estimado:** 3 puntos  
**Prioridad:** Media  
**Épica:** EP-09

---

#### HU-019: Acciones Rápidas Personalizadas
**Como** usuario autenticado,  
**Quiero** tener acceso a acciones rápidas relevantes a mi rol,  
**Para** realizar tareas comunes de manera eficiente desde el dashboard.

**Valor de Negocio:** Aumenta la productividad al proporcionar acceso directo a las funciones más utilizadas según el rol.

**Criterios de Aceptación:**
**Given** que soy Super Administrador,  
**When** veo las acciones rápidas,  
**Then** puedo crear nuevos administradores y acceder a configuración.

**Given** que soy Administrador,  
**When** veo las acciones rápidas,  
**Then** puedo crear bibliotecarios, libros, usuarios y préstamos.

**Given** que soy Bibliotecario,  
**When** veo las acciones rápidas,  
**Then** puedo crear usuarios, préstamos, registrar devoluciones y buscar libros.

**Tamaño Estimado:** 5 puntos  
**Prioridad:** Media  
**Épica:** EP-09

---

### EP-08: Sistema de Notificaciones Avanzado

#### HU-020: Panel de Notificaciones
**Como** usuario,  
**Quiero** acceder a un panel centralizado de notificaciones,  
**Para** ver, filtrar y gestionar todas las alertas y mensajes del sistema.

**Valor de Negocio:** Centraliza la comunicación del sistema, mejora la gestión de información y reduce la pérdida de notificaciones importantes.

**Criterios de Aceptación:**
**Given** que tengo notificaciones pendientes,  
**When** hago clic en el botón de notificaciones,  
**Then** se abre un panel con todas mis notificaciones organizadas.

**Given** que estoy en el panel de notificaciones,  
**When** selecciono un filtro (todas, no leídas, por tipo),  
**Then** la lista se actualiza mostrando solo las notificaciones que coinciden.

**Given** que quiero gestionar notificaciones,  
**When** uso las opciones del panel,  
**Then** puedo marcar todas como leídas o eliminar todas las notificaciones.

**Tamaño Estimado:** 8 puntos  
**Prioridad:** Alta  
**Épica:** EP-08

---

#### HU-021: Badge de Notificaciones No Leídas
**Como** usuario,  
**Quiero** ver un indicador visual del número de notificaciones no leídas,  
**Para** estar al tanto de información pendiente sin abrir el panel.

**Valor de Negocio:** Mantiene a los usuarios informados sobre notificaciones pendientes, mejorando la comunicación y respuesta oportuna.

**Criterios de Aceptación:**
**Given** que tengo notificaciones no leídas,  
**When** estoy en cualquier pantalla del sistema,  
**Then** veo un badge con el número de notificaciones pendientes.

**Given** que marco notificaciones como leídas,  
**When** el contador cambia,  
**Then** el badge se actualiza automáticamente o se oculta si no hay pendientes.

**Tamaño Estimado:** 3 puntos  
**Prioridad:** Media  
**Épica:** EP-08

---

#### HU-022: Actividad Reciente
**Como** usuario,  
**Quiero** ver un resumen de la actividad reciente del sistema,  
**Para** mantenerme informado sobre las operaciones más importantes realizadas.

**Valor de Negocio:** Proporciona transparencia sobre las operaciones del sistema y ayuda en el seguimiento de actividades.

**Criterios de Aceptación:**
**Given** que se han realizado operaciones en el sistema,  
**When** accedo al dashboard,  
**Then** veo una lista de las actividades más recientes con detalles y timestamps.

**Given** que hay diferentes tipos de actividades,  
**When** reviso la lista,  
**Then** cada actividad tiene un icono distintivo y descripción clara.

**Tamaño Estimado:** 5 puntos  
**Prioridad:** Baja  
**Épica:** EP-08

---

### EP-10: Experiencia de Usuario Avanzada

#### HU-023: Interfaz con Animaciones Suaves
**Como** usuario,  
**Quiero** experimentar una interfaz con transiciones y animaciones fluidas,  
**Para** tener una experiencia de usuario moderna y agradable.

**Valor de Negocio:** Mejora la percepción de calidad del software y aumenta la satisfacción del usuario.

**Criterios de Aceptación:**
**Given** que interactúo con elementos de la interfaz,  
**When** hago hover, click o transiciones,  
**Then** veo animaciones suaves y apropiadas para cada acción.

**Given** que navego entre secciones,  
**When** cambio de vista,  
**Then** las transiciones son fluidas y proporcionan retroalimentación visual.

**Tamaño Estimado:** 5 puntos  
**Prioridad:** Baja  
**Épica:** EP-10

---

#### HU-024: Exportación de Datos
**Como** administrador o bibliotecario,  
**Quiero** exportar listas de usuarios, libros o datos del sistema,  
**Para** generar reportes externos o respaldos de información.

**Valor de Negocio:** Facilita el análisis externo de datos y cumple requisitos de respaldo e integración.

**Criterios de Aceptación:**
**Given** que estoy viendo una lista de datos (usuarios, libros, etc.),  
**When** selecciono la opción de exportar,  
**Then** puedo descargar un archivo CSV con la información filtrada.

**Given** que exporto datos,  
**When** se genera el archivo,  
**Then** incluye todos los campos relevantes con formato apropiado.

**Tamaño Estimado:** 3 puntos  
**Prioridad:** Media  
**Épica:** EP-05

---

#### HU-025: Búsqueda y Filtros Avanzados
**Como** usuario que gestiona información,  
**Quiero** usar filtros y búsquedas avanzadas en las listas,  
**Para** encontrar rápidamente la información específica que necesito.

**Valor de Negocio:** Aumenta la eficiencia operativa reduciendo el tiempo de búsqueda y mejorando la productividad.

**Criterios de Aceptación:**
**Given** que estoy en una lista con muchos elementos,  
**When** uso la búsqueda por texto,  
**Then** los resultados se filtran en tiempo real mostrando solo coincidencias.

**Given** que aplico filtros de estado o categoría,  
**When** selecciono opciones específicas,  
**Then** la lista se actualiza mostrando solo elementos que cumplen los criterios.

**Tamaño Estimado:** 5 puntos  
**Prioridad:** Alta  
**Épica:** EP-01

---

**Documento generado:** 2024  
**Proyecto:** BiblioSystem v2.0  
**Estado:** Documento vivo - sujeto a actualizaciones según evolución del proyecto 