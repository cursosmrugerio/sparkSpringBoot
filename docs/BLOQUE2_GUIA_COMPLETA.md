# 📚 Bloque 2: Transformaciones y Análisis de Datos - Guía Completa

## 🎯 Objetivo del Bloque 2

Dominar operaciones avanzadas de Spark: transformaciones, agregaciones, joins y exposición de resultados mediante REST API, aplicando análisis de negocio real sobre datos de e-commerce.

---

## 📖 Índice

1. [Conceptos Teóricos](#1-conceptos-teóricos)
2. [Transformaciones vs Acciones](#2-transformaciones-vs-acciones)
3. [Agregaciones de Negocio](#3-agregaciones-de-negocio)
4. [Joins entre Datasets](#4-joins-entre-datasets)
5. [Window Functions](#5-window-functions)
6. [API REST Completa](#6-api-rest-completa)
7. [Ejemplos de Código](#7-ejemplos-de-código)
8. [Ejercicios Prácticos](#8-ejercicios-prácticos)

---

## 1. Conceptos Teóricos

### 1.1 Transformaciones en Spark

Las **transformaciones** son operaciones que crean un nuevo DataFrame a partir de uno existente. Son **lazy** (perezosas): no se ejecutan hasta que se invoca una **acción**.

#### Transformaciones Básicas

| Operación | Descripción | Ejemplo |
|-----------|-------------|---------|
| `select()` | Selecciona columnas | `df.select("name", "age")` |
| `filter()` / `where()` | Filtra filas | `df.filter(col("age").gt(18))` |
| `groupBy()` | Agrupa por columnas | `df.groupBy("category")` |
| `orderBy()` / `sort()` | Ordena resultados | `df.orderBy(desc("sales"))` |
| `join()` | Combina datasets | `df1.join(df2, "id")` |
| `withColumn()` | Agrega/modifica columna | `df.withColumn("total", col("price") * col("qty"))` |

#### Acciones que Ejecutan el Plan

| Acción | Descripción | Retorna |
|--------|-------------|---------|
| `count()` | Cuenta filas | Long |
| `show()` | Muestra datos | void |
| `collect()` | Trae datos al driver | List<Row> |
| `take(n)` | Primeras n filas | List<Row> |
| `first()` | Primera fila | Row |
| `write()` | Escribe a disco | void |

---

## 2. Transformaciones vs Acciones

### 2.1 Lazy Evaluation en Detalle

```java
// TRANSFORMACIONES (NO ejecutan nada)
Dataset<Row> transactions = sparkSession.read().csv("data.csv");  // ❌ No lee archivo
Dataset<Row> filtered = transactions.filter("amount > 100");      // ❌ No filtra
Dataset<Row> grouped = filtered.groupBy("category");              // ❌ No agrupa
Dataset<Row> result = grouped.agg(sum("amount"));                 // ❌ No calcula

// ACCIÓN (EJECUTA TODO EL PLAN)
long count = result.count();  // ✅ AQUÍ se ejecuta todo el pipeline
```

### 2.2 ¿Por qué Lazy Evaluation?

**Ventajas:**
1. **Optimización**: Spark analiza TODO el plan antes de ejecutar
2. **Eficiencia**: Evita cálculos innecesarios
3. **Predicate Pushdown**: Filtros se aplican lo antes posible
4. **Fusión de operaciones**: Combina transformaciones compatibles

**Ejemplo de optimización:**
```java
// Código que escribes
Dataset<Row> result = transactions
    .select("product_id", "amount")
    .filter("amount > 100")
    .groupBy("product_id")
    .agg(sum("amount"));

// Spark optimiza a:
// 1. Lee SOLO las columnas necesarias (product_id, amount)
// 2. Aplica filtro DURANTE la lectura (predicate pushdown)
// 3. Agrupa y suma en una sola pasada
```

---

## 3. Agregaciones de Negocio

### 3.1 Funciones de Agregación

```java
import static org.apache.spark.sql.functions.*;

// Agregaciones simples
df.agg(
    sum("amount"),           // Suma total
    avg("amount"),           // Promedio
    count("*"),              // Conteo
    max("amount"),           // Máximo
    min("amount"),           // Mínimo
    stddev("amount")         // Desviación estándar
);

// Agregaciones con alias
df.groupBy("category")
    .agg(
        sum("amount").alias("totalSales"),
        avg("amount").alias("avgTicket"),
        count("transaction_id").alias("txCount")
    );
```

### 3.2 Caso de Uso: Ventas por Categoría

**Objetivo:** Calcular ventas totales, cantidad vendida y ticket promedio por categoría.

**Código:**
```java
public List<SalesByCategory> getSalesByCategory() {
    Dataset<Row> transactions = dataReaderService.readTransactions();
    Dataset<Row> products = dataReaderService.readProducts();

    // JOIN para obtener categoría del producto
    Dataset<Row> result = transactions
            .join(products, "product_id")
            .groupBy("category")
            .agg(
                    sum("amount").alias("totalSales"),
                    sum("quantity").alias("totalQuantity"),
                    avg("amount").alias("avgAmount"),
                    count("transaction_id").alias("transactionCount")
            )
            .orderBy(desc("totalSales"));

    // Convertir a DTO
    return result.collectAsList().stream()
            .map(row -> new SalesByCategory(
                    row.getAs("category"),
                    row.getAs("totalSales"),
                    row.getAs("totalQuantity"),
                    row.getAs("avgAmount"),
                    row.getAs("transactionCount")
            ))
            .toList();
}
```

**Resultado:**
```json
[
    {
        "category": "Electronics",
        "totalSales": 2314.83,
        "totalQuantity": 17,
        "avgAmount": 210.44,
        "transactionCount": 11
    },
    {
        "category": "Furniture",
        "totalSales": 799.99,
        "totalQuantity": 1,
        "avgAmount": 799.99,
        "transactionCount": 1
    }
]
```

**Operaciones Spark:**
1. `join()` - Combinar transactions con products
2. `groupBy("category")` - Agrupar por categoría
3. `agg()` - Múltiples agregaciones
4. `orderBy(desc())` - Ordenar descendente

---

## 4. Joins entre Datasets

### 4.1 Tipos de Joins

| Tipo | Descripción | Uso |
|------|-------------|-----|
| **inner** | Solo filas que coinciden en ambos datasets | Default, más común |
| **left** | Todas las filas del izquierdo, nulls en derecho | Mantener todas las transacciones |
| **right** | Todas las filas del derecho, nulls en izquierdo | Mantener todos los productos |
| **full** / **outer** | Todas las filas de ambos | Raro, análisis exhaustivo |

### 4.2 Sintaxis de Joins

```java
// Join simple (inner por defecto)
Dataset<Row> result = transactions.join(products, "product_id");

// Join con tipo específico
Dataset<Row> result = transactions.join(products,
    transactions.col("product_id").equalTo(products.col("product_id")),
    "left"
);

// Join con múltiples condiciones
Dataset<Row> result = transactions.join(products,
    transactions.col("product_id").equalTo(products.col("product_id"))
        .and(transactions.col("region").equalTo(products.col("region"))),
    "inner"
);
```

### 4.3 Caso de Uso: Enriquecer Transacciones

**Objetivo:** Agregar información de producto y cliente a cada transacción.

```java
// Leer datasets
Dataset<Row> transactions = dataReaderService.readTransactions();
Dataset<Row> products = dataReaderService.readProducts();
Dataset<Row> customers = dataReaderService.readCustomers();

// JOIN triple
Dataset<Row> enriched = transactions
    .join(products, "product_id")           // Agregar info de producto
    .join(customers, "customer_id")         // Agregar info de cliente
    .select(
        "transaction_id",
        "transaction_date",
        "amount",
        "product_name",
        "category",
        "price",
        customers.col("name").alias("customer_name"),
        "region"
    );

enriched.show(5);
```

**Resultado:**
```
+---------------+-------------------+------+-----------------+-----------+------+--------------+------+
|transaction_id |transaction_date   |amount|product_name     |category   |price |customer_name |region|
+---------------+-------------------+------+-----------------+-----------+------+--------------+------+
|TXN001         |2024-10-01 10:30:00|59.98 |Wireless Mouse   |Electronics|29.99 |John Doe      |North |
|TXN002         |2024-10-01 11:15:00|129.99|Laptop Stand     |Accessories|129.99|Jane Smith    |South |
+---------------+-------------------+------+-----------------+-----------+------+--------------+------+
```

---

## 5. Window Functions

### 5.1 ¿Qué son Window Functions?

**Window Functions** permiten realizar cálculos sobre un "ventana" de filas relacionadas, sin colapsar el resultado como lo hace `groupBy`.

**Casos de uso:**
- Rankings (top N por categoría)
- Running totals (suma acumulada)
- Moving averages (promedios móviles)
- Comparación con fila anterior/siguiente

### 5.2 Sintaxis Básica

```java
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

// Definir ventana
WindowSpec windowSpec = Window
    .partitionBy("category")      // Opcional: particionar por columna
    .orderBy(desc("totalSales")); // Orden para ranking

// Aplicar función de ventana
Dataset<Row> result = df.withColumn("rank", row_number().over(windowSpec));
```

### 5.3 Caso de Uso: Top Productos con Ranking

**Objetivo:** Obtener top 10 productos más vendidos con su posición (rank).

```java
public List<TopProduct> getTopProducts(int limit) {
    Dataset<Row> transactions = dataReaderService.readTransactions();
    Dataset<Row> products = dataReaderService.readProducts();

    // Agregar ventas por producto
    Dataset<Row> productSales = transactions
            .join(products, "product_id")
            .groupBy("product_id", "product_name", "category")
            .agg(
                    sum("amount").alias("totalSales"),
                    sum("quantity").alias("quantity")
            )
            .orderBy(desc("totalSales"))
            .limit(limit);

    // Agregar ranking con Window Function
    WindowSpec windowSpec = Window.orderBy(desc("totalSales"));
    Dataset<Row> result = productSales
            .withColumn("rank", row_number().over(windowSpec));

    return result.collectAsList().stream()
            .map(row -> new TopProduct(
                    row.getAs("product_id"),
                    row.getAs("product_name"),
                    row.getAs("category"),
                    row.getAs("totalSales"),
                    row.getAs("quantity"),
                    row.getAs("rank")
            ))
            .toList();
}
```

**Resultado:**
```json
[
    {
        "productId": "PROD011",
        "productName": "Gaming Chair",
        "category": "Furniture",
        "totalSales": 799.99,
        "quantity": 1,
        "rank": 1
    },
    {
        "productId": "PROD014",
        "productName": "Smartwatch",
        "category": "Electronics",
        "totalSales": 599.99,
        "quantity": 1,
        "rank": 2
    }
]
```

### 5.4 Funciones de Ventana Comunes

| Función | Descripción | Ejemplo |
|---------|-------------|---------|
| `row_number()` | Número de fila único | Ranking estricto |
| `rank()` | Ranking con empates | Puede saltar números |
| `dense_rank()` | Ranking sin saltos | No salta números |
| `lag(col, n)` | Valor de n filas atrás | Comparar con anterior |
| `lead(col, n)` | Valor de n filas adelante | Comparar con siguiente |
| `sum().over()` | Suma acumulada | Running total |

---

## 6. API REST Completa

### 6.1 Arquitectura REST

```
Cliente (curl/Postman)
    ↓
Controller (Spring @RestController)
    ↓
Service (AnalyticsService con Spark)
    ↓
Spark Operations (groupBy, join, agg)
    ↓
Dataset<Row> → DTO → JSON Response
```

### 6.2 Endpoints Implementados

#### Endpoint 1: Ventas por Categoría
```java
@GetMapping("/api/sales/by-category")
public ResponseEntity<List<SalesByCategory>> getSalesByCategory()
```

**Request:**
```bash
curl http://localhost:8080/api/sales/by-category
```

**Response:**
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

#### Endpoint 2: Top Productos
```java
@GetMapping("/api/products/top-selling")
public ResponseEntity<List<TopProduct>> getTopProducts(@RequestParam int limit)
```

**Request:**
```bash
curl "http://localhost:8080/api/products/top-selling?limit=5"
```

#### Endpoint 3: Estadísticas Generales
```java
@GetMapping("/api/sales/statistics")
public ResponseEntity<Map<String, Object>> getStatistics()
```

**Request:**
```bash
curl http://localhost:8080/api/sales/statistics
```

**Response:**
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

#### Endpoint 4: Resumen Diario (con filtros)
```java
@GetMapping("/api/sales/daily-summary")
public ResponseEntity<List<DailySalesSummary>> getDailySummary(
    @RequestParam(required = false) String startDate,
    @RequestParam(required = false) String endDate)
```

**Request:**
```bash
curl "http://localhost:8080/api/sales/daily-summary?startDate=2024-10-01&endDate=2024-10-02"
```

**Filtrado Dinámico:**
```java
// Si hay filtros, aplicarlos
if (startDate != null && endDate != null) {
    transactions = transactions.filter(
        col("transaction_date").between(startDate, endDate)
    );
}
```

#### Endpoint 5: Ventas por Región
```java
@GetMapping("/api/sales/by-region")
public ResponseEntity<List<SalesByRegion>> getSalesByRegion(
    @RequestParam(required = false) String startDate,
    @RequestParam(required = false) String endDate)
```

#### Endpoint 6: Productos por Categoría
```java
@GetMapping("/api/products/by-category/{category}")
public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(
    @PathVariable String category)
```

**Request:**
```bash
curl http://localhost:8080/api/products/by-category/Electronics
```

#### Endpoint 7: Analytics de Producto Específico
```java
@GetMapping("/api/products/{productId}/analytics")
public ResponseEntity<Map<String, Object>> getProductAnalytics(
    @PathVariable String productId)
```

**Request:**
```bash
curl http://localhost:8080/api/products/PROD001/analytics
```

**Response:**
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

---

## 7. Ejemplos de Código

### 7.1 Filtrado por Rango de Fechas

```java
// Filtrar transacciones entre dos fechas
Dataset<Row> filtered = transactions.filter(
    col("transaction_date").between("2024-10-01", "2024-10-31")
);

// Filtrar por mes específico
Dataset<Row> october = transactions.filter(
    month(col("transaction_date")).equalTo(10)
);

// Filtrar últimos 7 días
Dataset<Row> lastWeek = transactions.filter(
    col("transaction_date").geq(date_sub(current_date(), 7))
);
```

### 7.2 Agregaciones Múltiples

```java
// Calcular múltiples métricas en una sola pasada
Dataset<Row> metrics = transactions.agg(
    sum("amount").alias("totalRevenue"),
    avg("amount").alias("avgTicket"),
    max("amount").alias("maxTransaction"),
    min("amount").alias("minTransaction"),
    count("*").alias("totalTransactions"),
    countDistinct("customer_id").alias("uniqueCustomers"),
    stddev("amount").alias("stdDeviation")
);
```

### 7.3 Transformación de Fechas

```java
// Extraer componentes de fecha
Dataset<Row> result = transactions
    .withColumn("year", year(col("transaction_date")))
    .withColumn("month", month(col("transaction_date")))
    .withColumn("day", dayofmonth(col("transaction_date")))
    .withColumn("dayOfWeek", dayofweek(col("transaction_date")))
    .withColumn("date", to_date(col("transaction_date")));

// Agrupar por mes
Dataset<Row> monthlySales = result
    .groupBy("year", "month")
    .agg(sum("amount").alias("totalSales"))
    .orderBy("year", "month");
```

### 7.4 Manejo de Valores Null

```java
// Filtrar nulls
Dataset<Row> nonNull = df.filter(col("amount").isNotNull());

// Reemplazar nulls
Dataset<Row> filled = df.na().fill(0, new String[]{"amount", "quantity"});

// Eliminar filas con nulls
Dataset<Row> dropped = df.na().drop(new String[]{"customer_id", "product_id"});
```

---

## 8. Ejercicios Prácticos

### Ejercicio 1: Análisis de Productos por Categoría

**Objetivo:** Crear un endpoint que muestre productos de una categoría ordenados por ventas.

**Pasos:**
1. Filtrar productos por categoría
2. JOIN con transactions
3. Agregar ventas por producto
4. Ordenar por totalSales descendente

**Solución:**
```java
public List<Map<String, Object>> getProductsByCategory(String category) {
    Dataset<Row> transactions = dataReaderService.readTransactions();
    Dataset<Row> products = dataReaderService.readProducts();

    Dataset<Row> result = products
            .filter(col("category").equalTo(category))
            .join(transactions, "product_id")
            .groupBy("product_id", "product_name", "category", "price")
            .agg(
                    sum("amount").alias("totalSales"),
                    sum("quantity").alias("totalQuantity"),
                    count("transaction_id").alias("transactionCount")
            )
            .orderBy(desc("totalSales"));

    return result.collectAsList().stream()
            .map(row -> /* convertir a Map */)
            .toList();
}
```

### Ejercicio 2: Calcular Tasa de Conversión por Región

**Objetivo:** Calcular ventas por cliente único en cada región.

```java
Dataset<Row> conversionRate = transactions
    .groupBy("region")
    .agg(
        sum("amount").alias("totalSales"),
        countDistinct("customer_id").alias("uniqueCustomers")
    )
    .withColumn("salesPerCustomer",
        col("totalSales").divide(col("uniqueCustomers")));
```

### Ejercicio 3: Identificar Clientes VIP

**Objetivo:** Clientes con más de $500 en compras.

```java
Dataset<Row> vipCustomers = transactions
    .groupBy("customer_id")
    .agg(sum("amount").alias("totalSpent"))
    .filter(col("totalSpent").gt(500))
    .join(customers, "customer_id")
    .select("customer_id", "name", "email", "totalSpent")
    .orderBy(desc("totalSpent"));
```

---

## 🎓 Conceptos Clave Aprendidos

### ✅ Checklist de Aprendizaje

- [ ] Entiendo la diferencia entre transformaciones y acciones
- [ ] Puedo usar groupBy con múltiples agregaciones
- [ ] Sé hacer joins entre 2 o más datasets
- [ ] Entiendo Window Functions y cuándo usarlas
- [ ] Puedo filtrar datos por rango de fechas
- [ ] Sé exponer resultados de Spark en REST API
- [ ] Entiendo cómo convertir Dataset<Row> a DTOs
- [ ] Puedo usar funciones de fecha (to_date, month, year)
- [ ] Sé manejar valores null en Spark
- [ ] Entiendo el concepto de lazy evaluation

### 🔑 Puntos Clave para Recordar

1. **Joins requieren columna común**: Asegúrate que la columna de join exista en ambos datasets
2. **Alias son importantes**: Usa `.alias()` para nombres claros en agregaciones
3. **Window Functions ≠ groupBy**: Windows mantienen todas las filas, groupBy las colapsa
4. **Filtros antes de joins**: Optimiza filtrando datos antes de hacer joins pesados
5. **collectAsList() trae datos al driver**: Solo úsalo con resultados pequeños
6. **Spark optimiza automáticamente**: Confía en el optimizer, escribe código claro

---

## 🚀 Próximos Pasos: Bloque 3

En el siguiente bloque aprenderemos:
- ✨ **UDFs (User Defined Functions)**: Funciones personalizadas
- 🔧 **Optimización**: Particionamiento, caché, broadcast joins
- 💾 **Persistencia**: Escribir resultados en PostgreSQL y Parquet
- 🧹 **Limpieza de datos**: Manejo avanzado de nulls y duplicados
- 🚨 **Detección de anomalías**: Sistema de alertas

---

## 📚 Referencias Adicionales

### Documentación Oficial
- [Spark SQL Guide](https://spark.apache.org/docs/latest/sql-programming-guide.html)
- [Spark Functions API](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/functions.html)
- [Window Functions](https://spark.apache.org/docs/latest/sql-ref-syntax-qry-select-window.html)

### Cheat Sheets
- [DataFrame Operations](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/Dataset.html)
- [SQL Functions Reference](https://spark.apache.org/docs/latest/api/sql/)

---

**Versión del Documento:** 1.0
**Fecha:** Octubre 2025
**Autor:** Equipo de Capacitación Spark

---

**¡Felicitaciones por completar el Bloque 2! 🎉**

Ahora dominas transformaciones avanzadas, agregaciones de negocio y APIs REST con Spark. Estás listo para optimización y procesamiento avanzado en el Bloque 3.
