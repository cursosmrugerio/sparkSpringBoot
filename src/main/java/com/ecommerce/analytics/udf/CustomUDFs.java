package com.ecommerce.analytics.udf;

import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * User Defined Functions (UDFs) personalizadas para Spark
 *
 * Implementa funciones de validación y transformación:
 * - Validación de emails
 * - Categorización de montos
 * - Detección de fraude básico
 */
@Component
public class CustomUDFs implements Serializable {

    private static final long serialVersionUID = 1L;

    // Patrón regex para validar emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * UDF 1: Validación de email
     *
     * @return true si el email es válido, false en caso contrario
     */
    public static class ValidateEmail implements UDF1<String, Boolean>, Serializable {
        @Override
        public Boolean call(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return EMAIL_PATTERN.matcher(email.trim()).matches();
        }
    }

    /**
     * UDF 2: Categorización de montos
     *
     * Clasifica transacciones en:
     * - BAJO: < 50
     * - MEDIO: 50-200
     * - ALTO: 200-500
     * - MUY_ALTO: > 500
     */
    public static class CategorizeAmount implements UDF1<Double, String>, Serializable {
        @Override
        public String call(Double amount) {
            if (amount == null) {
                return "DESCONOCIDO";
            }

            if (amount < 50) {
                return "BAJO";
            } else if (amount < 200) {
                return "MEDIO";
            } else if (amount < 500) {
                return "ALTO";
            } else {
                return "MUY_ALTO";
            }
        }
    }

    /**
     * UDF 3: Detección de fraude básico
     *
     * Detecta transacciones sospechosas basándose en:
     * - Monto muy alto (> 1000)
     * - Cantidad excesiva (> 10 unidades)
     */
    public static class DetectFraud implements UDF2<Double, Integer, String>, Serializable {
        @Override
        public String call(Double amount, Integer quantity) {
            if (amount == null || quantity == null) {
                return "DESCONOCIDO";
            }

            boolean highAmount = amount > 1000;
            boolean highQuantity = quantity > 10;

            if (highAmount && highQuantity) {
                return "ALTO_RIESGO";
            } else if (highAmount || highQuantity) {
                return "MEDIO_RIESGO";
            } else {
                return "BAJO_RIESGO";
            }
        }
    }

    /**
     * UDF 4: Normalizar strings (trim + uppercase)
     */
    public static class NormalizeString implements UDF1<String, String>, Serializable {
        @Override
        public String call(String input) {
            if (input == null) {
                return "";
            }
            return input.trim().toUpperCase();
        }
    }

    /**
     * UDF 5: Calcular descuento por categoría de monto
     *
     * Retorna % de descuento según categoría:
     * - BAJO: 0%
     * - MEDIO: 5%
     * - ALTO: 10%
     * - MUY_ALTO: 15%
     */
    public static class CalculateDiscount implements UDF1<Double, Double>, Serializable {
        @Override
        public Double call(Double amount) {
            if (amount == null) {
                return 0.0;
            }

            if (amount < 50) {
                return 0.0;
            } else if (amount < 200) {
                return 5.0;
            } else if (amount < 500) {
                return 10.0;
            } else {
                return 15.0;
            }
        }
    }
}
