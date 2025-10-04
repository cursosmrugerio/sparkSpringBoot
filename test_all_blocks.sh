#!/bin/bash

# Script de pruebas completas para validar Bloques 1-4
# Proyecto: E-commerce Analytics con Apache Spark

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║     SUITE DE PRUEBAS COMPLETA - BLOQUES 1-4                  ║"
echo "║     E-commerce Analytics con Apache Spark                     ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "Fecha de ejecución: $(date)"
echo ""

# Contadores de resultados
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para ejecutar test
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_http_code="${3:-200}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🧪 Test $TOTAL_TESTS: $test_name"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Ejecutar el comando y capturar código HTTP
    http_code=$(eval "$test_command" -w '%{http_code}' -o /tmp/test_response_$TOTAL_TESTS.json 2>/dev/null)

    if [ "$http_code" = "$expected_http_code" ]; then
        echo "✅ PASS - HTTP $http_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))

        # Mostrar preview de la respuesta
        if [ -f /tmp/test_response_$TOTAL_TESTS.json ]; then
            echo "📋 Response preview:"
            head -c 500 /tmp/test_response_$TOTAL_TESTS.json 2>/dev/null | python3 -m json.tool 2>/dev/null | head -20 || cat /tmp/test_response_$TOTAL_TESTS.json | head -5
        fi
    else
        echo "❌ FAIL - Expected HTTP $expected_http_code, got $http_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        if [ -f /tmp/test_response_$TOTAL_TESTS.json ]; then
            echo "Response: $(cat /tmp/test_response_$TOTAL_TESTS.json)"
        fi
    fi
    echo ""
}

# ============================================================================
# BLOQUE 1: Operaciones Básicas con Spark
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║          BLOQUE 1: Operaciones Básicas con Spark            ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "1.1 - Leer todas las transacciones" \
    "curl -s http://localhost:8080/api/data/transactions"

run_test "1.2 - Leer todos los productos" \
    "curl -s http://localhost:8080/api/data/products"

run_test "1.3 - Leer todos los clientes" \
    "curl -s http://localhost:8080/api/data/customers"

run_test "1.4 - Health check de Spark" \
    "curl -s http://localhost:8080/api/data/health"

run_test "1.5 - Transacciones limpias (optimización)" \
    "curl -s http://localhost:8080/api/optimization/transactions/clean"

# ============================================================================
# BLOQUE 2: Transformaciones y Agregaciones
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║       BLOQUE 2: Transformaciones y Agregaciones              ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "2.1 - Ventas totales por categoría" \
    "curl -s http://localhost:8080/api/sales/by-category"

run_test "2.2 - Top 5 productos más vendidos" \
    "curl -s 'http://localhost:8080/api/products/top-selling?limit=5'"

run_test "2.3 - Ventas por región" \
    "curl -s http://localhost:8080/api/sales/by-region"

run_test "2.4 - Estadísticas de ventas" \
    "curl -s http://localhost:8080/api/sales/statistics"

run_test "2.5 - Resumen diario de ventas" \
    "curl -s http://localhost:8080/api/sales/daily-summary"

# ============================================================================
# BLOQUE 3: UDFs, Persistencia y Optimización
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║      BLOQUE 3: UDFs, Persistencia y Optimización            ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "3.1 - Detección de fraude y persistencia" \
    "curl -s -X POST http://localhost:8080/api/fraud/detect-and-save"

run_test "3.2 - Obtener todas las alertas de fraude" \
    "curl -s 'http://localhost:8080/api/fraud/alerts?onlyUnreviewed=false'"

run_test "3.3 - Alertas de alto riesgo" \
    "curl -s http://localhost:8080/api/fraud/alerts/high-risk"

run_test "3.4 - Estadísticas de fraude" \
    "curl -s http://localhost:8080/api/fraud/statistics"

run_test "3.5 - Patrones de fraude por cliente" \
    "curl -s http://localhost:8080/api/fraud/customer-patterns"

run_test "3.6 - Detección de duplicados sospechosos" \
    "curl -s http://localhost:8080/api/fraud/duplicates"

run_test "3.7 - Transacciones enriquecidas (optimización)" \
    "curl -s http://localhost:8080/api/optimization/transactions/enriched"

run_test "3.8 - Estadísticas de persistencia" \
    "curl -s http://localhost:8080/api/persistence/stats"

run_test "3.9 - Obtener reportes de ventas guardados" \
    "curl -s http://localhost:8080/api/persistence/reports"

run_test "3.10 - Performance de productos" \
    "curl -s http://localhost:8080/api/persistence/products/performance"

run_test "3.11 - Top productos por ingresos" \
    "curl -s 'http://localhost:8080/api/persistence/products/top-revenue?limit=5'"

run_test "3.12 - Broadcast join de transacciones" \
    "curl -s http://localhost:8080/api/optimization/transactions/broadcast-join"

# ============================================================================
# BLOQUE 4: Batch Processing y Automatización
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║       BLOQUE 4: Batch Processing y Automatización            ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "4.1 - Dashboard de batch jobs" \
    "curl -s http://localhost:8080/api/batch/dashboard"

run_test "4.2 - Ejecutar ETL pipeline" \
    "curl -s -X POST http://localhost:8080/api/batch/etl/run"

run_test "4.3 - Procesamiento incremental" \
    "curl -s -X POST 'http://localhost:8080/api/batch/incremental/run?since=2025-01-01T00:00:00'"

run_test "4.4 - Generar reporte diario" \
    "curl -s -X POST 'http://localhost:8080/api/batch/report/generate?reportDate=2025-01-15'"

run_test "4.5 - Historial de ejecuciones" \
    "curl -s http://localhost:8080/api/batch/executions"

run_test "4.6 - Consultar reportes por rango de fechas" \
    "curl -s 'http://localhost:8080/api/batch/reports?startDate=2025-01-01&endDate=2025-01-31'"

# ============================================================================
# TESTS ADICIONALES DE INTEGRACIÓN
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║              TESTS DE INTEGRACIÓN                            ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "INT.1 - Health check de Spark (reemplazo de Actuator)" \
    "curl -s http://localhost:8080/api/data/health"

run_test "INT.2 - Filtrar ejecuciones por job name" \
    "curl -s 'http://localhost:8080/api/batch/executions?jobName=ETL_DAILY_PIPELINE'"

run_test "INT.3 - Filtrar ejecuciones por status" \
    "curl -s 'http://localhost:8080/api/batch/executions?status=SUCCESS'"

run_test "INT.4 - Análisis de producto específico" \
    "curl -s http://localhost:8080/api/products/PROD001/analytics"

run_test "INT.5 - Patrones de fraude por producto" \
    "curl -s http://localhost:8080/api/fraud/product-patterns"

# ============================================================================
# RESUMEN DE RESULTADOS
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                    RESUMEN DE RESULTADOS                     ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "📊 Total de tests ejecutados: $TOTAL_TESTS"
echo "✅ Tests exitosos: $PASSED_TESTS"
echo "❌ Tests fallidos: $FAILED_TESTS"
echo ""

# Calcular porcentaje de éxito
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$(echo "scale=2; ($PASSED_TESTS * 100) / $TOTAL_TESTS" | bc)
    echo "📈 Tasa de éxito: $SUCCESS_RATE%"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Determinar resultado final
if [ $FAILED_TESTS -eq 0 ]; then
    echo "🎉 RESULTADO FINAL: TODOS LOS TESTS PASARON ✅"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 0
else
    echo "⚠️  RESULTADO FINAL: ALGUNOS TESTS FALLARON ❌"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 1
fi
