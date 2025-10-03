# Bloque 3: Procesamiento Avanzado y Optimización - Guía Completa

## 📋 Resumen del Bloque

**Duración**: 2 horas
**Estado**: ✅ IMPLEMENTADO
**Nivel**: Avanzado

### Objetivos de Aprendizaje

1. ✅ Crear y utilizar User Defined Functions (UDFs) personalizadas
2. ✅ Implementar estrategias de caché y particionamiento
3. ✅ Optimizar performance con broadcast joins
4. ✅ Persistir resultados en PostgreSQL (bidireccional)
5. ✅ Desarrollar sistema de detección de fraude y anomalías

---

## 🎯 Funcionalidades Implementadas

### 1. User Defined Functions (UDFs)

**Ubicación**: `src/main/java/com/ecommerce/analytics/udf/CustomUDFs.java`

Se implementaron 5 UDFs personalizadas:

#### 1.1 Validación de Email
```java
public static class ValidateEmail implements UDF1<String, Boolean>
```
- **Input**: Email (String)
- **Output**: Boolean
- **Función**: Valida formato de email con regex

#### 1.2 Categorización de Montos
```java
public static class CategorizeAmount implements UDF1<Double, String>
```
- **Input**: Monto (Double)
- **Output**: Categoría (String)
- **Categorías**:
  - BAJO: < $50
  - MEDIO: $50-200
  - ALTO: $200-500
  - MUY_ALTO: > $500

#### 1.3 Detección de Fraude
```java
public static class DetectFraud implements UDF2<Double, Long, String>
```
- **Inputs**: Monto (Double), Cantidad (Long)
- **Output**: Nivel de riesgo (String)
- **Niveles**:
  - BAJO_RIESGO: Transacciones normales
  - MEDIO_RIESGO: Monto > $1000 O cantidad > 10
  - ALTO_RIESGO: Monto > $1000 Y cantidad > 10

#### 1.4 Normalización de Strings
```java
public static class NormalizeString implements UDF1<String, String>
```
- Convierte a mayúsculas y elimina espacios

#### 1.5 Cálculo de Descuento
```java
public static class CalculateDiscount implements UDF1<Double, Double>
```
- **Descuentos por categoría**:
  - BAJO: 0%
  - MEDIO: 5%
  - ALTO: 10%
  - MUY_ALTO: 15%

---

### 2. Servicio de Optimización

**Ubicación**: `src/main/java/com/ecommerce/analytics/service/OptimizationService.java`

#### 2.1 Caché Estratégico
```java
public void cacheMainDatasets()
```
- Cachea datasets principales en memoria
- Usa `StorageLevel.MEMORY_AND_DISK()` para evitar OOM
- Reduce tiempos de lectura en queries repetitivos

```java
public void unpersistAll()
```
- Libera caché de todos los datasets
- Importante para gestión de memoria

#### 2.2 Registro de UDFs
```java
public void registerUDFs()
```
- Registra todas las UDFs en SparkSession
- Las hace disponibles para usar en SQL queries

#### 2.3 Limpieza de Datos
```java
public Dataset<Row> cleanTransactions()
```
- Elimina nulls en campos críticos
- Filtra valores inválidos (amount > 0, quantity > 0)
- Elimina duplicados por transaction_id

#### 2.4 Aplicación de UDFs
```java
public Dataset<Row> applyUDFsToTransactions()
```
- Enriquece transacciones con:
  - Categoría de monto
  - Nivel de riesgo de fraude
  - Porcentaje de descuento
  - Monto con descuento aplicado

#### 2.5 Broadcast Joins
```java
public Dataset<Row> broadcastJoinTransactionsWithProducts()
```
- Optimiza joins con tablas pequeñas
- Usa `broadcast()` para distribuir tabla pequeña a todos los executors

---

### 3. Persistencia en PostgreSQL

#### 3.1 Entidades JPA

##### SalesReportEntity
**Tabla**: `sales_reports`

