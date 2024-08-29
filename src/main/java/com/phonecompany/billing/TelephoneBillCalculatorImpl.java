package com.phonecompany.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final BigDecimal STANDARD_TIME_COST = new BigDecimal("1.0");
    private static final BigDecimal DIFFERENT_TIME_COST = new BigDecimal("0.5");
    private static final BigDecimal COST_AFTER_5_MINUTES = new BigDecimal("0.2");

    private static final int STANDARD_TIME_WINDOW_START = 8;
    private static final int STANDARD_TIME_WINDOW_END = 16;

    @Override
    public BigDecimal calculate(String phoneLog) {
        List<CallRecord> records = parsePhoneLog(phoneLog);
        String mostCommonNumber = determineMostCommonNumber(records);

        BigDecimal finalCost = BigDecimal.ZERO;
        for (CallRecord record : records) {
            if (record.number().equals(mostCommonNumber)) {
                continue;
            }

            finalCost = finalCost.add(recordCost(record));
        }
        return finalCost.setScale(2, RoundingMode.HALF_UP); // Ensuring two decimal places
    }

    private String determineMostCommonNumber(List<CallRecord> records) {
        Map<String, Integer> numberOfCalls = new HashMap<>();

        records.forEach(record -> numberOfCalls.put(record.number(), numberOfCalls.getOrDefault(record.number(), 0) + 1));

        int maxNumberOfCalls = numberOfCalls.values().stream().max(Integer::compareTo).orElse(0);

        List<String> mostCommonPhoneNumbers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : numberOfCalls.entrySet()) {
            if (entry.getValue() == maxNumberOfCalls) {
                mostCommonPhoneNumbers.add(entry.getKey());
            }
        }

        return getNumberWithHighestArithmeticValue(mostCommonPhoneNumbers);
    }

    private String getNumberWithHighestArithmeticValue(List<String> phoneNumbers) {
        return phoneNumbers.stream()
                .map(Long::parseLong)
                .max(Long::compareTo)
                .map(String::valueOf)
                .orElse("");
    }

    private BigDecimal recordCost(CallRecord record) {
        Duration callDuration = Duration.between(record.start(), record.end());
        long callDurationSeconds = callDuration.getSeconds();
        long callDurationMinutes = (long) Math.ceil(callDurationSeconds / 60.0);

        LocalDateTime currentMinute = record.start();
        BigDecimal callCost = BigDecimal.ZERO;

        for (long i = 0; i < callDurationMinutes; i++) {
            int currentHour = currentMinute.getHour();
            BigDecimal costPerMinute;

            if (i < 5) {
                if (currentHour >= STANDARD_TIME_WINDOW_START && currentHour < STANDARD_TIME_WINDOW_END) {
                    costPerMinute = STANDARD_TIME_COST;
                } else {
                    costPerMinute = DIFFERENT_TIME_COST;
                }
            } else {
                costPerMinute = COST_AFTER_5_MINUTES;
            }

            callCost = callCost.add(costPerMinute);
            currentMinute = currentMinute.plusMinutes(1);
        }

        return callCost.setScale(2, RoundingMode.HALF_UP); // Ensuring two decimal places
    }

    private List<CallRecord> parsePhoneLog(String phoneLog) {
        String[] logs = phoneLog.split("\n");
        List<CallRecord> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        for (String log : logs) {
            String[] logElements = log.split(",");

            String number = logElements[0];
            LocalDateTime start = LocalDateTime.parse(logElements[1], formatter);
            LocalDateTime end = LocalDateTime.parse(logElements[2], formatter);

            records.add(new CallRecord(number, start, end));
        }

        return records;
    }
}
