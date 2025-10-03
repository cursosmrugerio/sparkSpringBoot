# PRD: Capacitación Apache Spark con Java y Spring Boot

## 1. Resumen

### Objetivo General
Capacitar a desarrolladores Java en el uso de Apache Spark para procesamiento de datos a gran escala, mediante un enfoque práctico orientado a proyectos reales con Spring Boot.

### Duración
**8 horas** distribuidas en **4 sesiones de 2 horas** cada una.

### Audiencia
Desarrolladores Java con conocimientos básicos de:
- Java 8+ (lambdas, streams)
- Spring Boot fundamentals
- SQL básico
- Git y Maven/Gradle

### Entregables
- Proyecto completo funcional: Sistema de Análisis de Logs de E-commerce
- Repositorio Git con código documentado
- Guías de referencia y buenas prácticas
- Dataset de ejemplo para práctica

---

## 2. Problemática y Justificación

### Problema
Los desarrolladores Java tradicionales enfrentan limitaciones al procesar grandes volúmenes de datos:
- Procesamiento secuencial ineficiente para millones de registros
- Dificultad para escalar aplicaciones de analytics
- Falta de conocimiento en herramientas de big data modernas

### Solución
Apache Spark proporciona:
- Procesamiento distribuido y paralelo de datos
- API amigable para desarrolladores Java
- Integración natural con Spring Boot
- Capacidad de procesamiento in-memory 100x más rápido que Hadoop MapReduce

### Beneficios
- **Técnicos:** Equipo capacitado en tecnología de análisis de datos a gran escala
- **Negocio:** Capacidad de construir soluciones de analytics y reporting robustas
- **ROI:** Reducción de tiempos de procesamiento de datos de horas a minutos

---

## 3. Alcance del Programa

### 3.1 Proyecto Práctico Central
**Sistema de Análisis de Logs de E-commerce**

Procesamiento y análisis de transacciones de comercio electrónico para generar:
- Reportes de ventas por categoría y producto
- Detección de patrones de compra
- Identificación de anomalías en transacciones
- Dashboard de métricas en tiempo real

### 3.2 Stack Tecnológico

⚠️ **IMPORTANTE**: Versiones específicas validadas y compatibles entre sí.

- **Java:** 17.0.13 LTS (⚠️ NO usar Java 18 o superior - incompatible con Spark 3.5.0)
- **Apache Spark:** 3.5.0
- **Spring Boot:** 3.2.0
- **Scala:** 2.12 (incluida con Spark)
- **ANTLR:** 4.9.3 (forzada vía dependency management)
- **PostgreSQL:** 15
- **Docker:** 20.10+ o Docker Desktop
- **Maven:** 3.8+

---

## 4. Contenido Detallado por Bloque

### **BLOQUE 1: Fundamentos de Spark y Configuración (2 horas)**

#### Objetivos de Aprendizaje
- Comprender la arquitectura distribuida de Spark
- Configurar ambiente de desarrollo con Docker
- Integrar Spark con Spring Boot
- Ejecutar primeras operaciones de lectura de datos

#### Contenido Teórico (40 min)
- ¿Qué es Apache Spark y casos de uso?
- Arquitectura: Driver, Executors, Cluster Manager
- Conceptos clave: RDD, DataFrame, Dataset
- Spark vs procesamiento tradicional: comparativa de rendimiento
- Ecosistema Spark (Spark SQL, Streaming, MLlib)

#### Práctica Guiada (70 min)
1. **Setup de infraestructura**
   - Verificar Java 17 instalado con SDKMAN
   - Configurar JAVA_HOME y PATH
   - Levantar PostgreSQL con Docker Compose
   - Verificar conexión a PostgreSQL
   - ⚠️ **Nota**: Spark UI estará deshabilitada por conflictos de servlets