Campos:
- `report_date`: Fecha del reporte
- `category`: Categoría de producto
- `region`: Región geográfica
- `total_sales`: Ventas totales
- `total_quantity`: Cantidad total
- `transaction_count`: Número de transacciones
- `avg_ticket`: Ticket promedio
- `unique_customers`: Clientes únicos
- `created_at`: Timestamp de creación

##### FraudAlertEntity
**Tabla**: `fraud_alerts`

Campos:
- `transaction_id`: ID de transacción sospechosa
- `customer_id`, `product_id`
- `amount`, `quantity`
- `risk_level`: BAJO/MEDIO/ALTO_RIESGO
- `amount_category`: BAJO/MEDIO/ALTO/MUY_ALTO
- `deviation_from_mean`: Desviación estándar
- `is_outlier`: Boolean (outlier detectado)
- `reviewed`: Boolean (alerta revisada)

##### ProductPerformanceEntity
**Tabla**: `product_performance`

Campos:
- `product_id`, `product_name`, `category`
- `total_revenue`: Ingresos totales
- `total_quantity`: Cantidad vendida
- `transaction_count`: Número de transacciones
- `avg_ticket`: Ticket promedio
- `unique_customers`: Clientes únicos
- `rank_overall`: Ranking general
- `rank_in_category`: Ranking en categoría
- `analysis_date`: Fecha del análisis

#### 3.2 Repositorios Spring Data JPA

##### SalesReportRepository
```java
// Buscar por rango de fechas
List<SalesReportEntity> findByReportDateBetween(LocalDate start, LocalDate end)

// Buscar por categoría
List<SalesReportEntity> findByCategory(String category)

// Top por ventas
@Query("SELECT s FROM SalesReportEntity s ORDER BY s.totalSales DESC")
List<SalesReportEntity> findTopByTotalSales()
```

##### FraudAlertRepository
```java
// Alertas no revisadas
List<FraudAlertEntity> findByReviewed(Boolean reviewed)

// Alertas de alto riesgo
@Query("SELECT f FROM FraudAlertEntity f WHERE f.riskLevel = 'ALTO_RIESGO' ...")
List<FraudAlertEntity> findHighRiskUnreviewed()

// Contar por nivel de riesgo
@Query("SELECT f.riskLevel, COUNT(f) FROM FraudAlertEntity f ...")
List<Object[]> countAlertsByRiskLevel()
```

##### ProductPerformanceRepository
```java
// Top productos por revenue
@Query("SELECT p FROM ProductPerformanceEntity p ORDER BY p.totalRevenue DESC")
List<ProductPerformanceEntity> findTopByRevenue()

// Último análisis de un producto
Optional<ProductPerformanceEntity> findLatestByProductId(String productId)
```

#### 3.3 Servicio de Persistencia

**Ubicación**: `src/main/java/com/ecommerce/analytics/service/PersistenceService.java`

##### Escritura de DataFrames a PostgreSQL
```java
public void saveDataFrameToPostgres(Dataset<Row> df, String tableName, SaveMode saveMode)
```
- Escribe DataFrame directamente usando JDBC
- Soporta modos: APPEND, OVERWRITE, etc.

##### Guardar Alertas de Fraude
```java
public int saveFraudAlertsFromDataFrame(Dataset<Row> fraudData)
```
- Convierte Row a entidades JPA
- Guarda en lote para mejor performance

##### Guardar Rendimiento de Productos
```java
public int saveProductPerformanceFromDataFrame(Dataset<Row> productData)
```

##### Lectura desde PostgreSQL
```java
public List<FraudAlertEntity> getUnreviewedFraudAlerts()
public List<ProductPerformanceEntity> getTopProductsByRevenue(int limit)
public Map<String, Object> getDatabaseStats()
```

---

### 4. Sistema de Detección de Fraude

**Ubicación**: `src/main/java/com/ecommerce/analytics/service/FraudDetectionService.java`

#### 4.1 Detección de Outliers
```java
public Dataset<Row> detectSuspiciousTransactions(double stdDevThreshold)
```

**Algoritmo**:
1. Calcula media y desviación estándar de montos
2. Define límites: `mean ± (threshold × stddev)`
3. Marca transacciones fuera de límites como outliers
4. Aplica UDFs para categorización adicional

