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

### Stack Tecnológico
- **Java:** 17 LTS
- **Apache Spark:** 3.5.0
- **Spring Boot:** 3.2.0
- **PostgreSQL:** 15
- **Docker:** Cluster Spark + PostgreSQL

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

### 📊 Bloque 2: Análisis de Negocio ✨ NUEVO

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
│   └── BLOQUE2_GUIA_COMPLETA.md    # Guía completa Bloque 2 ✨ NUEVO
├── src/main/java/com/ecommerce/analytics/
│   ├── EcommerceAnalyticsApplication.java    # Main class
│   ├── config/
│   │   └── SparkConfig.java                  # Configuración SparkSession
│   ├── model/                                # DTOs Bloque 2 ✨ NUEVO
│   │   ├── SalesByCategory.java              # DTO ventas por categoría
│   │   ├── TopProduct.java                   # DTO productos más vendidos
│   │   ├── DailySalesSummary.java            # DTO resumen diario
│   │   └── SalesByRegion.java                # DTO ventas por región
│   ├── service/
│   │   ├── DataReaderService.java            # Servicio lectura de datos
│   │   └── AnalyticsService.java             # Análisis y agregaciones ✨ NUEVO
│   └── controller/
│       ├── DataExplorationController.java    # REST endpoints Bloque 1
│       ├── SalesAnalyticsController.java     # Endpoints ventas ✨ NUEVO
│       └── ProductAnalyticsController.java   # Endpoints productos ✨ NUEVO
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

### Bloque 2: Transformaciones y Análisis ✨ NUEVO

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

## 🔜 Próximos Pasos: Bloque 3

En el siguiente bloque implementaremos:
- User Defined Functions (UDFs)
- Spark SQL avanzado (CREATE TEMP VIEW, queries complejas)
- Optimización y tuning (cache, persist, repartition)
- Persistencia de resultados en PostgreSQL
- Análisis predictivo básico

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

#### ❌ ANTLR Version Conflict
```
Error: Could not deserialize ATN with version 3 (expected 4)
```
**Solución**: El pom.xml incluye `<dependencyManagement>` que fuerza ANTLR 4.9.3. Si persiste, ejecutar `mvn clean package`.

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
- 📘 **Guía Bloque 1**: `docs/BLOQUE1_GUIA_COMPLETA.md` - Tutorial completo con ejemplos
- 🔧 **Troubleshooting**: Ver sección "Consideraciones Técnicas Críticas" en guía Bloque 1

### Documentación Oficial
- [Apache Spark 3.5.0 Documentation](https://spark.apache.org/docs/3.5.0/)
- [Spring Boot 3.2.0 Reference](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [Spark SQL Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)

---

**Versión:** 2.0 - Bloque 2 Completado - Transformaciones y Análisis Avanzado
**Fecha:** Octubre 2025
**Última Actualización:** Octubre 2025 - Bloque 2: Agregaciones, Joins y Window Functions

**Cambios en v2.0:**
- ✅ **Bloque 2 Completado**: Transformaciones y análisis de datos
- ✅ Agregadas 7 nuevos endpoints REST (ventas y productos)
- ✅ Implementadas agregaciones avanzadas (groupBy, sum, avg, count, etc.)
- ✅ Joins entre datasets (transactions + products + customers)
- ✅ Window Functions para rankings
- ✅ Filtrado dinámico por fechas y categorías
- ✅ DTOs con Lombok para responses estructurados
- ✅ Documentación completa en `BLOQUE2_GUIA_COMPLETA.md`

**Cambios en v1.1:**
- ✅ Agregada sección Quick Start con script automatizado (`start.sh`)
- ✅ Comandos de ejecución validados (Maven y JAR)
- ✅ Documentación de JVM arguments obligatorios para Java 17
- ✅ Enlaces a documentación completa y troubleshooting
