package pl.marcinm312.springquestionsanswers.shared.testdataprovider;

import java.time.LocalDateTime;
import java.time.Month;

public class DateProvider {

    public static LocalDateTime prepareDate(int year, Month month, int day, int hour, int minute, int second) {

        return LocalDateTime.of(year, month, day, hour, minute, second);
    }
}
