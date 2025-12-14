import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateHelper {

    // Вычисление возраста
    public static int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Проверка високосного года
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // Форматирование даты
    public static String formatDate(LocalDate date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    // Разница между датами в днях
    public static long daysBetween(LocalDate date1, LocalDate date2) {
        return ChronoUnit.DAYS.between(date1, date2);
    }

    // Является ли дата выходным
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() > 5;
    }

    // Добавление рабочих дней
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int added = 0;
        while (added < days) {
            result = result.plusDays(1);
            if (!isWeekend(result)) {
                added++;
            }
        }
        return result;
    }
}