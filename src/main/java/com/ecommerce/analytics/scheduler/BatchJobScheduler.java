package com.ecommerce.analytics.scheduler;

import com.ecommerce.analytics.service.BatchJobService;
import com.ecommerce.analytics.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Scheduler para jobs programados
 * Ejecuta tareas automáticas en intervalos configurados
 */
@Component
public class BatchJobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobScheduler.class);

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private ReportService reportService;

    /**
     * Job diario: Ejecuta ETL pipeline completo
     * Cron: Todos los días a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyETLPipeline() {
        logger.info("⏰ Iniciando job programado: Daily ETL Pipeline");
        try {
            batchJobService.runETLPipeline("scheduled_daily");
            logger.info("✅ Daily ETL Pipeline completado");
        } catch (Exception e) {
            logger.error("❌ Error en Daily ETL Pipeline: {}", e.getMessage());
        }
    }

    /**
     * Job diario: Genera reporte del día anterior
     * Cron: Todos los días a las 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void dailyReportGeneration() {
        logger.info("⏰ Iniciando job programado: Daily Report Generation");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            reportService.generateDailyReport(yesterday);
            logger.info("✅ Daily Report generado para: {}", yesterday);
        } catch (Exception e) {
            logger.error("❌ Error en Daily Report: {}", e.getMessage());
        }
    }

    /**
     * Job incremental: Procesa datos cada hora
     * Cron: Cada hora en punto
     */
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyIncrementalProcessing() {
        logger.info("⏰ Iniciando job programado: Hourly Incremental Processing");
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            batchJobService.processIncrementalData(oneHourAgo);
            logger.info("✅ Procesamiento incremental completado");
        } catch (Exception e) {
            logger.error("❌ Error en procesamiento incremental: {}", e.getMessage());
        }
    }

    /**
     * Job de monitoreo: Verifica estado del sistema cada 15 minutos
     */
    @Scheduled(fixedRate = 900000) // 15 minutos en ms
    public void systemHealthCheck() {
        logger.debug("🏥 Health check: Sistema operativo");
        // Aquí podrían agregarse validaciones de sistema, espacio en disco, etc.
    }
}
