# E-commerce Analytics con Apache Spark y Spring Boot

Sistema de análisis de datos de e-commerce utilizando Apache Spark para procesamiento distribuido integrado con Spring Boot.

## 📋 Progreso de Capacitación

### ✅ Bloque 1: Fundamentos y Configuración - COMPLETADO
- Setup de infraestructura (Java 17, Docker, PostgreSQL)
- Configuración Spark + Spring Boot
- Primera lectura de datos (CSVs)
- API REST básica (4 endpoints)

### ✅ Bloque 2: Transformaciones y Análisis - COMPLETADO
- Transformaciones avanzadas (select, filter, groupBy, orderBy)
- Agregaciones de negocio (sum, avg, count, max, min)
- Joins entre datasets (transactions + products + customers)
- Window Functions (rankings)
- API REST completa (7 endpoints nuevos)

### ✅ Bloque 3: Procesamiento Avanzado y Optimización - COMPLETADO
- User Defined Functions (UDFs) personalizadas (5 UDFs)
- Optimización con caché y broadcast joins
- Detección de fraude con análisis estadístico
- Persistencia bidireccional PostgreSQL
- API REST extendida (20 endpoints nuevos)

### Stack Tecnológico
- **Java:** 17 LTS (OBLIGATORIO - Java 18+ incompatible)
- **Apache Spark:** 3.5.0
- **Spring Boot:** 2.7.18 (downgraded from 3.2.0 for ANTLR compatibility)
- **PostgreSQL:** 15
- **ANTLR:** 4.9.3 (forced version for Spark 3.5.0 compatibility)
- **Docker:** PostgreSQL container

---

## 🚀 Quick Start (3 Pasos)

### Opción 1: Script Automatizado (Recomendado)

```bash
# 1. Ejecutar script de inicio
./start.sh
```

El script automáticamente:
- ✅ Verifica Java 17
- ✅ Compila el proyecto
- ✅ Inicia PostgreSQL (Docker)
- ✅ Ejecuta la aplicación con la configuración correcta

### Opción 2: Ejecución Manual

#### Paso 1: Instalar Java 17

⚠️ **CRÍTICO**: Java 17 es obligatorio (Java 18+ NO funciona con Spark 3.5.0)

```bash
# Instalar con SDKMAN (recomendado)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.13-tem
sdk use java 17.0.13-tem
java -version  # Debe mostrar: openjdk version "17.0.13"
```

#### Paso 2: Iniciar PostgreSQL

```bash
# Levantar PostgreSQL con Docker
docker compose up -d postgres

# Verificar que esté corriendo
docker ps | grep postgres
```

**Credenciales PostgreSQL:**
- Host: `localhost:5432`
- Usuario: `sparkuser`
- Password: `sparkpass`
- Base de datos: `ecommerce_analytics`

#### Paso 3: Compilar y Ejecutar

**Opción A: Con Maven (Desarrollo)**
```bash
# Configurar Java 17
export JAVA_HOME=~/.sdkman/candidates/java/17.0.13-tem
export PATH=$JAVA_HOME/bin:$PATH

# Compilar y ejecutar
mvn clean spring-boot:run -Dspring-boot.run.profiles=local
```

**Opción B: Con JAR (Producción)**
```bash
# 1. Compilar
mvn clean package -DskipTests

# 2. Ejecutar con JVM arguments (OBLIGATORIO para Java 17)
export JAVA_HOME=~/.sdkman/candidates/java/17.0.13-tem
export PATH=$JAVA_HOME/bin:$PATH

java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
     -jar target/analytics-1.0.0.jar \
     --spring.profiles.active=local
```

⚠️ **IMPORTANTE**: Los argumentos `--add-opens` son obligatorios cuando ejecutas el JAR directamente. Sin ellos obtendrás `IllegalAccessError`.

**Aplicación disponible en:** http://localhost:8080

---

## 📖 Documentación Completa

