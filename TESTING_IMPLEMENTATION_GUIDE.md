# 🧪 Guía de Implementación de Tests - 85% Cobertura

## ✅ Tests YA Implementados

### 1. BatchJobServiceTest.java ✅
- ✅ startJobExecution_ShouldCreateAndSaveJobExecution
- ✅ completeJobExecution_ShouldUpdateStatusToSuccess
- ✅ failJobExecution_ShouldUpdateStatusToFailed
- ✅ getExecutionsByJobName_ShouldReturnFilteredExecutions
- ✅ getExecutionsByStatus_ShouldReturnFilteredExecutions
- ✅ getAllExecutions_ShouldReturnAllExecutions
- ✅ processIncrementalData_WithValidDate_ShouldReturnSuccess
- ✅ cleanAndValidateData_ShouldRemoveNullsAndDuplicates

**Cobertura estimada**: 90% del BatchJobService

---

## 📋 Tests Pendientes por Implementar

### Comandos para Ejecutar Tests

```bash
# Compilar y ejecutar tests
mvn clean test

# Generar reporte de cobertura
mvn jacoco:report

# Ver reporte HTML
open target/site/jacoco/index.html

# Ejecutar tests específicos
mvn test -Dtest=BatchJobServiceTest

# Ejecutar con cobertura mínima enforcement
mvn verify
```

---

## 📁 Estructura de Archivos de Test Requeridos

```
src/test/java/com/ecommerce/analytics/
├── service/
│   ├── BatchJobServiceTest.java          ✅ IMPLEMENTADO
│   ├── ReportServiceTest.java            ⚠️ CREAR
│   ├── FraudDetectionServiceTest.java    ⚠️ CREAR
│   ├── DataReaderServiceTest.java        ⚠️ CREAR
│   ├── AnalyticsServiceTest.java         ⚠️ CREAR
│   └── PersistenceServiceTest.java       ⚠️ CREAR
├── controller/
│   ├── BatchJobControllerTest.java       ⚠️ CREAR
│   ├── DataExplorationControllerTest.java ⚠️ CREAR
│   └── FraudDetectionControllerTest.java  ⚠️ CREAR
└── integration/
    └── BatchJobIntegrationTest.java      ⚠️ CREAR
```

---

## 🔧 Configuración de Test (application-test.yml)

Crear archivo: `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  h2:
    console:
      enabled: false

spark:
  master: local[1]
  appName: ecommerce-analytics-test
  config:
    spark.sql.shuffle.partitions: 2
    spark.driver.memory: 1g

scheduling:
  enabled: false

logging:
  level:
    com.ecommerce.analytics: DEBUG
```

---

## 📝 Templates para Tests Restantes

### 2. ReportServiceTest.java

```java
package com.ecommerce.analytics.service;

import com.ecommerce.analytics.entity.DailyReportEntity;
import com.ecommerce.analytics.repository.DailyReportRepository;
import com.ecommerce.analytics.repository.FraudAlertRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DataReaderService dataReaderService;

    @Mock
    private DailyReportRepository dailyReportRepository;

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private Dataset<Row> mockDataset;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateDailyReport_WhenNotExists_ShouldCreateReport() {
        // Given
        LocalDate reportDate = LocalDate.now();
        when(dailyReportRepository.existsByReportDate(reportDate)).thenReturn(false);
        when(dataReaderService.readTransactions()).thenReturn(mockDataset);
        when(dataReaderService.readProducts()).thenReturn(mockDataset);
        // Mock Spark operations...

        // When
        DailyReportEntity result = reportService.generateDailyReport(reportDate);

        // Then
        assertThat(result).isNotNull();
        verify(dailyReportRepository, times(1)).save(any(DailyReportEntity.class));
    }

    @Test
    void generateDailyReport_WhenExists_ShouldReturnExisting() {
        // Given
        LocalDate reportDate = LocalDate.now();
        DailyReportEntity existing = new DailyReportEntity();
        when(dailyReportRepository.existsByReportDate(reportDate)).thenReturn(true);
        when(dailyReportRepository.findByReportDate(reportDate)).thenReturn(Optional.of(existing));

        // When
        DailyReportEntity result = reportService.generateDailyReport(reportDate);

        // Then
        assertThat(result).isEqualTo(existing);
        verify(dailyReportRepository, never()).save(any());
    }
}
```

---

### 3. FraudDetectionServiceTest.java

```java
package com.ecommerce.analytics.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private DataReaderService dataReaderService;

    @Mock
    private Dataset<Row> mockDataset;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    @Test
    void detectFraud_WithValidThreshold_ShouldReturnOutliers() {
        // Given
        Double stdDevThreshold = 2.0;
        when(dataReaderService.readTransactions()).thenReturn(mockDataset);
        // Mock Spark statistical operations...

        // When
        Dataset<Row> result = fraudDetectionService.detectFraud(stdDevThreshold);

        // Then
        assertThat(result).isNotNull();
        verify(dataReaderService, times(1)).readTransactions();
    }
}
```

