package br.com.utfpr.gerenciamento.server.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDate parseStringToLocalDate(String dt) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dt, dateTimeFormatter);
    }

    public static String parseLocalDateToString(LocalDate dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dt.format(formatter);
    }
}