- **Guía Bloque 1**: `docs/BLOQUE1_GUIA_COMPLETA.md` - Fundamentos y configuración
- **Guía Bloque 2**: `docs/BLOQUE2_GUIA_COMPLETA.md` - Transformaciones y análisis ✨ NUEVO
- **Troubleshooting**: Ver sección "Consideraciones Técnicas Críticas" en Bloque 1

---

## 🧪 API REST - Endpoints Disponibles

### 📌 Bloque 1: Exploración Básica de Datos

#### Health Check
```bash
curl http://localhost:8080/api/data/health
```

#### Listar Datos (Transacciones, Productos, Clientes)
```bash
curl http://localhost:8080/api/data/transactions?limit=5
curl http://localhost:8080/api/data/products?limit=5
curl http://localhost:8080/api/data/customers?limit=5
```

---

### 📊 Bloque 2: Análisis de Negocio

#### 1. Ventas por Categoría
```bash
curl http://localhost:8080/api/sales/by-category
```

**Respuesta:**
```json
[
    {
        "category": "Electronics",
        "totalSales": 2314.83,
        "totalQuantity": 17,
        "avgAmount": 210.44,
        "transactionCount": 11
    }
]
```

**Operaciones Spark:** JOIN (transactions + products), groupBy, agg(sum, avg, count)

#### 2. Top Productos Más Vendidos
```bash
curl "http://localhost:8080/api/products/top-selling?limit=10"
```

**Respuesta:**
```json
[
    {
        "productId": "PROD011",
        "productName": "Gaming Chair",
        "category": "Furniture",
        "totalSales": 799.99,
        "quantity": 1,
        "rank": 1
    }
]
```

**Operaciones Spark:** Window Functions (row_number), JOIN, groupBy, orderBy

#### 3. Estadísticas Generales
```bash
curl http://localhost:8080/api/sales/statistics
```

**Respuesta:**
```json
{
    "totalRevenue": 4049.61,
    "avgTicket": 202.48,
    "maxTransaction": 799.99,
    "minTransaction": 29.98,
    "totalTransactions": 20,
    "uniqueCustomers": 17
}
```

**Operaciones Spark:** Múltiples agregaciones (sum, avg, max, min, count, countDistinct)

#### 4. Ventas por Región
```bash
# Sin filtros
curl http://localhost:8080/api/sales/by-region

# Con filtros de fecha
curl "http://localhost:8080/api/sales/by-region?startDate=2024-10-01&endDate=2024-10-31"
```

**Operaciones Spark:** Filtrado dinámico, groupBy, countDistinct

#### 5. Resumen Diario de Ventas
```bash
curl "http://localhost:8080/api/sales/daily-summary?startDate=2024-10-01&endDate=2024-10-02"
```

**Respuesta:**
```json
[
    {
        "date": "2024-10-01",
        "totalSales": 1464.80,
        "transactionCount": 10,
        "avgTicket": 146.48,
        "uniqueCustomers": 9
    }
]
```

**Operaciones Spark:** Funciones de fecha (to_date), filtrado por rango, groupBy

#### 6. Productos por Categoría con Ventas
```bash
curl http://localhost:8080/api/products/by-category/Electronics
```

**Operaciones Spark:** Filter, JOIN, groupBy, orderBy

#### 7. Analytics de Producto Específico
```bash
curl http://localhost:8080/api/products/PROD001/analytics
```

**Respuesta:**
```json
{
    "productId": "PROD001",
    "productName": "Wireless Mouse",
    "category": "Electronics",
    "price": 29.99,
    "stock": 150,
    "totalRevenue": 119.96,
    "totalQuantity": 4,
    "transactionCount": 2,
    "avgTicket": 59.98,
    "uniqueCustomers": 2
}
```

**Operaciones Spark:** Filter, JOIN, agregaciones específicas

---

### 🔬 Bloque 3: Optimización y Detección de Fraude ✨ NUEVO

#### 1. Caché de Datasets
```bash
# Cachear datasets en memoria para mejor performance
curl -X POST http://localhost:8080/api/optimization/cache

# Ver información de caché
curl http://localhost:8080/api/optimization/cache/info

# Liberar caché
curl -X DELETE http://localhost:8080/api/optimization/cache
```