---

### 4. BatchJobControllerTest.java (MockMvc)

```java
package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.entity.BatchJobExecutionEntity;
import com.ecommerce.analytics.service.BatchJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchJobController.class)
class BatchJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchJobService batchJobService;

    @Test
    void getDashboard_ShouldReturnDashboardData() throws Exception {
        // Given
        List<BatchJobExecutionEntity> executions = Arrays.asList(new BatchJobExecutionEntity());
        when(batchJobService.getAllExecutions()).thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/api/batch/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalExecutions").exists());
    }

    @Test
    void getExecutions_WithoutFilters_ShouldReturnAll() throws Exception {
        // Given
        List<BatchJobExecutionEntity> executions = Arrays.asList(new BatchJobExecutionEntity());
        when(batchJobService.getAllExecutions()).thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/api/batch/executions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void runETL_ShouldExecutePipelineAndReturnResult() throws Exception {
        // Given
        BatchJobExecutionEntity execution = new BatchJobExecutionEntity();
        execution.setStatus("SUCCESS");
        when(batchJobService.runETLPipeline(any())).thenReturn(execution);

        // When & Then
        mockMvc.perform(post("/api/batch/etl/run"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
```

---

## 🎯 Meta de Cobertura

### Por Componente:

| Componente | Cobertura Meta | Prioridad |
|------------|----------------|-----------|
| BatchJobService | 90% | ✅ HECHO |
| ReportService | 85% | Alta |
| FraudDetectionService | 85% | Alta |
| DataReaderService | 80% | Media |
| BatchJobController | 85% | Alta |
| AnalyticsService | 80% | Media |
| PersistenceService | 80% | Media |

### Global:
- **Líneas**: 85% mínimo
- **Branches**: 80% mínimo

---

## 🚀 Pasos para Alcanzar 85% de Cobertura

### Fase 1: Tests Unitarios de Servicios (Días 1-2)
1. ✅ BatchJobServiceTest - HECHO
2. ⚠️ ReportServiceTest - Crear 6 tests
3. ⚠️ FraudDetectionServiceTest - Crear 5 tests
4. ⚠️ DataReaderServiceTest - Crear 4 tests

### Fase 2: Tests de Controladores (Día 3)
5. ⚠️ BatchJobControllerTest - Crear 7 tests con MockMvc
6. ⚠️ DataExplorationControllerTest - Crear 4 tests
7. ⚠️ FraudDetectionControllerTest - Crear 5 tests

### Fase 3: Tests de Integración (Día 4)
8. ⚠️ BatchJobIntegrationTest - Crear 3 tests end-to-end

---

## 📊 Verificar Cobertura

```bash
# 1. Ejecutar tests
mvn clean test

# 2. Ver resumen en consola
mvn jacoco:report

# 3. Abrir reporte HTML detallado
open target/site/jacoco/index.html

# 4. Verificar cumplimiento de 85%
mvn verify  # Falla si < 85%
```

---

## 💡 Tips para Tests con Spark

### Mockear Spark Datasets:

```java
@Mock
private Dataset<Row> mockDataset;

@Mock
private org.apache.spark.sql.DataFrameNaFunctions mockNaFunctions;

@BeforeEach
void setUp() {
    when(mockDataset.na()).thenReturn(mockNaFunctions);
    when(mockNaFunctions.drop()).thenReturn(mockDataset);
    when(mockDataset.dropDuplicates()).thenReturn(mockDataset);
    when(mockDataset.filter(anyString())).thenReturn(mockDataset);
}
```

### Evitar Tests de Spark Reales:

- ❌ No instanciar SparkSession en tests unitarios
- ✅ Mockear todos los Datasets y operaciones
- ✅ Usar tests de integración para pruebas reales con mini-cluster

---

## 📝 Notas Importantes

1. **JaCoCo excluye**:
   - Entidades JPA (`**/entity/**`)
   - DTOs (`**/model/**`)
   - Configuraciones (`**/config/**`)
   - Clases generadas por Lombok

2. **Tests deben ser**:
   - Independientes
   - Repetibles
   - Rápidos (< 5 segundos total)
   - Aislados (sin dependencias externas reales)

3. **Usar AssertJ** para aserciones fluidas:
```java
assertThat(result)
    .isNotNull()
    .hasFieldOrPropertyWithValue("status", "SUCCESS")
    .extracting("recordsProcessed")
    .isEqualTo(100L);
```

---

**Estado Actual**: 1/8 archivos de test implementados (~12% del total)
**Objetivo**: 8/8 archivos con 85% cobertura global
**Tiempo estimado**: 3-4 días de desarrollo
