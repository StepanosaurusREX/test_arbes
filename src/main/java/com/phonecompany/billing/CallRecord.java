package com.phonecompany.billing;

import java.time.LocalDateTime;

public record CallRecord(String number, LocalDateTime start, LocalDateTime end) {
}
