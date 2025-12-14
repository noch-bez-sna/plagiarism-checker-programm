import java.util.regex.Pattern;

public class InputValidation {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+?[0-9\\s\\-()]{7,20}$");
    }

    public static boolean isPositiveNumber(int number) {
        return number > 0;
    }
}