2. **Proyecto Spring Boot inicial**
   - Crear proyecto con Spring Initializr
   - Configurar pom.xml con:
     - Dependencias de Spark 3.5.0
     - `<dependencyManagement>` para forzar ANTLR 4.9.3
     - Exclusiones de ANTLR en spark-sql
     - Dependencias javax.servlet y Jersey
     - JVM arguments en spring-boot-maven-plugin
   - Configurar SparkSession como Bean Spring (modo local)
   - Configurar profile `local` con `spark.ui.enabled=false`

3. **Primera lectura de datos**
   - Cargar archivo CSV de transacciones
   - Operaciones básicas: count(), show(), printSchema()
   - Entender lazy evaluation

#### Ejercicio Práctico
- Leer dataset de productos y mostrar las primeras 20 filas
- Contar número total de registros
- Mostrar schema del DataFrame

#### Entregable del Bloque
✅ Proyecto Spring Boot + Spark funcionando  
✅ Cluster Docker levantado  
✅ Primera lectura exitosa de datos

---

### **BLOQUE 2: Transformaciones y Análisis de Datos (2 horas)**

#### Objetivos de Aprendizaje
- Dominar operaciones de transformación en DataFrames
- Realizar agregaciones y joins
- Exponer resultados mediante REST API
- Escribir queries SQL con Spark

#### Contenido Teórico (30 min)
- Transformaciones vs Acciones (lazy vs eager)
- Operaciones fundamentales: select, filter, groupBy, orderBy
- Funciones de agregación: sum, avg, count, max, min
- Joins: inner, left, right, full outer
- Spark SQL: sintaxis y ventajas

#### Práctica Guiada (80 min)
1. **Análisis exploratorio de datos**
   - Filtrar transacciones por rango de fechas
   - Seleccionar columnas específicas
   - Ordenar por monto de transacción
   - Manejar valores null

2. **Agregaciones de negocio**
   - Ventas totales por categoría de producto
   - Top 10 productos más vendidos
   - Promedio de ticket de compra
   - Ventas por mes/día

3. **Joins entre datasets**
   - Join de transacciones con catálogo de productos
   - Join con información de clientes
   - Enriquecer datos con información externa

4. **API REST con Spring Boot**
   - Endpoint: GET /api/sales/by-category
   - Endpoint: GET /api/products/top-selling
   - Endpoint: GET /api/sales/daily-summary
   - Manejo de parámetros (fechas, filtros)

#### Ejercicio Práctico
- Crear endpoint que retorne ventas por región
- Implementar filtros dinámicos (fecha inicio, fecha fin)
- Agregar paginación de resultados

#### Entregable del Bloque
✅ API REST con 4-5 endpoints funcionales  
✅ Queries SQL de Spark documentadas  
✅ Tests básicos de endpoints

---

### **BLOQUE 3: Procesamiento Avanzado y Optimización (2 horas)**

#### Objetivos de Aprendizaje
- Crear funciones personalizadas (UDFs)
- Optimizar performance con particionamiento
- Implementar caché estratégico
- Persistir resultados en PostgreSQL
- Limpiar y validar datos

#### Contenido Teórico (30 min)
- User Defined Functions (UDFs) en Java
- Estrategias de particionamiento de datos
- Caché y persistencia: cuándo y cómo usar
- Formatos de almacenamiento: Parquet vs CSV vs JSON
- Mejores prácticas de optimización

#### Práctica Guiada (80 min)
1. **UDFs personalizadas**
   - Crear UDF para validación de emails
   - UDF para categorización de montos (bajo, medio, alto)
   - UDF para detección de fraude básico
   - Registrar y usar UDFs en Spark SQL

2. **Optimización de queries**
   - Implementar particionamiento por fecha
   - Usar bucketing para joins frecuentes
   - Aplicar broadcast joins para tablas pequeñas
   - Medir tiempos con y sin optimización

3. **Caché estratégico**
   - Cachear DataFrames usados múltiples veces
   - Diferentes niveles de persistencia
   - Liberar memoria con unpersist()