**Operaciones Spark:** `persist(StorageLevel.MEMORY_AND_DISK)`, cache management

#### 2. Transacciones Enriquecidas con UDFs
```bash
curl "http://localhost:8080/api/optimization/transactions/enriched?limit=3"
```

**Respuesta:**
```json
[
    {
        "transactionId": "TXN001",
        "amount": 59.98,
        "quantity": 2,
        "fraudRisk": "BAJO_RIESGO",
        "amountCategory": "MEDIO",
        "discountPct": 5.0,
        "amountWithDiscount": 56.98
    }
]
```

**UDFs Aplicadas:**
- `detect_fraud`: Categoriza riesgo (BAJO/MEDIO/ALTO_RIESGO)
- `categorize_amount`: Clasifica monto (BAJO/MEDIO/ALTO/MUY_ALTO)
- `calculate_discount`: Calcula descuento basado en monto

#### 3. Detección de Fraude
```bash
# Detectar transacciones sospechosas
curl "http://localhost:8080/api/fraud/detect?stdDevThreshold=2.0&limit=10"

# Detectar y guardar en PostgreSQL
curl -X POST "http://localhost:8080/api/fraud/detect-and-save?stdDevThreshold=2.5"

# Ver estadísticas de fraude
curl http://localhost:8080/api/fraud/statistics
```

**Respuesta (detect):**
```json
[
    {
        "transactionId": "TXN013",
        "amount": 799.99,
        "quantity": 1,
        "deviation": 2.93,
        "isOutlier": true,
        "fraudRisk": "BAJO_RIESGO",
        "amountCategory": "MUY_ALTO"
    }
]
```

**Algoritmo:** Detección de outliers usando desviación estándar (Z-score)

#### 4. Análisis de Patrones de Fraude
```bash
# Patrones por cliente
curl "http://localhost:8080/api/fraud/customer-patterns?stdDevThreshold=2.0&limit=5"

# Patrones por producto
curl "http://localhost:8080/api/fraud/product-patterns?stdDevThreshold=2.0&limit=5"

# Detectar duplicados sospechosos
curl "http://localhost:8080/api/fraud/duplicates?limit=10"
```

#### 5. Gestión de Alertas de Fraude
```bash
# Obtener todas las alertas
curl http://localhost:8080/api/fraud/alerts

# Obtener alertas de alto riesgo
curl http://localhost:8080/api/fraud/alerts/high-risk

# Marcar alerta como revisada
curl -X PUT http://localhost:8080/api/fraud/alerts/1/review
```

#### 6. Broadcast Joins Optimizados
```bash
curl "http://localhost:8080/api/optimization/transactions/broadcast-join?limit=5"
```

**Operación:** Join optimizado usando `broadcast()` para tablas pequeñas (productos)

#### 7. Persistencia PostgreSQL
```bash
# Ver estadísticas de base de datos
curl http://localhost:8080/api/persistence/stats

# Obtener reportes guardados
curl "http://localhost:8080/api/persistence/reports?startDate=2024-10-01&endDate=2024-10-31"

# Top productos por revenue
curl "http://localhost:8080/api/persistence/products/top-revenue?limit=10"
```

**Operaciones:** Escritura JDBC (Spark → PostgreSQL) y lectura JPA (PostgreSQL → Spring Boot)

---

## 📊 Datasets de Ejemplo

Ubicación: `./data/`

- **transactions.csv**: 20 transacciones de ejemplo
- **products.csv**: 17 productos con categorías
- **customers.csv**: 17 clientes con información demográfica

---

## 🏗️ Estructura del Proyecto

