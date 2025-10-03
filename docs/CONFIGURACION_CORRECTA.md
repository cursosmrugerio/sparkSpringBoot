# ⚠️ Configuración Correcta para Bloque 1 - VALIDADO

## Versiones Requeridas

**IMPORTANTE**: Estas son las versiones exactas que funcionan correctamente:

- **Java**: 17 LTS (NO usar Java 18+)
- **Spring Boot**: 3.2.0
- **Apache Spark**: 3.5.0
- **Scala**: 2.12
- **ANTLR**: 4.9.3 (versión forzada)
- **PostgreSQL**: 15

---

## 🔧 Configuración pom.xml Completa

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.ecommerce</groupId>
    <artifactId>analytics</artifactId>
    <version>1.0.0</version>
    <name>ecommerce-analytics</name>
    <description>E-commerce Analytics with Apache Spark and Spring Boot</description>

    <properties>
        <java.version>17</java.version>
        <spark.version>3.5.0</spark.version>
        <scala.binary.version>2.12</scala.binary.version>
        <hadoop.version>3.3.4</hadoop.version>
        <antlr.version>4.9.3</antlr.version>
    </properties>

    <!-- ⚠️ CRÍTICO: Dependency Management para forzar ANTLR 4.9.3 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.antlr</groupId>
                <artifactId>antlr4-runtime</artifactId>
                <version>${antlr.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Apache Spark -->
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_${scala.binary.version}</artifactId>
            <version>${spark.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-reload4j</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.apache.logging.log4j</groupId>
                    <artifactId>log4j-slf4j-impl</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_${scala.binary.version}</artifactId>
            <version>${spark.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-reload4j</artifactId>
                </exclusion>
                <!-- ⚠️ CRÍTICO: Excluir ANTLR para forzar versión compatible -->
                <exclusion>
                    <groupId>org.antlr</groupId>
                    <artifactId>antlr4-runtime</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- ⚠️ CRÍTICO: Forzar ANTLR 4.9.3 -->
        <dependency>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-runtime</artifactId>
            <version>4.9.3</version>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- ⚠️ REQUERIDO: javax.servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
        </dependency>

        <!-- ⚠️ REQUERIDO: Jersey para soporte servlets -->
        <dependency>
            <groupId>org.glassfish.jersey.core</groupId>
            <artifactId>jersey-server</artifactId>
            <version>2.40</version>
        </dependency>

        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>2.40</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                    <!-- ⚠️ CRÍTICO: JVM arguments para Java 17 -->
                    <jvmArguments>
                        --add-opens java.base/java.lang=ALL-UNNAMED
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED
                        --add-opens java.base/sun.util.calendar=ALL-UNNAMED
                    </jvmArguments>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🔧 Configuración SparkConfig.java

**CRÍTICO**: Debe deshabilitarse la UI de Spark por conflictos de servlets

```java
@Configuration
public class SparkConfig {

    @Value("${spark.app.name}")
    private String appName;

    @Value("${spark.master}")
    private String masterUri;

    @Bean
    @Profile("local")
    public SparkSession sparkSessionLocal() {
        // Fix for Java 17+ security manager issues
        System.setProperty("HADOOP_HOME", "/tmp/hadoop");
        System.setProperty("hadoop.home.dir", "/tmp/hadoop");

        return SparkSession.builder()
                .appName(appName)
                .master("local[*]")
                .config("spark.driver.host", "localhost")
                .config("spark.sql.shuffle.partitions", "4")
                .config("spark.hadoop.fs.defaultFS", "file:///")
                .config("spark.sql.warehouse.dir", "/tmp/spark-warehouse")
                .config("spark.ui.enabled", "false")  // ⚠️ CRÍTICO: Deshabilitar UI
                .getOrCreate();
    }

    @Bean
    @Profile("docker")
    public SparkSession sparkSessionDocker() {
        return SparkSession.builder()
                .appName(appName)
                .master(masterUri)
                .config("spark.submit.deployMode", "client")
                .config("spark.driver.host", "host.docker.internal")
                .config("spark.sql.shuffle.partitions", "8")
                .config("spark.ui.enabled", "false")  // ⚠️ CRÍTICO: Deshabilitar UI
                .getOrCreate();
    }
}
```

---

## 📝 Pasos de Instalación

### 1. Instalar Java 17 con SDKMAN

```bash
# Instalar SDKMAN si no lo tienes
curl -s "https://get.sdkman.io" | bash

# Abrir nueva terminal o ejecutar
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Instalar Java 17
sdk install java 17.0.13-tem

# Verificar versión
java -version
# Debe mostrar: openjdk version "17.0.13"
```

### 2. Levantar PostgreSQL con Docker

```bash
# Levantar servicios
docker-compose up -d postgres

# Verificar que esté corriendo
docker ps | grep postgres
```

### 3. Compilar el Proyecto

```bash
# Limpiar y compilar
export JAVA_HOME=~/.sdkman/candidates/java/17.0.13-tem
export PATH=$JAVA_HOME/bin:$PATH
mvn clean package -DskipTests
```

### 4. Ejecutar la Aplicación

```bash
# Con Maven (RECOMENDADO)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# O con JAR (requiere flags adicionales)
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
     -jar target/analytics-1.0.0.jar \
     --spring.profiles.active=local
```

---

## ✅ Validación de Endpoints

### 1. Health Check
```bash
curl http://localhost:8080/api/data/health
```

**Respuesta esperada:**
```json
{
  "status": "OK",
  "spark": "Running",
  "message": "Spark integration is working correctly"
}
```

### 2. Transactions
```bash
curl "http://localhost:8080/api/data/transactions?limit=5"
```

**Respuesta esperada:**
```json
{
  "totalRecords": 20,
  "schema": [
    "transaction_id: string",
    "customer_id: string",
    "product_id: string",
    "quantity: int",
    "amount: double",
    "transaction_date: timestamp",
    "payment_method: string",
    "region: string"
  ],
  "data": [
    {
      "transaction_id": "TXN001",
      "customer_id": "CUST101",
      "product_id": "PROD001",
      "quantity": 2,
      "amount": 59.98,
      "transaction_date": "2024-10-01T16:30:00.000+00:00",
      "payment_method": "credit_card",
      "region": "North"
    }
  ]
}
```

### 3. Products
```bash
curl "http://localhost:8080/api/data/products?limit=5"
```

### 4. Customers
```bash
curl "http://localhost:8080/api/data/customers?limit=5"
```

---

## 🐛 Troubleshooting

### Problema 1: Java Version Error

**Error:**
```
getSubject is supported only if a security manager is allowed
```

**Causa:** Estás usando Java 18+ (Spark 3.5.0 solo soporta hasta Java 17)

**Solución:**
```bash
sdk install java 17.0.13-tem
sdk use java 17.0.13-tem
java -version  # Verificar que sea 17.x.x
```

### Problema 2: ANTLR Version Conflict

**Error:**
```
InvalidClassException: org.antlr.v4.runtime.atn.ATN; Could not deserialize ATN with version 3 (expected 4)
```

**Causa:** Conflicto entre ANTLR 4.13 (Spring Boot 3) y ANTLR 4.9 (Spark 3.5.0)

**Solución:** Asegurarse que el pom.xml tiene:
```xml
<exclusion>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-runtime</artifactId>
</exclusion>
```

Y la dependencia forzada:
```xml
<dependency>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-runtime</artifactId>
    <version>4.9.3</version>
</dependency>
```

### Problema 3: Servlet Class Not Found

**Error:**
```
NoClassDefFoundError: javax/servlet/Servlet
```

**Causa:** Spring Boot 3 usa Jakarta EE, Spark aún requiere javax.servlet

**Solución:** Agregar dependencias al pom.xml:
```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
</dependency>

<dependency>
    <groupId>org.glassfish.jersey.core</groupId>
    <artifactId>jersey-server</artifactId>
    <version>2.40</version>
</dependency>

<dependency>
    <groupId>org.glassfish.jersey.containers</groupId>
    <artifactId>jersey-container-servlet</artifactId>
    <version>2.40</version>
</dependency>
```

### Problema 4: Module Access Errors

**Error:**
```
IllegalAccessError: class org.apache.spark.storage.StorageUtils$ cannot access class sun.nio.ch.DirectBuffer
```

**Causa:** Java 17 módulos cerrados que Spark necesita acceder

**Solución:** Usar flags JVM (ya incluidos en pom.xml):
```bash
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/sun.util.calendar=ALL-UNNAMED
```

### Problema 5: Puerto 8080 en Uso

**Error:**
```
Port 8080 is already in use
```

**Solución 1:** Cambiar puerto en `application.yml`:
```yaml
server:
  port: 8081
```

**Solución 2:** Matar proceso que usa el puerto:
```bash
lsof -ti:8080 | xargs kill -9
```

### Problema 6: PostgreSQL No Conecta

**Error:**
```
Connection refused: localhost:5432
```

**Solución:**
```bash
# Verificar que PostgreSQL esté corriendo
docker ps | grep postgres

# Si no está corriendo
docker-compose up -d postgres

# Ver logs
docker logs postgres-db
```

---

## 📊 Resumen de Dependencias Críticas

| Dependencia | Versión | ¿Por qué es crítica? |
|-------------|---------|---------------------|
| Java | **17.0.13** | Spark 3.5.0 NO soporta Java 18+ |
| ANTLR | **4.9.3** (forzada) | Spring Boot trae 4.13, incompatible con Spark |
| javax.servlet-api | **4.0.1** | Spring Boot 3 usa Jakarta, Spark requiere javax |
| Jersey | **2.40** | Soporte para servlets javax (no jakarta) |
| Spark UI | **disabled** | Conflictos de servlets irresolubles |

---

## ✅ Checklist de Validación

Antes de continuar al Bloque 2, verifica:

- [ ] Java version es 17.x.x (`java -version`)
- [ ] PostgreSQL está corriendo (`docker ps`)
- [ ] Aplicación inicia sin errores (`mvn spring-boot:run`)
- [ ] Health check retorna OK (`curl http://localhost:8080/api/data/health`)
- [ ] Endpoint transactions funciona correctamente
- [ ] Endpoint products funciona correctamente
- [ ] Endpoint customers funciona correctamente
- [ ] No aparecen errores de ANTLR en logs
- [ ] No aparecen errores de javax.servlet en logs
- [ ] No aparecen errores de module access en logs

---

**Versión:** 1.0
**Fecha:** Octubre 2025
**Estado:** ✅ Validado y Funcionando