4. **Integración con PostgreSQL**
   - Configurar JDBC en Spring Boot
   - Escribir resultados procesados en tablas
   - Leer datos desde PostgreSQL
   - Usar Spring Data JPA para consultas rápidas

5. **Limpieza de datos**
   - Manejo de nulls: fillna, dropna
   - Validación de tipos de datos
   - Eliminación de duplicados
   - Normalización de strings

#### Ejercicio Práctico
- Detectar transacciones sospechosas (monto > 3 desviaciones estándar)
- Guardar alertas en PostgreSQL
- Crear endpoint para consultar alertas

#### Entregable del Bloque
✅ Sistema con UDFs implementadas  
✅ Queries optimizadas (con métricas de mejora)  
✅ Persistencia bidireccional con PostgreSQL  
✅ Sistema de detección de anomalías funcional

---

### **BLOQUE 4: Batch Processing y Automatización (2 horas)**

#### Objetivos de Aprendizaje
- Implementar jobs programados con Spring Scheduler
- Diseñar pipelines de procesamiento batch
- Manejar errores y reintentos
- Configurar sistema para producción
- Entender conceptos de streaming (introducción)

#### Contenido Teórico (25 min)
- Batch vs Streaming: cuándo usar cada uno
- Scheduling de jobs en Spring
- Estrategias de manejo de errores
- Monitoreo y logging de Spark jobs
- Consideraciones de deployment

#### Práctica Guiada (85 min)
1. **Jobs programados**
   - Configurar @Scheduled para procesar archivos diarios
   - Leer archivos de directorio con pattern matching
   - Procesamiento incremental (solo datos nuevos)
   - Marcar archivos procesados

2. **Pipeline completo de ETL**
   - Extract: Leer CSVs de directorio /input
   - Transform: Limpiar, validar y agregar
   - Load: Guardar en PostgreSQL y Parquet

3. **Sistema de reportes automáticos**
   - Generar reporte diario de ventas
   - Guardar reporte en PDF/Excel (opcional)
   - Enviar notificación (email simulado)

4. **Manejo robusto de errores**
   - Try-catch en jobs de Spark
   - Logging estructurado con SLF4J
   - Reintentos con backoff exponencial
   - Dead letter queue para registros fallidos

5. **Configuración por ambientes**
   - application-dev.yml
   - application-prod.yml
   - Variables de entorno para secretos
   - Profiles de Spring Boot

6. **Dashboard de métricas**
   - Endpoint que expone últimas ejecuciones
   - Estado de jobs (success, failed, running)
   - Métricas: registros procesados, tiempo de ejecución

7. **Introducción a Spark Streaming (conceptual)**
   - Diferencias con batch processing
   - Casos de uso reales
   - Arquitectura básica
   - Recursos para profundizar

#### Ejercicio Práctico Final
- Implementar job que procese transacciones del día anterior
- Detectar productos con bajo stock
- Generar alerta automática
- Guardar resultado en PostgreSQL

#### Entregable del Bloque
✅ Sistema completo con jobs automatizados  
✅ Pipeline ETL funcional  
✅ Configuración lista para diferentes ambientes  
✅ Dashboard básico de monitoreo  
✅ Documentación de deployment

---

## 5. Entregables Finales del Programa

### 5.1 Proyecto Completo
**Repositorio Git** conteniendo:

```
ecommerce-analytics/
├── README.md                          # Documentación del proyecto
├── docker-compose.yml                 # Infraestructura completa
├── pom.xml                           # Dependencias Maven
├── data/
│   ├── transactions.csv              # Dataset de ejemplo (10K registros)
│   ├── products.csv
│   └── customers.csv
├── src/main/java/
│   ├── config/
│   │   ├── SparkConfig.java         # Configuración de Spark
│   │   └── DatabaseConfig.java
│   ├── controller/
│   │   └── AnalyticsController.java # REST endpoints
│   ├── service/
│   │   ├── SparkJobService.java
│   │   ├── AnalyticsService.java
│   │   └── ReportService.java
│   ├── scheduler/
│   │   └── BatchJobScheduler.java   # Jobs programados
│   ├── udf/
│   │   └── CustomValidations.java   # UDFs personalizadas
│   ├── model/
│   │   ├── Transaction.java
│   │   ├── Product.java
│   │   └── SalesReport.java
│   └── repository/
│       └── SalesReportRepository.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-docker.yml
│   └── logback-spring.xml
└── docs/
    ├── setup-guide.md               # Guía de instalación
    ├── api-documentation.md         # Documentación de endpoints
    └── spark-cheatsheet.md          # Referencia rápida
```

### 5.2 Documentación
- **README completo** con instrucciones de setup
- **API Documentation** con ejemplos de uso
- **Guía de troubleshooting** para errores comunes
- **Cheatsheet** de operaciones Spark más usadas
- **Slides de presentación** de cada bloque

### 5.3 Datasets de Ejemplo
- **transactions.csv**: 10,000 transacciones realistas
- **products.csv**: 500 productos con categorías
- **customers.csv**: 1,000 clientes con demografía

---

## 6. Metodología de Enseñanza

### Enfoque Práctico (70/30)
- **70% práctica**: Coding hands-on, ejercicios guiados
- **30% teoría**: Conceptos fundamentales, arquitectura, mejores prácticas

### Estructura de Cada Sesión
1. **Revisión** (5 min): Recap del bloque anterior
2. **Teoría** (25-40 min): Presentación de conceptos con ejemplos
3. **Demo en vivo** (15-20 min): Instructor muestra implementación
4. **Práctica guiada** (40-60 min): Estudiantes codean junto al instructor
5. **Ejercicio independiente** (15-20 min): Reto individual o por equipos
6. **Q&A y cierre** (5-10 min): Dudas, resumen, preview siguiente sesión

### Soporte Durante el Curso
- Repositorio con issues para reportar problemas

---

## 7. Pre-requisitos Técnicos

### Conocimientos Requeridos
- ✅ Java 8+ (lambdas, streams API)
- ✅ Spring Boot básico (dependency injection, controllers)
- ✅ SQL (SELECT, JOIN, GROUP BY)
- ✅ Git básico
- ✅ Maven

### Software Necesario (instalar ANTES del curso)

⚠️ **CRÍTICO**: Seguir estas instrucciones exactas para evitar problemas de compatibilidad.

- **Java JDK 17.0.13 LTS** (instalado con SDKMAN - recomendado)
  ```bash
  # Instalar SDKMAN
  curl -s "https://get.sdkman.io" | bash
  source "$HOME/.sdkman/bin/sdkman-init.sh"

  # Instalar Java 17
  sdk install java 17.0.13-tem
  sdk use java 17.0.13-tem

  # Verificar (debe mostrar 17.0.13)
  java -version
  ```
  ⚠️ **NO instalar Java 18 o superior** - incompatible con Spark 3.5.0

- **Docker Desktop** (Windows/Mac) o Docker Engine 20.10+ (Linux)
- **Eclipse IDE** 2023+ o **IntelliJ IDEA** 2023+
- **Git** 2.x+
- **Maven** 3.8+ (o usar Maven Wrapper incluido)
- **Postman** o similar para testing de APIs
- **DBeaver** o cliente PostgreSQL (opcional pero recomendado)

### Recursos de Hardware Mínimos
- **RAM**: 16 GB (mínimo absoluto 8 GB - puede tener problemas de rendimiento)
- **Disco**: 20 GB libres
- **CPU**: 4 cores mínimo (recomendado 8 cores)

**Nota**: Spark es intensivo en memoria. Con 8GB es posible pero limitado para datasets grandes.

---

## 8. Consideraciones Técnicas Críticas

⚠️ **IMPORTANTE**: Esta sección documenta problemas de compatibilidad conocidos y sus soluciones.