**Criterios de Detección**:
- Monto > 3 desviaciones estándar
- Cantidad excesiva (> 10 unidades)
- Monto muy alto (> $1000)

#### 4.2 Análisis de Patrones por Cliente
```java
public List<Map<String, Object>> analyzeCustomerFraudPatterns(double threshold)
```

Retorna:
- Número de transacciones sospechosas por cliente
- Monto total sospechoso
- Monto promedio sospechoso
- Desviación máxima
- Lista de niveles de riesgo

#### 4.3 Análisis de Patrones por Producto
```java
public List<Map<String, Object>> analyzeProductFraudPatterns(double threshold)
```

Identifica productos frecuentemente involucrados en fraude.

#### 4.4 Detección de Duplicados
```java
public Dataset<Row> detectDuplicateSuspiciousTransactions()
```

Encuentra transacciones duplicadas:
- Mismo cliente + producto + monto
- Múltiples ocurrencias

#### 4.5 Guardar Alertas
```java
public int detectAndSaveFraudAlerts(double threshold)
```

Detecta y persiste alertas en PostgreSQL automáticamente.

---

## 🌐 Endpoints REST del Bloque 3

### Optimización (`/api/optimization`)

#### 1. Cachear Datasets
```bash
POST /api/optimization/cache
```

**Response**:
```json
{
  "message": "Datasets cacheados exitosamente",
  "durationMs": 1234,
  "cacheInfo": {
    "transactionsCached": true,
    "productsCached": true,
    "customersCached": true
  }
}
```

#### 2. Liberar Caché
```bash
DELETE /api/optimization/cache
```

#### 3. Información del Caché
```bash
GET /api/optimization/cache/info
```

#### 4. Transacciones Enriquecidas con UDFs
```bash
GET /api/optimization/transactions/enriched?limit=10
```

**Response**:
```json
[
  {
    "transactionId": "TXN001",
    "customerId": "CUST001",
    "productId": "PROD001",
    "amount": 299.99,
    "quantity": 2,
    "amountCategory": "ALTO",
    "fraudRisk": "BAJO_RIESGO",
    "discountPct": 10.0,
    "amountWithDiscount": 269.99
  }
]
```

#### 5. Transacciones Limpias
```bash
GET /api/optimization/transactions/clean
```

#### 6. Broadcast Join Optimizado
```bash
GET /api/optimization/transactions/broadcast-join?limit=5
```

#### 7. Registrar UDFs
```bash
POST /api/optimization/udfs/register
```

---

### Detección de Fraude (`/api/fraud`)

#### 1. Detectar Transacciones Sospechosas
```bash
GET /api/fraud/detect?stdDevThreshold=3.0&limit=20
```

**Response**:
```json
[
  {
    "transactionId": "TXN015",
    "customerId": "CUST012",
    "productId": "PROD008",
    "amount": 799.99,
    "quantity": 1,
    "deviation": 3.42,
    "isOutlier": true,
    "amountCategory": "MUY_ALTO",
    "fraudRisk": "MEDIO_RIESGO",
    "transactionDate": "2024-10-02",
    "region": "West"
  }
]
```

#### 2. Detectar y Guardar Alertas
```bash
POST /api/fraud/detect-and-save?stdDevThreshold=3.0
```

**Response**:
```json
{
  "message": "Alertas de fraude detectadas y guardadas",
  "alertsSaved": 5,
  "stdDevThreshold": 3.0
}
```

#### 3. Estadísticas de Fraude
```bash
GET /api/fraud/statistics?stdDevThreshold=3.0
```

**Response**:
```json
{
  "totalTransactions": 20,
  "suspiciousTransactions": 5,
  "fraudRate": 25.0,
  "riskLevelCounts": {
    "MEDIO_RIESGO": 3,
    "ALTO_RIESGO": 2
  },
  "totalSuspiciousAmount": 2500.50,
  "avgSuspiciousAmount": 500.10
}
```

#### 4. Patrones de Fraude por Cliente
```bash
GET /api/fraud/customer-patterns?stdDevThreshold=3.0
```