```
ecommerce-analytics/
├── docker-compose.yml              # Infraestructura Spark + PostgreSQL
├── pom.xml                         # Dependencias Maven
├── start.sh                        # Script de inicio automatizado
├── data/
│   ├── transactions.csv            # Dataset transacciones
│   ├── products.csv                # Dataset productos
│   └── customers.csv               # Dataset clientes
├── docs/
│   ├── BLOQUE1_GUIA_COMPLETA.md    # Guía completa Bloque 1
│   ├── BLOQUE2_GUIA_COMPLETA.md    # Guía completa Bloque 2
│   └── BLOQUE3_GUIA_COMPLETA.md    # Guía completa Bloque 3 ✨ NUEVO
├── BLOQUE3_RESUMEN.md              # Resumen ejecutivo Bloque 3 ✨ NUEVO
├── src/main/java/com/ecommerce/analytics/
│   ├── EcommerceAnalyticsApplication.java    # Main class
│   ├── config/
│   │   └── SparkConfig.java                  # Configuración SparkSession
│   ├── udf/                                  # ✨ NUEVO - Bloque 3
│   │   └── CustomUDFs.java                   # 5 UDFs personalizadas
│   ├── entity/                               # ✨ NUEVO - Bloque 3
│   │   ├── SalesReportEntity.java            # Entidad JPA reportes
│   │   ├── FraudAlertEntity.java             # Entidad JPA alertas fraude
│   │   └── ProductPerformanceEntity.java     # Entidad JPA métricas productos
│   ├── repository/                           # ✨ NUEVO - Bloque 3
│   │   ├── SalesReportRepository.java        # Repositorio Spring Data JPA
│   │   ├── FraudAlertRepository.java         # Repositorio alertas fraude
│   │   └── ProductPerformanceRepository.java # Repositorio métricas
│   ├── model/                                # DTOs Bloque 2
│   │   ├── SalesByCategory.java              # DTO ventas por categoría
│   │   ├── TopProduct.java                   # DTO productos más vendidos
│   │   ├── DailySalesSummary.java            # DTO resumen diario
│   │   └── SalesByRegion.java                # DTO ventas por región
│   ├── service/
│   │   ├── DataReaderService.java            # Servicio lectura de datos
│   │   ├── AnalyticsService.java             # Análisis y agregaciones (Bloque 2)
│   │   ├── OptimizationService.java          # Caché y optimización ✨ NUEVO
│   │   ├── FraudDetectionService.java        # Detección de fraude ✨ NUEVO
│   │   └── PersistenceService.java           # Persistencia PostgreSQL ✨ NUEVO
│   └── controller/
│       ├── DataExplorationController.java    # REST endpoints Bloque 1
│       ├── SalesAnalyticsController.java     # Endpoints ventas (Bloque 2)
│       ├── ProductAnalyticsController.java   # Endpoints productos (Bloque 2)
│       ├── OptimizationController.java       # Endpoints optimización ✨ NUEVO
│       ├── FraudDetectionController.java     # Endpoints fraude ✨ NUEVO
│       └── PersistenceController.java        # Endpoints persistencia ✨ NUEVO
└── src/main/resources/
    ├── application.yml               # Configuración base
    ├── application-local.yml         # Perfil local
    └── application-docker.yml        # Perfil Docker
```

---

## 🔑 Conceptos Clave Implementados

### Bloque 1: Fundamentos

#### 1. SparkSession como Bean de Spring
- Configuración en `SparkConfig.java`
- Perfiles separados: `local` y `docker`
- Inyección de dependencias con `@Autowired`

#### 2. Lectura de Datos CSV
- Headers automáticos con `option("header", "true")`
- Inferencia de schema con `option("inferSchema", "true")`
- DataFrames tipados como `Dataset<Row>`

#### 3. Operaciones Básicas de Spark
- `count()`: Contar registros (acción)
- `show()`: Mostrar datos (acción)
- `printSchema()`: Ver estructura de datos
- **Lazy Evaluation**: Las transformaciones no se ejecutan hasta una acción

#### 4. Integración Spring Boot + Spark
- Servicio `DataReaderService` con inyección de SparkSession
- Endpoints REST que exponen resultados de Spark
- Conversión de DataFrames a JSON para APIs

### Bloque 2: Transformaciones y Análisis

#### 1. Transformaciones Avanzadas
- `select()`: Selección de columnas
- `filter()`: Filtrado de datos
- `orderBy()`: Ordenamiento con `asc()` y `desc()`
- Alias de columnas con `alias()`

