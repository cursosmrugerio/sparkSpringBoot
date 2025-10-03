# 📚 Bloque 1: Fundamentos de Spark y Configuración - Guía Completa

## 🎯 Objetivo del Bloque 1

Aprender los fundamentos de Apache Spark y configurar un ambiente de desarrollo completo integrando Spark con Spring Boot, para realizar el primer procesamiento de datos.

---

## 📖 Índice

1. [¿Qué es Apache Spark?](#1-qué-es-apache-spark)
2. [Arquitectura de Spark](#2-arquitectura-de-spark)
3. [Conceptos Clave](#3-conceptos-clave)
4. [Configuración del Proyecto](#4-configuración-del-proyecto)
5. [Integración Spring Boot + Spark](#5-integración-spring-boot--spark)
6. [Primera Lectura de Datos](#6-primera-lectura-de-datos)
7. [API REST para Consultas](#7-api-rest-para-consultas)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. ¿Qué es Apache Spark?

### Definición

**Apache Spark** es un motor de procesamiento de datos distribuido y de alto rendimiento diseñado para analizar grandes volúmenes de datos de manera rápida y eficiente.

### ¿Por qué Spark?

| Característica | Procesamiento Tradicional | Apache Spark |
|---------------|---------------------------|--------------|
| **Velocidad** | Lento (disco) | 100x más rápido (memoria) |
| **Escalabilidad** | Limitada | Distribuida en cluster |
| **Complejidad** | Alto código boilerplate | API simple y expresiva |
| **Lenguajes** | Generalmente uno | Java, Scala, Python, R, SQL |

### Casos de Uso Reales

- **E-commerce**: Análisis de comportamiento de compra, recomendaciones
- **Finanzas**: Detección de fraude en tiempo real
- **Telecomunicaciones**: Análisis de logs de red
- **Salud**: Procesamiento de datos médicos masivos
- **IoT**: Análisis de datos de sensores

---

## 2. Arquitectura de Spark

### Componentes Principales

```
┌─────────────────────────────────────────────────────┐
│                   SPARK APPLICATION                  │
│                                                      │
│  ┌────────────────────────────────────────────┐    │
│  │         DRIVER PROGRAM                      │    │
│  │  - SparkContext/SparkSession                │    │
│  │  - Planificación de tareas                  │    │
│  │  - Coordinación                             │    │
│  └─────────────┬───────────────────────────────┘    │
│                │                                      │
│                │ Comunica con                         │
│                ▼                                      │
│  ┌────────────────────────────────────────────┐    │
│  │         CLUSTER MANAGER                     │    │
│  │  - Asignación de recursos                   │    │
│  │  - Gestión de workers                       │    │
│  └─────────────┬───────────────────────────────┘    │
│                │                                      │
│                │ Distribuye trabajo a                 │
│                ▼                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │            EXECUTORS (Workers)                │  │
│  │  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐    │  │
│  │  │Task 1│  │Task 2│  │Task 3│  │Task 4│    │  │
│  │  └──────┘  └──────┘  └──────┘  └──────┘    │  │
│  │         Procesan datos en paralelo           │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Explicación de Componentes

#### 1. **Driver Program**
- **Función**: Cerebro de la aplicación Spark
- **Responsabilidades**:
  - Crear el `SparkSession` (punto de entrada)
  - Convertir código en plan de ejecución
  - Distribuir tareas a los executors
  - Recolectar resultados

#### 2. **Cluster Manager**
- **Función**: Gestor de recursos del cluster
- **Tipos**:
  - **Local**: Para desarrollo (1 máquina)
  - **Standalone**: Cluster Spark nativo
  - **YARN**: Hadoop cluster manager
  - **Mesos**: Apache Mesos
  - **Kubernetes**: Orquestación de contenedores

#### 3. **Executors**
- **Función**: Workers que ejecutan tareas
- **Responsabilidades**:
  - Ejecutar código en paralelo
  - Almacenar datos en memoria/disco
  - Reportar estado al Driver

---

## 3. Conceptos Clave

### 3.1 RDD (Resilient Distributed Dataset)

**API de bajo nivel** (raramente usado hoy)

```java
// Ejemplo RDD (NO recomendado - solo para entender)
JavaRDD<String> lines = sparkContext.textFile("data.txt");
```

### 3.2 DataFrame

**API de alto nivel** (recomendado) - Similar a una tabla SQL

```java
// DataFrame: Datos estructurados con schema
Dataset<Row> df = sparkSession.read()
    .option("header", "true")
    .csv("transactions.csv");

df.show(5);  // Muestra primeras 5 filas
```

**Ventajas de DataFrames:**
- ✅ Optimización automática de queries
- ✅ Schema conocido (tipos de datos)
- ✅ Compatible con SQL
- ✅ API similar a Pandas (Python) o dplyr (R)

### 3.3 Dataset<T>

DataFrame **tipado** (type-safe)

```java
// Dataset tipado con clase Transaction
Dataset<Transaction> transactions = df.as(Encoders.bean(Transaction.class));
```

### 3.4 Lazy Evaluation (Evaluación Perezosa)

**Concepto clave**: Spark NO ejecuta nada hasta que lo solicitas explícitamente.

```java
// TRANSFORMACIONES (lazy - no ejecutan nada todavía)
Dataset<Row> filtered = df.filter("amount > 100");    // ❌ NO ejecuta
Dataset<Row> sorted = filtered.orderBy("amount");     // ❌ NO ejecuta

// ACCIONES (eager - ejecutan TODO el plan)
long count = sorted.count();                          // ✅ EJECUTA TODO
sorted.show();                                        // ✅ EJECUTA TODO
```

**¿Por qué es importante?**
- Spark optimiza TODO el plan antes de ejecutar
- Evita cálculos innecesarios
- Mejora drásticamente el rendimiento

#### Tipos de Operaciones

| Transformaciones (Lazy) | Acciones (Eager) |
|-------------------------|------------------|
| `select()` | `count()` |
| `filter()` | `show()` |
| `groupBy()` | `collect()` |
| `orderBy()` | `save()` |
| `join()` | `take()` |

---

## 4. Configuración del Proyecto

### 4.1 Estructura del Proyecto

```
ecommerce-analytics/
├── pom.xml                           # Dependencias Maven
├── docker-compose.yml                # Infraestructura (Spark + PostgreSQL)
├── README.md                         # Documentación general
├── data/                             # Datasets de ejemplo
│   ├── transactions.csv              # 20 transacciones
│   ├── products.csv                  # 17 productos
│   └── customers.csv                 # 17 clientes
├── src/
│   └── main/
│       ├── java/com/ecommerce/analytics/
│       │   ├── EcommerceAnalyticsApplication.java    # Main class
│       │   ├── config/
│       │   │   └── SparkConfig.java                  # Configuración Spark
│       │   ├── service/
│       │   │   └── DataReaderService.java            # Lectura de datos
│       │   └── controller/
│       │       └── DataExplorationController.java    # REST endpoints
│       └── resources/
│           ├── application.yml                       # Config base
│           ├── application-local.yml                 # Config local
│           └── application-docker.yml                # Config Docker
└── docs/
    └── BLOQUE1_GUIA_COMPLETA.md                     # Este documento
```

### 4.2 Dependencias Maven (pom.xml)

```xml
<properties>
    <java.version>17</java.version>
    <spark.version>3.5.0</spark.version>
    <scala.binary.version>2.12</scala.binary.version>
</properties>

<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Apache Spark Core -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_${scala.binary.version}</artifactId>
        <version>${spark.version}</version>
        <exclusions>
            <!-- Evitar conflictos de logging -->
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-reload4j</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Apache Spark SQL -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql_${scala.binary.version}</artifactId>
        <version>${spark.version}</version>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**Explicación de versiones:**
- `spark-core`: Motor principal de Spark
- `spark-sql`: API de DataFrames y SQL
- `_2.12`: Versión de Scala (Spark está escrito en Scala)
- `3.5.0`: Versión de Spark

### 4.3 Docker Compose (Infraestructura)

```yaml
services:
  # Base de datos PostgreSQL
  postgres:
    image: postgres:15
    container_name: postgres-db
    environment:
      - POSTGRES_USER=sparkuser
      - POSTGRES_PASSWORD=sparkpass
      - POSTGRES_DB=ecommerce_analytics
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

**¿Por qué PostgreSQL?**
- Almacenar resultados procesados por Spark
- Persistencia de reportes y agregaciones
- Integración con Spring Data JPA

---

## 5. Integración Spring Boot + Spark

### 5.1 Configuración de SparkSession

**Archivo**: `src/main/java/com/ecommerce/analytics/config/SparkConfig.java`

```java
@Configuration
public class SparkConfig {

    @Value("${spark.app.name}")
    private String appName;

    @Bean
    @Profile("local")
    public SparkSession sparkSessionLocal() {
        // Configuración para Java 17+
        System.setProperty("HADOOP_HOME", "/tmp/hadoop");
        System.setProperty("hadoop.home.dir", "/tmp/hadoop");

        return SparkSession.builder()
                .appName(appName)
                .master("local[*]")  // Usar todos los cores locales
                .config("spark.driver.host", "localhost")
                .config("spark.sql.shuffle.partitions", "4")
                .config("spark.hadoop.fs.defaultFS", "file:///")
                .config("spark.sql.warehouse.dir", "/tmp/spark-warehouse")
                .getOrCreate();
    }
}
```

**Desglose de la configuración:**

| Configuración | Significado |
|---------------|-------------|
| `appName` | Nombre de la aplicación Spark |
| `master("local[*]")` | Modo local, usar todos los cores CPU |
| `spark.driver.host` | Host del driver (localhost para local) |
| `spark.sql.shuffle.partitions` | Particiones para shuffles (default: 200) |
| `spark.hadoop.fs.defaultFS` | Sistema de archivos (local) |
| `spark.sql.warehouse.dir` | Directorio para tablas temporales |

**¿Por qué `@Profile("local")`?**
```java
@Profile("local")   // Se activa con: --spring.profiles.active=local
@Profile("docker")  // Se activa con: --spring.profiles.active=docker
```

Permite tener diferentes configuraciones según el ambiente.

### 5.2 Archivos de Configuración

#### application.yml (Base)
```yaml
spring:
  application:
    name: ecommerce-analytics
  profiles:
    active: local

spark:
  app:
    name: ${spring.application.name}

logging:
  level:
    com.ecommerce.analytics: INFO
    org.apache.spark: WARN
```

#### application-local.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_analytics
    username: sparkuser
    password: sparkpass

spark:
  master: local[*]
  data:
    path: ./data  # Ruta relativa a los CSVs
```

#### application-docker.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/ecommerce_analytics
    username: sparkuser
    password: sparkpass

spark:
  master: spark://spark-master:7077  # Cluster Spark
  data:
    path: /data
```

---

## 6. Primera Lectura de Datos

### 6.1 Servicio de Lectura de Datos

**Archivo**: `src/main/java/com/ecommerce/analytics/service/DataReaderService.java`

```java
@Service
public class DataReaderService {

    @Autowired
    private SparkSession sparkSession;

    @Value("${spark.data.path}")
    private String dataPath;

    /**
     * Lee archivo CSV de transacciones
     * Demuestra: lectura básica, inferencia de schema, headers
     */
    public Dataset<Row> readTransactions() {
        String filePath = dataPath + "/transactions.csv";

        return sparkSession.read()
                .option("header", "true")       // Primera fila = nombres columnas
                .option("inferSchema", "true")  // Detectar tipos automáticamente
                .csv(filePath);
    }

    /**
     * Lee archivo CSV de productos
     */
    public Dataset<Row> readProducts() {
        String filePath = dataPath + "/products.csv";

        return sparkSession.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(filePath);
    }

    /**
     * Muestra información básica de un DataFrame
     */
    public void showDatasetInfo(Dataset<Row> dataset, String datasetName) {
        System.out.println("=== Dataset: " + datasetName + " ===");
        System.out.println("Total de registros: " + dataset.count());

        System.out.println("\nSchema:");
        dataset.printSchema();

        System.out.println("\nPrimeras 5 filas:");
        dataset.show(5);
    }
}
```

### 6.2 Operaciones Básicas de Spark

#### Operación 1: Leer CSV

```java
Dataset<Row> df = sparkSession.read()
    .option("header", "true")       // Primera fila contiene nombres
    .option("inferSchema", "true")  // Detectar tipos (int, string, etc.)
    .csv("transactions.csv");
```

**¿Qué hace `inferSchema`?**
```
transaction_id,amount,quantity
TXN001,59.98,2        → amount: double, quantity: int
TXN002,129.99,1       → Spark detecta tipos automáticamente
```

#### Operación 2: Ver Schema

```java
df.printSchema();
```

**Output:**
```
root
 |-- transaction_id: string (nullable = true)
 |-- customer_id: string (nullable = true)
 |-- product_id: string (nullable = true)
 |-- quantity: integer (nullable = true)
 |-- amount: double (nullable = true)
 |-- transaction_date: timestamp (nullable = true)
 |-- payment_method: string (nullable = true)
 |-- region: string (nullable = true)
```

#### Operación 3: Mostrar Datos

```java
df.show(5);  // Muestra 5 filas
```

**Output:**
```
+--------------+-----------+----------+--------+------+-------------------+--------------+------+
|transaction_id|customer_id|product_id|quantity|amount|   transaction_date|payment_method|region|
+--------------+-----------+----------+--------+------+-------------------+--------------+------+
|        TXN001|   CUST101|  PROD001 |       2| 59.98|2024-10-01 10:30:00|   credit_card| North|
|        TXN002|   CUST102|  PROD002 |       1|129.99|2024-10-01 11:15:00|    debit_card| South|
+--------------+-----------+----------+--------+------+-------------------+--------------+------+
```

#### Operación 4: Contar Registros

```java
long count = df.count();  // ACCIÓN - Ejecuta todo el plan
System.out.println("Total registros: " + count);
```

---

## 7. API REST para Consultas

### 7.1 Controlador REST

**Archivo**: `src/main/java/com/ecommerce/analytics/controller/DataExplorationController.java`

```java
@RestController
@RequestMapping("/api/data")
public class DataExplorationController {

    @Autowired
    private DataReaderService dataReaderService;

    /**
     * GET /api/data/transactions?limit=10
     * Retorna transacciones en formato JSON
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(defaultValue = "10") int limit) {

        Dataset<Row> transactions = dataReaderService.readTransactions();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", transactions.count());
        response.put("schema", getSchemaInfo(transactions));
        response.put("data", getDataAsList(transactions, limit));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/data/health
     * Health check del sistema Spark
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("spark", "Running");
        response.put("message", "Spark integration is working correctly");

        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares para convertir DataFrame a JSON
    private List<String> getSchemaInfo(Dataset<Row> dataset) {
        return List.of(dataset.schema().fields()).stream()
                .map(field -> field.name() + ": " + field.dataType().simpleString())
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDataAsList(Dataset<Row> dataset, int limit) {
        return dataset.limit(limit)
                .collectAsList()  // ACCIÓN - trae datos al driver
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < row.size(); i++) {
                        map.put(row.schema().fields()[i].name(), row.get(i));
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }
}
```

### 7.2 Endpoints Disponibles

#### 1. Health Check
```bash
curl http://localhost:8080/api/data/health
```

**Respuesta:**
```json
{
  "status": "OK",
  "spark": "Running",
  "message": "Spark integration is working correctly"
}
```

#### 2. Listar Transacciones
```bash
curl http://localhost:8080/api/data/transactions?limit=5
```

**Respuesta:**
```json
{
  "totalRecords": 20,
  "schema": [
    "transaction_id: string",
    "customer_id: string",
    "product_id: string",
    "quantity: int",
    "amount: double"
  ],
  "data": [
    {
      "transaction_id": "TXN001",
      "customer_id": "CUST101",
      "product_id": "PROD001",
      "quantity": 2,
      "amount": 59.98
    }
  ]
}
```

#### 3. Listar Productos
```bash
curl http://localhost:8080/api/data/products?limit=5
```

#### 4. Listar Clientes
```bash
curl http://localhost:8080/api/data/customers?limit=5
```

---

## 8. Troubleshooting

### Problema 1: Error de Java Version

**Error:**
```
getSubject is supported only if a security manager is allowed
```

**Causa:** Spark 3.5.0 no es compatible con Java 18+

**Solución:**
```bash
# Instalar Java 17
sdk install java 17.0.9-tem

# Verificar versión
java -version  # Debe mostrar 17.x.x

# Ejecutar con Java 17
java -jar target/analytics-1.0.0.jar --spring.profiles.active=local
```

### Problema 2: Puerto 8080 en Uso

**Error:**
```
Port 8080 is already in use
```

**Solución:**
```yaml
# En application.yml
server:
  port: 8081
```

### Problema 3: No Encuentra Archivos CSV

**Error:**
```
FileNotFoundException: ./data/transactions.csv
```

**Solución:**
```bash
# Verificar que existe el directorio
ls -la data/

# Verificar ruta en application-local.yml
spark:
  data:
    path: ./data  # Ruta relativa desde donde se ejecuta la app
```

### Problema 4: PostgreSQL No Conecta

**Error:**
```
Connection refused: localhost:5432
```

**Solución:**
```bash
# Verificar que PostgreSQL está corriendo
docker ps | grep postgres

# Si no está corriendo
docker compose up -d postgres

# Verificar logs
docker logs postgres-db
```

---

## 📝 Ejercicios Prácticos

### Ejercicio 1: Explorar el Dataset

**Objetivo:** Familiarizarse con las operaciones básicas de Spark

```java
// 1. Leer dataset de productos
Dataset<Row> products = dataReaderService.readProducts();

// 2. Mostrar schema
products.printSchema();

// 3. Contar productos
long totalProducts = products.count();

// 4. Mostrar primeros 10
products.show(10);

// 5. Filtrar productos de categoría "Electronics"
Dataset<Row> electronics = products.filter("category = 'Electronics'");
electronics.show();
```

### Ejercicio 2: Crear Nuevo Endpoint

**Objetivo:** Crear endpoint para buscar productos por categoría

```java
@GetMapping("/products/category/{category}")
public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(
        @PathVariable String category) {

    Dataset<Row> products = dataReaderService.readProducts();
    Dataset<Row> filtered = products.filter(col("category").equalTo(category));

    List<Map<String, Object>> result = getDataAsList(filtered, 100);
    return ResponseEntity.ok(result);
}
```

**Probar:**
```bash
curl http://localhost:8080/api/data/products/category/Electronics
```

### Ejercicio 3: Agregar Logging

**Objetivo:** Entender lazy evaluation

```java
public Dataset<Row> readTransactions() {
    logger.info("Leyendo CSV..."); // Se ejecuta inmediatamente

    Dataset<Row> df = sparkSession.read()
        .option("header", "true")
        .csv(dataPath + "/transactions.csv");

    logger.info("DataFrame creado"); // Se ejecuta inmediatamente
    // PERO aún NO se ha leído el archivo

    long count = df.count(); // AQUÍ se ejecuta la lectura
    logger.info("Archivo leído, {} registros", count);

    return df;
}
```

---

## 🎓 Conceptos Clave Aprendidos

### ✅ Checklist de Aprendizaje

- [ ] Entiendo qué es Apache Spark y sus ventajas
- [ ] Conozco la arquitectura Driver-Executor
- [ ] Comprendo la diferencia entre RDD, DataFrame y Dataset
- [ ] Entiendo Lazy Evaluation (Transformaciones vs Acciones)
- [ ] Sé configurar SparkSession en Spring Boot
- [ ] Puedo leer archivos CSV con Spark
- [ ] Conozco operaciones básicas: count(), show(), printSchema()
- [ ] Sé exponer datos de Spark mediante REST API
- [ ] Entiendo perfiles de configuración (local vs docker)

### 🔑 Puntos Clave para Recordar

1. **Spark es Lazy**: No ejecuta nada hasta que haces `.count()`, `.show()`, etc.
2. **DataFrames son inmutables**: Cada transformación crea un nuevo DataFrame
3. **SparkSession es el punto de entrada**: Todo comienza con `sparkSession.read()`
4. **Perfiles de Spring Boot**: Permiten configuraciones diferentes por ambiente
5. **CSV Options importantes**:
   - `header=true`: Primera fila son nombres de columnas
   - `inferSchema=true`: Detecta tipos automáticamente

---

## ⚠️ CONSIDERACIONES TÉCNICAS CRÍTICAS

Esta sección documenta problemas de compatibilidad conocidos y sus soluciones validadas. **Es fundamental seguir estas instrucciones para evitar errores.**

### 🔴 Compatibilidad de Versiones

#### Java 17 - Requisito Estricto

- ✅ **Java 17**: Compatible y validado (usar 17.0.13)
- ❌ **Java 18+**: **INCOMPATIBLE** - Spark 3.5.0 usa Hadoop que requiere Security Manager (removido en Java 18+)

**Error típico con Java 18+:**
```
getSubject is supported only if a security manager is allowed
```

**Solución:**
```bash
# Instalar Java 17 con SDKMAN
sdk install java 17.0.13-tem
sdk use java 17.0.13-tem

# Verificar (debe mostrar 17.0.13)
java -version
```

#### Conflicto ANTLR

- **Problema**: Spring Boot 3.2.0 incluye ANTLR 4.13.1, pero Spark 3.5.0 requiere ANTLR 4.9.x
- **Síntoma**: `Could not deserialize ATN with version 3 (expected 4)`

**Solución (ya implementada en pom.xml):**
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

**Solución (ya implementada en pom.xml):**
```xml
<!-- Javax Servlet API (requerido por Spark 3.5.0) -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
</dependency>

<!-- Jersey para compatibilidad -->
<dependency>
    <groupId>org.glassfish.jersey.core</groupId>
    <artifactId>jersey-server</artifactId>
    <version>2.40</version>
</dependency>
```

### 🔧 Configuración Obligatoria para Java 17

#### JVM Arguments Requeridos

El módulo system de Java 17 requiere flags especiales para permitir acceso a módulos internos:

**En pom.xml (para `mvn spring-boot:run`):**
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/sun.nio.ch=ALL-UNNAMED
            --add-opens java.base/sun.util.calendar=ALL-UNNAMED
        </jvmArguments>
    </configuration>
</plugin>
```

**Para ejecutar JAR directamente:**
```bash
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
     -jar target/analytics-1.0.0.jar --spring.profiles.active=local
```

**Error sin estos argumentos:**
```
IllegalAccessError: class org.apache.spark.storage.StorageUtils$ cannot access
class sun.nio.ch.DirectBuffer because module java.base does not export sun.nio.ch
```

#### Spark UI Deshabilitada

- **Razón**: Conflictos irresolubles entre servlets de Spark UI y Spring Boot 3
- **Configuración**: `spark.ui.enabled=false` en SparkConfig.java
- **Impacto**: No habrá acceso a Spark UI web (http://localhost:4040)

```java
@Bean
@Profile("local")
public SparkSession sparkSessionLocal() {
    return SparkSession.builder()
            .config("spark.ui.enabled", "false")  // CRÍTICO: Evita conflictos
            .getOrCreate();
}
```

### 🚀 Comandos de Ejecución Validados

#### Opción 1: Maven (Desarrollo)
```bash
# Los JVM arguments se aplican automáticamente desde pom.xml
mvn clean spring-boot:run -Dspring-boot.run.profiles=local
```

#### Opción 2: JAR (Producción)
```bash
# 1. Compilar
mvn clean package -DskipTests

# 2. Ejecutar con JVM arguments
export JAVA_HOME=~/.sdkman/candidates/java/17.0.13-tem
export PATH=$JAVA_HOME/bin:$PATH

java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
     -jar target/analytics-1.0.0.jar \
     --spring.profiles.active=local
```

#### Opción 3: Script Automatizado (Recomendado)
```bash
# Usar el script de inicio incluido
./start.sh
```

### 🐛 Troubleshooting Común

#### Problema 1: Error de Módulos Java
**Error:**
```
IllegalAccessError: cannot access class sun.nio.ch.DirectBuffer
```
**Solución:** Usar los JVM arguments `--add-opens` (ver arriba)

#### Problema 2: ANTLR Version Mismatch
**Error:**
```
Could not deserialize ATN with version 3 (expected 4)
```
**Solución:** Verificar `<dependencyManagement>` en pom.xml fuerza ANTLR 4.9.3

#### Problema 3: NoClassDefFoundError javax.servlet
**Error:**
```
NoClassDefFoundError: javax/servlet/Servlet
```
**Solución:** Agregar dependencias `javax.servlet-api` y Jersey (ver pom.xml)

#### Problema 4: Puerto 8080 Ocupado
**Error:**
```
Port 8080 is already in use
```
**Solución:**
```bash
# Opción A: Liberar puerto
lsof -ti:8080 | xargs kill -9

# Opción B: Cambiar puerto en application.yml
server:
  port: 8081
```

#### Problema 5: No Encuentra CSVs
**Error:**
```
FileNotFoundException: ./data/transactions.csv
```
**Solución:**
```bash
# Verificar que el directorio existe
ls -la data/

# Verificar ruta en application-local.yml
spark:
  data:
    path: ./data  # Ruta relativa desde donde se ejecuta
```

---

## 🚀 Próximos Pasos: Bloque 2

En el siguiente bloque aprenderemos:

- ✨ **Transformaciones avanzadas**: select, filter, groupBy, orderBy
- 🔗 **Joins** entre datasets
- 📊 **Agregaciones complejas**: ventas por categoría, top productos
- 🔍 **Spark SQL**: Escribir queries SQL en Spark
- 🎯 **Window Functions**: Cálculos por ventanas de datos

---

## 📚 Referencias Adicionales

### Documentación Oficial
- [Spark SQL Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)
- [Spark Java API](https://spark.apache.org/docs/latest/api/java/index.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

### Cheat Sheets
- [Spark DataFrame Operations](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/Dataset.html)
- [Spark SQL Functions](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/functions.html)

---

**Versión del Documento:** 1.1
**Última Actualización:** Octubre 2025 - Agregada sección "Consideraciones Técnicas Críticas"
**Autor:** Equipo de Capacitación Spark

**Cambios en v1.1:**
- ✅ Agregada sección "Consideraciones Técnicas Críticas"
- ✅ Documentación de conflictos de dependencias (ANTLR, javax/jakarta)
- ✅ Instrucciones detalladas de ejecución con JVM arguments
- ✅ Troubleshooting de errores comunes validados
- ✅ Comandos de ejecución para Maven, JAR y script automatizado

---

## ❓ ¿Preguntas Frecuentes?

### ¿Por qué Java 17 y no Java 21?
Spark 3.5.0 tiene problemas de compatibilidad con Java 18+. Spark 4.0 (próximo) soportará Java 21.

### ¿Puedo usar Python en lugar de Java?
Sí, Spark tiene API para Python (PySpark), pero este curso se enfoca en Java + Spring Boot.

### ¿Es necesario Docker?
Para este bloque no es estrictamente necesario. Docker se usará más en bloques avanzados para simular clusters.

### ¿Qué pasa si no tengo 8GB de RAM?
Spark en modo local puede funcionar con 4GB, pero el rendimiento será más lento.

### ¿Por qué necesito los JVM arguments?
Java 17 tiene módulos restrictivos. Los `--add-opens` permiten a Spark acceder a APIs internas necesarias para su funcionamiento.

### ¿Dónde está la Spark UI?
Está deshabilitada (`spark.ui.enabled=false`) para evitar conflictos de servlets con Spring Boot 3. En producción se usaría un cluster Spark separado.

---

**¡Felicitaciones por completar el Bloque 1! 🎉**

Ahora tienes las bases para trabajar con Apache Spark y Spring Boot. En el próximo bloque profundizaremos en transformaciones y análisis de datos más complejos.
