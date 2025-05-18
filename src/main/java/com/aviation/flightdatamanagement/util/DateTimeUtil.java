package com.aviation.flightdatamanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static final ZoneId CET_ZONE_ID = ZoneId.of("CET");

    // Converts a LocalDateTime in CET to OffsetDateTime in UTC
    public static OffsetDateTime fromCetToUtc(LocalDateTime cetDateTime) {
        if (cetDateTime == null) return null;
        return cetDateTime.atZone(CET_ZONE_ID)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime();
    }

    // Converts an OffsetDateTime (assumed UTC or with offset) to LocalDate in CET
    public static LocalDate toCetLocalDate(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.atZoneSameInstant(CET_ZONE_ID).toLocalDate();
    }

    // Parses ISO_DATE_TIME string to OffsetDateTime
    public static OffsetDateTime parseIsoDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) return null;
        return OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
    }
}