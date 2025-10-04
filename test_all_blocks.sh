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
    "curl -s http://localhost:8080/api/spark/transactions"

run_test "1.2 - Leer todos los productos" \
    "curl -s http://localhost:8080/api/spark/products"

run_test "1.3 - Leer todos los clientes" \
    "curl -s http://localhost:8080/api/spark/customers"

run_test "1.4 - Filtrar transacciones por región (Norte)" \
    "curl -s 'http://localhost:8080/api/spark/transactions/by-region?region=Norte'"

run_test "1.5 - Obtener transacciones de alto valor (>300)" \
    "curl -s 'http://localhost:8080/api/spark/transactions/high-value?minAmount=300'"

# ============================================================================
# BLOQUE 2: Transformaciones y Agregaciones
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║       BLOQUE 2: Transformaciones y Agregaciones              ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

run_test "2.1 - Ventas totales por categoría" \
    "curl -s http://localhost:8080/api/spark/sales/by-category"

run_test "2.2 - Top 5 productos más vendidos" \
    "curl -s 'http://localhost:8080/api/spark/products/top?limit=5'"

run_test "2.3 - Ventas por región" \
    "curl -s http://localhost:8080/api/spark/sales/by-region"

run_test "2.4 - Estadísticas de ventas" \
    "curl -s http://localhost:8080/api/spark/sales/stats"

run_test "2.5 - Productos con descuento aplicado" \
    "curl -s http://localhost:8080/api/spark/products/with-discount"

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

run_test "3.2 - Obtener alertas de fraude no revisadas" \
    "curl -s http://localhost:8080/api/fraud/alerts/pending"

run_test "3.3 - Alertas de alto riesgo" \
    "curl -s http://localhost:8080/api/fraud/alerts/high-risk"

run_test "3.4 - Validar emails de clientes" \
    "curl -s http://localhost:8080/api/customers/validate-emails"

run_test "3.5 - Categorizar montos de transacciones" \
    "curl -s http://localhost:8080/api/transactions/categorize-amounts"

run_test "3.6 - Detección de duplicados" \
    "curl -s http://localhost:8080/api/fraud/detect-duplicates"

run_test "3.7 - Limpiar datos (eliminar nulls/duplicados)" \
    "curl -s -X POST http://localhost:8080/api/data/clean"

run_test "3.8 - Dashboard de estadísticas" \
    "curl -s http://localhost:8080/api/dashboard/stats"

run_test "3.9 - Guardar reporte de ventas" \
    "curl -s -X POST http://localhost:8080/api/reports/sales/save"

run_test "3.10 - Obtener todos los reportes de ventas" \
    "curl -s http://localhost:8080/api/reports/sales"

run_test "3.11 - Análisis de performance de productos" \
    "curl -s -X POST http://localhost:8080/api/products/analyze-performance"

run_test "3.12 - Obtener productos de alto rendimiento" \
    "curl -s 'http://localhost:8080/api/products/performance/top?limit=5'"

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

run_test "INT.1 - Verificar health check de aplicación" \
    "curl -s http://localhost:8080/actuator/health"

run_test "INT.2 - Filtrar ejecuciones por job name" \
    "curl -s 'http://localhost:8080/api/batch/executions?jobName=ETL_DAILY_PIPELINE'"

run_test "INT.3 - Filtrar ejecuciones por status" \
    "curl -s 'http://localhost:8080/api/batch/executions?status=SUCCESS'"

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