#### 5. Patrones de Fraude por Producto
```bash
GET /api/fraud/product-patterns?stdDevThreshold=3.0
```

#### 6. Transacciones Duplicadas Sospechosas
```bash
GET /api/fraud/duplicates
```

**Response**:
```json
[
  {
    "customerId": "CUST005",
    "productId": "PROD003",
    "amount": 150.00,
    "occurrenceCount": 3,
    "transactionIds": ["TXN008", "TXN012", "TXN019"],
    "dates": ["2024-10-01", "2024-10-01", "2024-10-02"]
  }
]
```

#### 7. Obtener Alertas Guardadas
```bash
GET /api/fraud/alerts?onlyUnreviewed=true
```

#### 8. Alertas de Alto Riesgo
```bash
GET /api/fraud/alerts/high-risk
```

#### 9. Marcar Alerta como Revisada
```bash
PUT /api/fraud/alerts/{id}/review
```

---

### Persistencia (`/api/persistence`)

#### 1. Obtener Reportes de Ventas
```bash
GET /api/persistence/reports?startDate=2024-10-01&endDate=2024-10-31
```

#### 2. Rendimiento de Productos
```bash
GET /api/persistence/products/performance?productId=PROD001
```

#### 3. Top Productos por Revenue
```bash
GET /api/persistence/products/top-revenue?limit=10
```

#### 4. Estadísticas de Base de Datos
```bash
GET /api/persistence/stats
```

**Response**:
```json
{
  "totalSalesReports": 150,
  "totalFraudAlerts": 25,
  "unreviewedAlerts": 12,
  "highRiskAlerts": 5,
  "totalProductPerformance": 75
}
```

---

## 🔧 Conceptos Técnicos Implementados

### 1. Caché y Persistencia en Spark

**StorageLevel.MEMORY_AND_DISK()**:
- Primero intenta almacenar en memoria
- Si no hay espacio, usa disco
- Evita OutOfMemoryErrors

**Cuándo usar caché**:
- Datasets usados múltiples veces
- Operaciones iterativas
- Datos que no cambian frecuentemente

### 2. UDFs en Spark

**Ventajas**:
- Lógica de negocio personalizada
- Reutilización de código
- Integración con Spark SQL

**Consideraciones**:
- Son menos eficientes que funciones nativas
- No se optimizan con Catalyst optimizer
- Usar solo cuando sea necesario

### 3. Broadcast Joins

**Cuándo usar**:
- Tabla pequeña (< 10MB) + tabla grande
- Join frecuente
- Evita shuffle de datos

**Sintaxis**:
```java
largeDF.join(broadcast(smallDF), "key")
```

### 4. Detección de Outliers

**Método de Desviación Estándar**:
```
outlier = valor > (media + 3σ) || valor < (media - 3σ)
```

**Configuración**:
- `stdDevThreshold = 3.0`: Detección estricta (menos falsos positivos)
- `stdDevThreshold = 2.0`: Detección permisiva (más cobertura)

### 5. Persistencia Bidireccional

**Spark → PostgreSQL**:
```java
df.write()
  .format("jdbc")
  .option("url", jdbcUrl)
  .option("dbtable", tableName)
  .mode(SaveMode.APPEND)
  .save()
```

**PostgreSQL → Spring Boot**:
```java
repository.findByReportDateBetween(start, end)
```

---

## 📊 Flujo Completo de Análisis

### Caso de Uso: Detección y Reporte de Fraude

1. **Caché de Datos**
   ```bash
   POST /api/optimization/cache
   ```

2. **Registro de UDFs**
   ```bash
   POST /api/optimization/udfs/register
   ```

3. **Detectar Fraude**
   ```bash
   POST /api/fraud/detect-and-save?stdDevThreshold=3.0
   ```

4. **Obtener Alertas de Alto Riesgo**
   ```bash
   GET /api/fraud/alerts/high-risk
   ```

5. **Analizar Patrones por Cliente**
   ```bash
   GET /api/fraud/customer-patterns
   ```

