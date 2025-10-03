#!/bin/bash

###############################################################################
# Script de Inicio - E-commerce Analytics con Apache Spark
# Autor: Equipo de Capacitación Spark
# Versión: 1.0
# Descripción: Script automatizado para compilar y ejecutar la aplicación
#              con las configuraciones correctas de Java 17
###############################################################################

set -e  # Exit on error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║     E-commerce Analytics - Apache Spark + Spring Boot          ║"
echo "║                  Script de Inicio Automatizado                 ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Verificar Java 17
echo -e "${YELLOW}[1/5] Verificando Java 17...${NC}"
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')

if [ "$JAVA_VERSION" != "17" ]; then
    echo -e "${RED}✗ Error: Se requiere Java 17, pero tienes Java $JAVA_VERSION${NC}"
    echo -e "${YELLOW}Intentando cambiar a Java 17 con SDKMAN...${NC}"

    # Intentar cargar SDKMAN
    if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        sdk use java 17.0.13-tem 2>/dev/null || {
            echo -e "${RED}✗ Java 17.0.13 no está instalado.${NC}"
            echo -e "${YELLOW}Instálalo con: sdk install java 17.0.13-tem${NC}"
            exit 1
        }
    else
        echo -e "${RED}✗ SDKMAN no está instalado.${NC}"
        echo -e "${YELLOW}Por favor, instala Java 17 manualmente o instala SDKMAN.${NC}"
        exit 1
    fi
fi

# Configurar JAVA_HOME
if [ -d "$HOME/.sdkman/candidates/java/17.0.13-tem" ]; then
    export JAVA_HOME=$HOME/.sdkman/candidates/java/17.0.13-tem
    export PATH=$JAVA_HOME/bin:$PATH
    echo -e "${GREEN}✓ Java 17 configurado correctamente${NC}"
    java -version
else
    echo -e "${YELLOW}⚠ JAVA_HOME no configurado automáticamente${NC}"
fi

# Verificar Maven
echo -e "\n${YELLOW}[2/5] Verificando Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ Maven no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven encontrado: $(mvn -version | head -n 1)${NC}"

# Verificar PostgreSQL Docker
echo -e "\n${YELLOW}[3/5] Verificando PostgreSQL...${NC}"
if docker ps | grep -q postgres-db; then
    echo -e "${GREEN}✓ PostgreSQL está corriendo${NC}"
else
    echo -e "${YELLOW}⚠ PostgreSQL no está corriendo${NC}"
    echo -e "${YELLOW}Iniciando PostgreSQL con Docker Compose...${NC}"
    docker compose up -d postgres
    echo -e "${GREEN}✓ PostgreSQL iniciado${NC}"
    sleep 3
fi

# Compilar proyecto
echo -e "\n${YELLOW}[4/5] Compilando proyecto con Maven...${NC}"
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Compilación exitosa${NC}"
else
    echo -e "${RED}✗ Error en la compilación${NC}"
    exit 1
fi

# Ejecutar aplicación
echo -e "\n${YELLOW}[5/5] Iniciando aplicación Spring Boot...${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Aplicación iniciando en:${NC} http://localhost:8080"
echo -e "${GREEN}Profile activo:${NC} local"
echo -e "${GREEN}Endpoints disponibles:${NC}"
echo -e "  - Health check:   http://localhost:8080/api/data/health"
echo -e "  - Transacciones:  http://localhost:8080/api/data/transactions"
echo -e "  - Productos:      http://localhost:8080/api/data/products"
echo -e "  - Clientes:       http://localhost:8080/api/data/customers"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}\n"

# JVM Arguments para Java 17 (CRÍTICO)
JVM_ARGS="--add-opens java.base/java.lang=ALL-UNNAMED \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.base/sun.util.calendar=ALL-UNNAMED"

# Ejecutar JAR
java $JVM_ARGS \
    -jar target/analytics-1.0.0.jar \
    --spring.profiles.active=local