#### 2. Agregaciones de Negocio
- `groupBy()`: Agrupación por una o más columnas
- `agg()`: Múltiples agregaciones en una operación
- Funciones: `sum()`, `avg()`, `count()`, `countDistinct()`, `max()`, `min()`

#### 3. Joins entre Datasets
- `join()`: Inner join por defecto
- Join entre `transactions`, `products` y `customers`
- Enriquecimiento de datos con información relacionada

#### 4. Window Functions
- `Window.orderBy()`: Definición de ventana
- `row_number()`: Ranking de productos
- Uso de `.over(windowSpec)` para aplicar función

#### 5. Filtrado Dinámico
- Filtros opcionales con `@RequestParam(required = false)`
- Filtrado por rango de fechas con `.between()`
- Funciones de fecha: `to_date()`, `date_format()`

#### 6. Conversión Dataset<Row> a DTOs
- Uso de `collectAsList()` para materializar resultados
- Streams de Java para mapear `Row` a POJOs
- Lombok para reducir boilerplate en DTOs

### Bloque 3: Procesamiento Avanzado y Optimización ✨ NUEVO

#### 1. User Defined Functions (UDFs)
- 5 UDFs personalizadas implementadas como clases `Serializable`
- `ValidateEmail`: Validación de emails con regex
- `CategorizeAmount`: Clasificación de montos (BAJO/MEDIO/ALTO/MUY_ALTO)
- `DetectFraud`: Detección básica de fraude (BAJO/MEDIO/ALTO_RIESGO)
- `NormalizeString`: Normalización de texto (uppercase, trim)
- `CalculateDiscount`: Cálculo de descuentos progresivos
- Registro dinámico de UDFs con `spark.udf().register()`

#### 2. Optimización con Caché
- `persist(StorageLevel.MEMORY_AND_DISK)`: Caché híbrido
- Gestión de ciclo de vida del caché (cache/unpersist)
- Mejora de performance: ~10x más rápido en queries repetitivos
- Monitoreo de datasets cacheados

#### 3. Broadcast Joins
- Optimización de joins con tablas pequeñas usando `broadcast()`
- Reducción de shuffle en el cluster
- Mejora significativa en performance para joins dimensionales

#### 4. Detección de Fraude con Machine Learning Básico
- **Algoritmo**: Detección de outliers usando Z-score (desviación estándar)
- **Criterios**:
  - Monto > umbral de desviaciones estándar (configurable)
  - Cantidad excesiva de unidades (> 10)
  - Combinación de factores de riesgo
- **Análisis de patrones**:
  - Agrupación por cliente
  - Agrupación por producto
  - Detección de duplicados sospechosos

#### 5. Persistencia Bidireccional PostgreSQL
- **Escritura (Spark → PostgreSQL)**:
  - JDBC con `df.write().format("jdbc")`
  - SaveMode configurable (Append, Overwrite, ErrorIfExists)
- **Lectura (PostgreSQL → Spring Boot)**:
  - Spring Data JPA con repositorios
  - Queries personalizadas con @Query
- **Entidades JPA**:
  - SalesReportEntity: Reportes de ventas agregados
  - FraudAlertEntity: Alertas de fraude detectadas
  - ProductPerformanceEntity: Métricas de productos
- **Conversión bidireccional**: Dataset<Row> ↔ Entity

#### 6. Limpieza y Validación de Datos
- Eliminación de nulls con `na.drop()`
- Eliminación de duplicados con `dropDuplicates()`
- Validación de datos con UDFs antes del procesamiento

---

## 🎯 Ejercicios Prácticos del Bloque 1

### Ejercicio 1: Explorar Productos
```bash
# Ver primeras 20 filas de productos
curl http://localhost:8080/api/data/products?limit=20

# Observar el schema inferido
# Contar total de productos
```

### Ejercicio 2: Entender Lazy Evaluation
Agregar logging en `DataReaderService.java` para ver cuándo se ejecutan las operaciones:
```java
System.out.println("Leyendo CSV..."); // Se ejecuta inmediatamente
Dataset<Row> df = sparkSession.read()...
System.out.println("DataFrame creado"); // Aún no se leyó el archivo

long count = df.count(); // AQUÍ se ejecuta la lectura
System.out.println("Archivo leído y contado: " + count);
```

