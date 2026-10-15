package com.escola.biblioteca.job;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BatchJobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchJobRunner.class);

    private final CirculationJobService jobService;
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        String jobName = resolveJobName(args);
        if (jobName == null || jobName.isBlank()) {
            return;
        }

        log.info("--------------------------------------------------");
        log.info("BatchJobRunner ativado para o job: '{}'", jobName);
        log.info("--------------------------------------------------");

        int exitCode = executeJob(jobName);

        final int code = exitCode;
        int springExit = SpringApplication.exit(applicationContext, () -> code);
        exitProcess(springExit);
    }

    public int executeJob(String jobName) {
        try {
            switch (jobName.toLowerCase()) {
                case "daily-maintenance", "all" -> jobService.runDailyMaintenance();
                case "hold-expiry" -> jobService.runHoldExpiry();
                case "overdue-detection" -> jobService.runOverdueDetection();
                case "fine-accrual" -> jobService.runFineAccrual();
                case "auto-renewal" -> jobService.runAutoRenewal();
                case "patron-block" -> jobService.runPatronBlockCheck();
                case "cleanup" -> jobService.runCleanup();
                default -> {
                    log.error("Job desconhecido: '{}'. Opções válidas: daily-maintenance, hold-expiry, overdue-detection, fine-accrual, auto-renewal, patron-block, cleanup", jobName);
                    return 1;
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("Falha fatal na execução do job '{}': {}", jobName, e.getMessage(), e);
            return 1;
        }
    }

    protected void exitProcess(int code) {
        System.exit(code);
    }

    private String resolveJobName(ApplicationArguments args) {
        if (args.containsOption("job")) {
            List<String> values = args.getOptionValues("job");
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }
        }
        String sysProp = System.getProperty("job");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        return System.getenv("APP_JOB");
    }
}
