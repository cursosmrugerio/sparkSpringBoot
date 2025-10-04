package com.ecommerce.analytics.service;

import com.ecommerce.analytics.entity.BatchJobExecutionEntity;
import com.ecommerce.analytics.repository.BatchJobExecutionRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobServiceTest {

    @Mock
    private SparkSession sparkSession;

    @Mock
    private DataReaderService dataReaderService;

    @Mock
    private BatchJobExecutionRepository batchJobExecutionRepository;

    @Mock
    private Dataset<Row> mockDataset;

    @InjectMocks
    private BatchJobService batchJobService;

    private BatchJobExecutionEntity mockExecution;

    @BeforeEach
    void setUp() {
        mockExecution = new BatchJobExecutionEntity();
        mockExecution.setId(1L);
        mockExecution.setJobName("ETL_DAILY_PIPELINE");
        mockExecution.setStatus("RUNNING");
        mockExecution.setStartTime(LocalDateTime.now());
    }

    @Test
    void startJobExecution_ShouldCreateAndSaveJobExecution() {
        // Given
        String jobName = "TEST_JOB";
        String jobParams = "test-params";
        when(batchJobExecutionRepository.save(any(BatchJobExecutionEntity.class)))
            .thenReturn(mockExecution);

        // When
        BatchJobExecutionEntity result = batchJobService.startJobExecution(jobName, jobParams);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("RUNNING");
        verify(batchJobExecutionRepository, times(1)).save(any(BatchJobExecutionEntity.class));
    }

    @Test
    void completeJobExecution_ShouldUpdateStatusToSuccess() {
        // Given
        Long recordsProcessed = 100L;
        Long recordsFailed = 0L;
        when(batchJobExecutionRepository.save(any(BatchJobExecutionEntity.class)))
            .thenReturn(mockExecution);

        // When
        batchJobService.completeJobExecution(mockExecution, recordsProcessed, recordsFailed);

        // Then
        assertThat(mockExecution.getStatus()).isEqualTo("SUCCESS");
        assertThat(mockExecution.getRecordsProcessed()).isEqualTo(recordsProcessed);
        assertThat(mockExecution.getRecordsFailed()).isEqualTo(recordsFailed);
        assertThat(mockExecution.getEndTime()).isNotNull();
        assertThat(mockExecution.getDurationMs()).isNotNull();
        verify(batchJobExecutionRepository, times(1)).save(mockExecution);
    }

    @Test
    void failJobExecution_ShouldUpdateStatusToFailed() {
        // Given
        String errorMessage = "Test error";

        // When
        batchJobService.failJobExecution(mockExecution, errorMessage);

        // Then
        assertThat(mockExecution.getStatus()).isEqualTo("FAILED");
        assertThat(mockExecution.getErrorMessage()).contains(errorMessage);
        assertThat(mockExecution.getEndTime()).isNotNull();
        verify(batchJobExecutionRepository, times(1)).save(mockExecution);
    }

    @Test
    void getExecutionsByJobName_ShouldReturnFilteredExecutions() {
        // Given
        String jobName = "ETL_PIPELINE";
        List<BatchJobExecutionEntity> executions = Arrays.asList(mockExecution);
        when(batchJobExecutionRepository.findByJobName(jobName)).thenReturn(executions);

        // When
        List<BatchJobExecutionEntity> result = batchJobService.getExecutionsByJobName(jobName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobName()).isEqualTo("ETL_DAILY_PIPELINE");
        verify(batchJobExecutionRepository, times(1)).findByJobName(jobName);
    }

    @Test
    void getExecutionsByStatus_ShouldReturnFilteredExecutions() {
        // Given
        String status = "SUCCESS";
        List<BatchJobExecutionEntity> executions = Arrays.asList(mockExecution);
        when(batchJobExecutionRepository.findByStatus(status)).thenReturn(executions);

        // When
        List<BatchJobExecutionEntity> result = batchJobService.getExecutionsByStatus(status);

        // Then
        assertThat(result).hasSize(1);
        verify(batchJobExecutionRepository, times(1)).findByStatus(status);
    }

    @Test
    void getAllExecutions_ShouldReturnAllExecutions() {
        // Given
        List<BatchJobExecutionEntity> executions = Arrays.asList(mockExecution);
        when(batchJobExecutionRepository.findAllByOrderByStartTimeDesc()).thenReturn(executions);

        // When
        List<BatchJobExecutionEntity> result = batchJobService.getAllExecutions();

        // Then
        assertThat(result).hasSize(1);
        verify(batchJobExecutionRepository, times(1)).findAllByOrderByStartTimeDesc();
    }

    @Test
    void processIncrementalData_WithValidDate_ShouldReturnSuccess() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        when(batchJobExecutionRepository.save(any(BatchJobExecutionEntity.class)))
            .thenReturn(mockExecution);
        when(dataReaderService.readTransactions()).thenReturn(mockDataset);
        when(dataReaderService.readProducts()).thenReturn(mockDataset);
        when(mockDataset.filter(anyString())).thenReturn(mockDataset);
        when(mockDataset.join(any(), any())).thenReturn(mockDataset);
        when(mockDataset.groupBy(anyString())).thenReturn(mock(org.apache.spark.sql.RelationalGroupedDataset.class));
        when(mockDataset.count()).thenReturn(0L);

        // When
        BatchJobExecutionEntity result = batchJobService.processIncrementalData(since);

        // Then
        assertThat(result).isNotNull();
        verify(dataReaderService, times(1)).readTransactions();
    }

    @Test
    void cleanAndValidateData_ShouldRemoveNullsAndDuplicates() {
        // Given
        when(mockDataset.na()).thenReturn(mock(org.apache.spark.sql.DataFrameNaFunctions.class));
        when(mockDataset.na().drop()).thenReturn(mockDataset);
        when(mockDataset.dropDuplicates()).thenReturn(mockDataset);

        // When
        Dataset<Row> result = batchJobService.cleanAndValidateData(mockDataset);

        // Then
        assertThat(result).isNotNull();
        verify(mockDataset, times(1)).dropDuplicates();
    }
}