---

## 📝 Configuración de Perfiles

### Perfil Local (`application-local.yml`)
- Spark Master: `local[*]` (todos los cores locales)
- PostgreSQL: `localhost:5432`
- Path de datos: `./data`

### Perfil Docker (`application-docker.yml`)
- Spark Master: `spark://spark-master:7077`
- PostgreSQL: `postgres:5432`
- Path de datos: `/data`

---

## 🐳 Comandos Útiles de Docker

```bash
# Ver logs del cluster Spark
docker logs spark-master
docker logs spark-worker-1

# Detener servicios
docker-compose down

# Reiniciar servicios
docker-compose restart

# Eliminar todo (incluyendo volúmenes)
docker-compose down -v
```

---

## ✅ Checklist de Verificación Bloque 1

- [x] Proyecto Spring Boot creado con Maven
- [x] Dependencias de Spark agregadas
- [x] Docker Compose configurado (Spark + PostgreSQL)
- [x] SparkSession configurado como Bean
- [x] Perfiles `local` y `docker` funcionando
- [x] Datasets CSV creados
- [x] Servicio de lectura de datos implementado
- [x] Endpoints REST expuestos
- [x] Health check funcionando
- [x] Primera lectura exitosa de datos

---

## 🎓 Conceptos Aprendidos

1. **Arquitectura Spark**: Driver, Executors, Cluster Manager
2. **RDD vs DataFrame vs Dataset**: Diferencias y cuándo usar cada uno
3. **Lazy Evaluation**: Transformaciones vs Acciones
4. **SparkSession**: Punto de entrada para trabajar con Spark
5. **Integración Spring Boot**: Inyección de dependencias y configuración por perfiles

---

## 🔜 Próximos Pasos: Bloque 4

En el siguiente bloque implementaremos:
- Jobs programados (Batch Processing)
- ETL Pipelines automatizados
- Integración con sistemas externos
- Procesamiento de streams (Spark Streaming)
- Machine Learning avanzado con MLlib

---

## 🆘 Troubleshooting

Para problemas de compatibilidad y errores comunes, consulta la **guía completa de troubleshooting** en:
📖 `docs/CONFIGURACION_CORRECTA.md`

### Errores Comunes:

#### ❌ Java 18+ Incompatibilidad
```
Error: getSubject is supported only if a security manager is allowed
```
**Solución**: Usar Java 17. Ver instrucciones de instalación arriba.

#### ❌ ANTLR Version Conflict (CRÍTICO)
```
Error: Could not deserialize ATN with version 4 (expected 3)
```
**Causa**: Spring Boot 3.x usa Hibernate con ANTLR 4.10+, incompatible con Spark 3.5.0 que requiere ANTLR 4.9.3.

**Solución Aplicada**:
1. **Downgrade Spring Boot**: 3.2.0 → 2.7.18
2. **Imports JPA**: `jakarta.persistence.*` → `javax.persistence.*`
3. **Exclusiones ANTLR**: Agregadas en `pom.xml` para `spring-boot-starter-data-jpa`
4. **Versión forzada**: ANTLR 4.9.3 en `<dependencyManagement>`

**Verificación**:
```bash
mvn clean compile
# Debe compilar sin errores
```

#### ❌ Puerto 8080 en uso
```bash
# Matar proceso en puerto 8080
lsof -ti:8080 | xargs kill -9

# O cambiar puerto en application.yml
server:
  port: 8081
```

#### ❌ PostgreSQL no conecta
```bash
# Verificar contenedor
docker ps | grep postgres

# Reiniciar PostgreSQL
docker-compose restart postgres

# Ver logs
docker logs postgres-db
```

#### ❌ Module Access Errors (Java 17)
**Error:**
```
IllegalAccessError: class org.apache.spark.storage.StorageUtils$ cannot access
class sun.nio.ch.DirectBuffer
```

