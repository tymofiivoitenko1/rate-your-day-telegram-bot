package com.tymofiivoitenko.rateyourdaybot.telegram.job;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@AllArgsConstructor
public class GenerateMonthRateJob {

    public static final int UKRAINE_UTC_OFFSET = 3;

    public static final ZoneId SYSTEM_ZONE_ID = ZoneOffset.ofHours(UKRAINE_UTC_OFFSET);

    private final GenerateMonthRateJobHelper helper;

    private final ScheduledExecutorService executorService;


    @Autowired
    public GenerateMonthRateJob(GenerateMonthRateJobHelper helper) {
        this.helper = helper;
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void init() {
        executeNext();
    }

    private void executeNext() {
        Runnable task = () -> {
            helper.sendMonthRates();
            executeNext();
        };
        var delay = calculateDelay();

        this.executorService.schedule(task, delay, TimeUnit.SECONDS);
    }

    private long calculateDelay() {
        var localNow = LocalDateTime.now();
        var zonedNow = ZonedDateTime.of(localNow, SYSTEM_ZONE_ID);
        var zonedNextTarget = zonedNow.plusMonths(1).withDayOfMonth(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        var delay = Duration.between(zonedNow, zonedNextTarget).getSeconds() + 1;

        log.info("Send MonthRates in {} seconds", delay);
        return delay;
    }

}