6. **Revisar Alertas**
   ```bash
   PUT /api/fraud/alerts/1/review
   PUT /api/fraud/alerts/2/review
   ```

7. **Liberar Caché**
   ```bash
   DELETE /api/optimization/cache
   ```

---

## 🎓 Ejercicios Prácticos

### Ejercicio 1: Optimización de Queries

**Tarea**: Medir diferencia de performance con y sin caché.

1. Ejecutar query sin caché:
   ```bash
   GET /api/sales/by-category
   # Medir tiempo
   ```

2. Cachear datasets:
   ```bash
   POST /api/optimization/cache
   ```

3. Ejecutar mismo query:
   ```bash
   GET /api/sales/by-category
   # Comparar tiempo
   ```

### Ejercicio 2: Detección de Fraude Personalizada

**Tarea**: Ajustar umbral de detección y comparar resultados.

```bash
# Detección estricta (menos alertas)
GET /api/fraud/detect?stdDevThreshold=4.0

# Detección permisiva (más alertas)
GET /api/fraud/detect?stdDevThreshold=2.0
```

### Ejercicio 3: Análisis de Fraude por Región

**Tarea**: Crear query que combine fraude y región.

Modificar `FraudDetectionService` para agregar:
```java
public List<Map<String, Object>> analyzeFraudByRegion(double threshold) {
    // Agrupar transacciones sospechosas por región
    // Calcular tasa de fraude por región
    // Ordenar por tasa de fraude descendente
}
```

---

## 🚀 Mejoras Futuras (Bloque 4)

- ✨ Jobs programados con @Scheduled
- ✨ Procesamiento batch incremental
- ✨ Exportación de reportes (PDF, Excel)
- ✨ Dashboard de métricas en tiempo real
- ✨ Integración con Spark Streaming

---

## 📝 Resumen de Archivos Creados

### Nuevos Archivos (Bloque 3)

```
src/main/java/com/ecommerce/analytics/
├── udf/
│   └── CustomUDFs.java                    # 5 UDFs personalizadas
├── service/
│   ├── OptimizationService.java           # Caché y optimización
│   ├── FraudDetectionService.java         # Detección de fraude
│   └── PersistenceService.java            # Persistencia PostgreSQL
├── entity/
│   ├── SalesReportEntity.java             # Entidad JPA reportes
│   ├── FraudAlertEntity.java              # Entidad JPA alertas
│   └── ProductPerformanceEntity.java      # Entidad JPA productos
├── repository/
│   ├── SalesReportRepository.java         # Repositorio reportes
│   ├── FraudAlertRepository.java          # Repositorio alertas
│   └── ProductPerformanceRepository.java  # Repositorio productos
└── controller/
    ├── OptimizationController.java        # 7 endpoints optimización
    ├── FraudDetectionController.java      # 9 endpoints fraude
    └── PersistenceController.java         # 4 endpoints persistencia
```

**Total**: 15 nuevos archivos Java
**Líneas de código**: ~2500 líneas

---

## ✅ Checklist de Verificación Bloque 3

- [x] UDFs implementadas y registradas
- [x] Servicio de optimización con caché
- [x] Limpieza y validación de datos
- [x] Broadcast joins optimizados
- [x] Entidades JPA creadas
- [x] Repositorios Spring Data implementados
- [x] Persistencia bidireccional funcional
- [x] Sistema de detección de fraude completo
- [x] Detección de outliers con desviación estándar
- [x] 20 endpoints REST nuevos
- [x] PostgreSQL integrado
- [x] Documentación completa

---

**Versión**: 1.0
**Fecha**: Octubre 2025
**Última Actualización**: Bloque 3 Completado

**Cambios principales**:
- ✅ 5 UDFs personalizadas implementadas
- ✅ Sistema de caché y optimización completo
- ✅ 3 entidades JPA + 3 repositorios
- ✅ Persistencia bidireccional Spark ↔ PostgreSQL
- ✅ Sistema de detección de fraude con múltiples algoritmos
- ✅ 20 nuevos endpoints REST
- ✅ Guía completa con ejemplos prácticos
