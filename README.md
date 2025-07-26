# Valorium – Sistema de Gestión y Depreciación de Activos Fijos

**Versión:** 4.0.0
**Fecha:** Diciembre 2024

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> Valorium nace del concepto de “valor” y ofrece una solución integral para gestionar el ciclo de vida de los activos fijos en la Universidad Tecnológica del Perú. El sistema permite registrar altas, bajas, ventas, revaluaciones y renovaciones, calculando depreciaciones y generando reportes detallados para una toma de decisiones informada.

## Tabla de Contenidos
1. [Características Clave](#-características-clave)
2. [Tecnologías Utilizadas](#-tecnologías-utilizadas)
3. [Estructura del Proyecto](#-estructura-del-proyecto)
4. [Requisitos](#-requisitos)
5. [Instalación y Configuración](#-instalación-y-configuración)
6. [Uso Rápido](#-uso-rápido)
7. [Consideraciones](#-consideraciones)
8. [Capturas de Pantalla](#-capturas-de-pantalla)
9. [Agradecimientos](#-agradecimientos)
10. [Licencia](#-licencia)

## ✨ Características Clave

* **Gestión Integral de Activos:**
    * **Registro Detallado:** Alta de activos fijos con información como nombre, categoría, costo inicial, fecha de adquisición y sede.
    * **Ciclo de Vida Completo:** Soporte para todo el ciclo de vida del activo, incluyendo modificaciones, bajas (por obsolescencia, siniestro, fin de vida útil), y ventas.
    * **Gestión Avanzada:** Funcionalidades de **Revaluación** para ajustar el valor de los activos y **Renovación** para reemplazar activos depreciados o dados de baja.

* **Cálculo de Depreciación:**
    * **Método de Línea Recta:** Cálculo automático de la depreciación mensual y acumulada.
    * **Valor Residual:** Determinación precisa del valor en libros de cada activo en tiempo real.
    * **Proyección:** Herramienta para proyectar la depreciación futura de un activo a lo largo de su vida útil.

* **Análisis y Reportes:**
    * **Inventario Detallado:** Módulo de inventario con vista completa de todos los activos y sus valores.
    * **Análisis de Bajas:** Reportes visuales que muestran pérdidas por categoría y análisis de ventas de activos.
    * **Distribución Geográfica:** Visualización de la distribución de activos por sede mediante gráficos interactivos.
    * **Exportación a Excel:** Generación de reportes de inventario y proyecciones en formato `.xlsx` gracias a la integración con Apache POI.

* **Interfaz de Usuario y Usabilidad:**
    * **Interfaz Gráfica Intuitiva:** Desarrollada con Java Swing para una experiencia de usuario clara y funcional.
    * **Búsqueda y Filtrado:** Potentes herramientas de búsqueda y filtrado avanzado por costo, fecha, categoría y estado.
    * **Notificaciones:** Sistema de alertas para activos que están próximos a cumplir su vida útil (< 6 meses).
    * **Historial de Movimientos:** Registro detallado de todas las operaciones realizadas sobre los activos.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 11+
* **Interfaz Gráfica:** Java Swing
* **Base de Datos:** MySQL 5.7+
* **Conectividad de Base de Datos:** JDBC
* **Reportes y Exportación:**
    * Apache POI (para generación de archivos Excel)
    * JFreeChart (para gráficos y visualizaciones)
* **Componentes Adicionales:**
    * JCalendar (para selección de fechas)
* **Gestión de Dependencias:** Maven

## 📂 Estructura del Proyecto

El proyecto sigue una arquitectura Modelo-Vista-Controlador (MVC) para separar las responsabilidades y mejorar la mantenibilidad.

* `src/main/java/`
    * `controlador/`: Contiene las clases que actúan como intermediarios entre el modelo y la vista (`Main.java`, `ActivoControlador.java`, `Autenticacion.java`).
    * `modelo/`: Define las clases de dominio y la lógica de negocio (`ActivoFijo.java`, `Movimiento.java`, `Sede.java`, `Usuario.java`).
    * `vista/`: Contiene todas las clases de la interfaz gráfica de usuario (GUI) construidas con Swing (`MainFrame.java`, `LoginFrame.java`, `VentaFrame.java`).
    * `servicio/`: Capa de servicio que maneja la lógica de negocio más compleja y las interacciones con la base de datos (`ActivoServicio.java`, `RevaluacionServicio.java`).
    * `util/`: Clases de utilidad para tareas comunes como la conexión a la base de datos (`ConexionBD.java`) y validaciones (`ValidadorFechas.java`).
* `src/main/resources/`: Almacena recursos como íconos, imágenes y el script SQL de la base de datos (`valorium_db.sql`).
* `pom.xml`: Archivo de configuración de Maven que gestiona las dependencias del proyecto.

## ✅ Requisitos

* Java Development Kit (JDK) 11 o superior.
* Servidor de base de datos MySQL 5.7 o superior.
* Apache Maven para la gestión de dependencias y compilación.
* Un IDE compatible con Maven como Apache NetBeans o IntelliJ IDEA.

## 🚀 Instalación y Configuración

1.  **Clonar el repositorio:**
    ```
    git clone https://github.com/Ferinjoque/valorium.git
    cd valorium
    ```

2.  **Configurar la Base de Datos:**
    * Asegúrate de tener un servidor MySQL en funcionamiento.
    * Crea una nueva base de datos llamada `valorium_db`.
    * Importa el script `valorium_db.sql` (ubicado en `src/main/resources/`) para crear las tablas y los usuarios iniciales.
    ```
    CREATE DATABASE IF NOT EXISTS valorium_db;
    USE valorium_db;
    SOURCE ruta/a/tu/proyecto/valorium/src/main/resources/valorium_db.sql;
    ```

3.  **Configurar Credenciales:**
    * Abre el archivo `src/main/java/util/ConexionBD.java`.
    * Modifica las constantes `URL`, `USER`, y `PASSWORD` para que coincidan con la configuración de tu servidor MySQL.

4.  **Compilar y Ejecutar:**
    * Abre el proyecto en tu IDE preferido.
    * Maven resolverá e instalará automáticamente las dependencias (`Apache POI`, `JFreeChart`, etc.).
    * Ejecuta la clase principal `controlador.Main` para iniciar la aplicación.

## ⚡ Uso Rápido

1.  Ejecuta la aplicación. Se abrirá la ventana de inicio de sesión.
2.  Inicia sesión con uno de los usuarios predefinidos en el script SQL (ej. Usuario: `Fernando`, Contraseña: `1234`).
3.  Utiliza el panel de la izquierda para registrar un nuevo activo.
4.  La tabla principal mostrará todos los activos registrados. Desde aquí puedes seleccionar un activo para **Modificar**, **Dar de Baja** o **Vender**.
5.  Usa los botones de acción en el panel de registro para acceder a funcionalidades como **Ver inventario**, **Análisis de Bajas** o **Exportar reporte**.

## 📝 Consideraciones

* La vida útil de los activos está predefinida en la clase `ActivoFijo.java` según categorías comunes.
* El formato de fecha utilizado en toda la aplicación es `dd-MM-yyyy`.
* Los terrenos se consideran activos no depreciables (vida útil = 0).

## 🖼️ Capturas de Pantalla

![Menu de Valorium](/src/main/Valorium.png)

## 🙏 Agradecimientos

* **Equipo de desarrollo:** Rosa Quispe, Constantino Ramirez y Axel Ramirez.

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
