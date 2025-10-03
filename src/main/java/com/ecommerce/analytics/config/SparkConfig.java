package com.ecommerce.analytics.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
                .config("spark.ui.enabled", "false")  // Disable Spark UI to avoid servlet issues
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
                .getOrCreate();
    }
}