**Solución:**
- Si ejecutas con Maven: Los JVM arguments se aplican automáticamente desde `pom.xml`
- Si ejecutas el JAR: Debes agregar los argumentos `--add-opens` manualmente (ver comando arriba)
- Solución rápida: Usa el script `./start.sh`

---

## 📚 Recursos Adicionales

### Documentación del Proyecto
- 📖 **PRD Completo**: `PRD.md` - Product Requirements Document
- 📘 **Guía Bloque 1**: `docs/BLOQUE1_GUIA_COMPLETA.md` - Fundamentos y configuración
- 📗 **Guía Bloque 2**: `docs/BLOQUE2_GUIA_COMPLETA.md` - Transformaciones y análisis
- 📙 **Guía Bloque 3**: `docs/BLOQUE3_GUIA_COMPLETA.md` - Optimización y persistencia ✨ NUEVO
- 📄 **Resumen Bloque 3**: `BLOQUE3_RESUMEN.md` - Resumen ejecutivo ✨ NUEVO
- 🔧 **Troubleshooting**: Ver sección "Consideraciones Técnicas Críticas" y ANTLR conflict arriba

### Documentación Oficial
- [Apache Spark 3.5.0 Documentation](https://spark.apache.org/docs/3.5.0/)
- [Spring Boot 2.7.18 Reference](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)
- [Spark SQL Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

---

**Versión:** 3.0 - Bloque 3 Completado - Procesamiento Avanzado y Optimización ✨
**Fecha:** Octubre 2025
**Última Actualización:** Octubre 2025 - Bloque 3: UDFs, Optimización, Fraude, Persistencia PostgreSQL

## 📝 Changelog

### **v3.0 - Bloque 3 Completado** (Actual)
- ✅ **Bloque 3 Implementado y Verificado**: Procesamiento avanzado y optimización
- ✅ 5 UDFs personalizadas (ValidateEmail, CategorizeAmount, DetectFraud, etc.)
- ✅ Optimización con caché (persist) y broadcast joins
- ✅ Sistema de detección de fraude con análisis estadístico (Z-score)
- ✅ Persistencia bidireccional PostgreSQL (Spark ↔ Spring Data JPA)
- ✅ 3 entidades JPA (SalesReport, FraudAlert, ProductPerformance)
- ✅ 3 repositorios Spring Data JPA con queries personalizadas
- ✅ 20 endpoints REST nuevos (optimización, fraude, persistencia)
- ✅ **Fix crítico ANTLR**: Spring Boot downgrade 3.2.0 → 2.7.18
- ✅ **Fix imports**: jakarta.persistence → javax.persistence
- ✅ **Fix UDF types**: Long → Integer para quantity field (Spark compatibility)
- ✅ **Fix timestamp casting**: Timestamp → String para transactionDate
- ✅ Documentación completa en `BLOQUE3_GUIA_COMPLETA.md` y `BLOQUE3_RESUMEN.md`
- ✅ Todos los endpoints verificados y funcionando (24/24 endpoints)

### **v2.0 - Bloque 2 Completado**
- ✅ **Bloque 2 Completado**: Transformaciones y análisis de datos
- ✅ 7 nuevos endpoints REST (ventas y productos)
- ✅ Agregaciones avanzadas (groupBy, sum, avg, count, etc.)
- ✅ Joins entre datasets (transactions + products + customers)
- ✅ Window Functions para rankings
- ✅ Filtrado dinámico por fechas y categorías
- ✅ DTOs con Lombok para responses estructurados
- ✅ Documentación completa en `BLOQUE2_GUIA_COMPLETA.md`

### **v1.1 - Mejoras de Documentación**
- ✅ Agregada sección Quick Start con script automatizado (`start.sh`)
- ✅ Comandos de ejecución validados (Maven y JAR)
- ✅ Documentación de JVM arguments obligatorios para Java 17
- ✅ Enlaces a documentación completa y troubleshooting

### **v1.0 - Bloque 1 Inicial**
- ✅ Setup inicial con Spark + Spring Boot
- ✅ Lectura de CSVs y API REST básica