### 8.1 Compatibilidad de Versiones

#### Java 17 - Requisito Estricto
- ✅ **Java 17**: Compatible y validado
- ❌ **Java 18+**: **INCOMPATIBLE** - Spark 3.5.0 usa Hadoop que requiere Security Manager (removido en Java 18)
- **Error típico**: `getSubject is supported only if a security manager is allowed`

#### Conflicto ANTLR
- **Problema**: Spring Boot 3.2.0 incluye ANTLR 4.13.1, pero Spark 3.5.0 requiere ANTLR 4.9.x
- **Síntoma**: `Could not deserialize ATN with version 3 (expected 4)`
- **Solución**: Usar `<dependencyManagement>` en pom.xml para forzar ANTLR 4.9.3

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-runtime</artifactId>
            <version>4.9.3</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Conflicto Jakarta EE vs javax
- **Problema**: Spring Boot 3 usa Jakarta EE (`jakarta.servlet.*`), Spark usa javax (`javax.servlet.*`)
- **Síntoma**: `NoClassDefFoundError: javax/servlet/Servlet`
- **Solución**: Agregar dependencias javax.servlet y Jersey 2.40

### 8.2 Configuraciones Obligatorias

#### Dependency Management
El pom.xml **DEBE** incluir:
1. `<dependencyManagement>` para ANTLR 4.9.3
2. Exclusión de ANTLR en spark-sql dependency
3. Dependencias javax.servlet-api 4.0.1
4. Jersey 2.40 (server y container-servlet)

#### JVM Arguments para Java 17
El módulo system de Java 17 requiere flags especiales:
```xml
<jvmArguments>
    --add-opens java.base/java.lang=ALL-UNNAMED
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED
    --add-opens java.base/sun.util.calendar=ALL-UNNAMED
</jvmArguments>
```

#### Spark UI Deshabilitada
- **Razón**: Conflictos irresolubles entre servlets de Spark UI y Spring Boot 3
- **Configuración**: `spark.ui.enabled=false` en SparkConfig
- **Impacto**: No habrá acceso a Spark UI web (http://localhost:4040)

### 8.3 Modo de Operación Recomendado

**Para desarrollo local:**
- Usar Spark en modo `local[*]` (embebido)
- Solo PostgreSQL en Docker
- NO es necesario cluster Spark en Docker

**Cluster Spark Docker (opcional/avanzado):**
- Útil para simular ambiente distribuido
- Requiere configuraciones adicionales
- Perfil `docker` en Spring Boot

### 8.4 Documentación de Referencia

Para troubleshooting detallado y configuración paso a paso, consultar:
- `docs/CONFIGURACION_CORRECTA.md` - Guía completa de troubleshooting
- `README.md` - Quick start y setup básico

---

## 9. Métricas de Éxito

### Objetivos Cuantificables
- ✅ **100%** de participantes con ambiente funcionando en Bloque 1
- ✅ **90%+** de participantes completan el proyecto final
- ✅ **85%+** pueden explicar cuándo usar Spark vs procesamiento tradicional
- ✅ **80%+** pueden crear un job de Spark desde cero


---

**Versión del Documento**: 1.1 (Actualizado con consideraciones técnicas)
**Fecha**: Octubre 2025
**Última Actualización**: Octubre 2025 - Validación técnica completa
**Próxima Revisión**: Post-primera impartición

**Cambios en v1.1**:
- ✅ Versiones específicas del stack tecnológico validadas
- ✅ Agregada sección "Consideraciones Técnicas Críticas"
- ✅ Documentación de conflictos de dependencias (ANTLR, javax/jakarta)
- ✅ Instrucciones detalladas de instalación de Java 17 con SDKMAN
- ✅ Aclaración sobre Spark UI deshabilitada
- ✅ Actualización de práctica guiada Bloque 1 con configuraciones reales
- ✅ Referencias a documentación de troubleshooting

---
