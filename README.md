CMQ Salud — Gestión Documentos

Aplicación móvil nativa para la gestión, trazabilidad y digitalización de documentos laborales y clínicos

---

Descripción del Proyecto

El sistema centraliza la recepción, registro y validación de documentos de distintos establecimientos de salud comunal (CESFAM y SAPU)
Resuelve la problemática de dispersión de información y documentos mediante una base de datos local persistente y digitalización.

---

## 🚀 Características Principales

Autenticación Institucional Segura:
  * Control de acceso por RUT con validación (dígito verificador).
  * Selector dinámico del centro de salud.
  * Cuadro informativo de asistencia para restablecimiento de credenciales.

* Gestión Documental (CRUD con Room / SQLite):
  * Creación: Registro de expedientes con título, categoría clínica/administrativa, fecha automática y observaciones.
  * Edición: Modificación de expedientes existentes pasando su estado automáticamente a *En Revisión*.
  * Eliminación segura: Diálogo de confirmación previa para evitar pérdida accidental de datos.

* Digitalización:
  * Cámara Nativa: Captura de fotografías mediante `TakePicture` solicitando permisos en tiempo de ejecución.
  * Carga de Archivos: Selección de imágenes locales desde la galería del dispositivo mediante `GetContent()`.

* Interacción y Usabilidad Clínica:
  * Ciclo de Estados: Cambio rápido del estado administrativo (*Pendiente* ➔ *En Revisión* ➔ *Validado*) al interactuar con la tarjeta.
  * Vista Rápida: Texto emergente con visualización ampliada de la fotografía de respaldo y notas clínicas.
  * Búsqueda Dinámica: Filtrado en tiempo real por título del expediente.
  * Módulo de Ajustes: Ficha del funcionario y cierre de sesión seguro.

---

Información Tecnológica

* Lenguaje: Kotlin
* Interfaz de Usuario: Jetpack Compose
* Arquitectura: MVVM (Model-View-ViewModel)
* Persistencia Local: Room Database (SQLite)
* Carga de Imágenes: Coil Compose
* Navegación: Navegacion con rutas seguras
* Acceso a Hardware: Android Camera Intent + AndroidX FileProvider[cite: 20

---

Estructura del Proyecto

```text
cl.duoc.cmqsalud/
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Configuración de base de datos
│   │   └── DocumentoDao.kt         # Métodos de acceso a datos (CRUD)
│   └── repositorio/
│       └── DocumentoRepository.kt  # Capa intermedia de repositorio
│
├── model/
│   └── DocumentoFuncionario.kt     # Entidad (Categoría y Estado)
│
├── navegacion/
│   └── AppNavigation.kt            # NavHost y rutas serializables
│
├── ui/
│   ├── componentes/
│   │   └── AppComponents.kt        # Colores y TarjetaExpediente
│   ├── home/
│   │   └── HomeScreen.kt           # Bandeja, búsqueda, filtros y ajustes
│   ├── login/
│   │   └── LoginScreen.kt          # Acceso, selector CS y validación RUT
│   └── registro/
│       └── RegistroScreen.kt       # Formulario, cámara, galería y FileProvider
│
└── viewmodel/
    └── DocumentoViewModel.kt       # Gestión del estado de UI y lógica de negocio
