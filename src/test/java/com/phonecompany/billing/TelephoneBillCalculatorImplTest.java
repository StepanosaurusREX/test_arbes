package com.phonecompany.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneBillCalculatorImplTest {

    private final TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();

    //Single call should cost nothing because the number happens to be most common and according to promo free
    @Test
    public void testCalculateSingleCall() {
        String phoneLog = "1234567890,01-01-2024 08:00:00,01-01-2024 08:04:30";
        BigDecimal expectedCost = new BigDecimal("0.00");
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateMultipleCallsWithOnlyOneMostCommon() {
        String phoneLog = "1234567890,01-01-2024 08:00:00,01-01-2024 08:03:00\n" +
                "1234567890,01-01-2024 09:00:00,01-01-2024 09:07:00\n" +
                "0987654321,01-01-2024 09:30:00,01-01-2024 09:34:30";
        BigDecimal expectedCost = new BigDecimal("5.00"); // Only one number, 1234567890 is the most common and should be excluded
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateMostCommonNumberWithMultipleOccurrences() {
        String phoneLog = "1234567890,01-01-2024 08:00:00,01-01-2024 08:03:00\n" +
                "1234567890,01-01-2024 09:00:00,01-01-2024 09:07:00\n" +
                "0987654321,01-01-2024 09:30:00,01-01-2024 09:34:30\n" +
                "0987654321,01-01-2024 10:00:00,01-01-2024 10:09:30";
        BigDecimal expectedCost = new BigDecimal("11.00"); // Exclude 1234567890 as it's the most common and highest arithmetically
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateCallsStandardTime() {
        String phoneLog = "0987654321,01-01-2024 08:10:00,01-01-2024 08:12:45\n" +
                "1234567890,01-01-2024 08:00:00,01-01-2024 08:10:00";
        BigDecimal expectedCost = new BigDecimal("3.00"); // 2*0.5 + 3*1
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateCallsOutsideStandardTime() {
        String phoneLog = "0987654321,01-01-2024 07:10:00,01-01-2024 07:12:45\n" +
                "1234567890,01-01-2024 08:00:00,01-01-2024 08:10:00";
        BigDecimal expectedCost = new BigDecimal("1.50"); // 2*0.5 + 3*1
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateCallsSpanningAcrossTimeWindows() {
        String phoneLog = "0987654321,01-01-2024 07:58:00,01-01-2024 08:02:45\n" +
                "1234567890,01-01-2024 08:00:00,01-01-2024 08:10:00";
        BigDecimal expectedCost = new BigDecimal("4.00"); // 2*0.5 + 3*1
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }

    @Test
    public void testCalculateCallsLongerThan5Minutes() {
        String phoneLog = "0987654321,01-01-2024 08:00:00,01-01-2024 08:09:45\n" +
                "1234567890,01-01-2024 08:00:00,01-01-2024 08:10:00";
        BigDecimal expectedCost = new BigDecimal("6.00"); //5*1.0 + 5*0.2
        assertEquals(expectedCost, calculator.calculate(phoneLog));
    }


}