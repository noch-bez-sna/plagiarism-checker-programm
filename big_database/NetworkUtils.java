import java.net.InetAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetworkUtils {

    // Проверка доступности хоста
    public static boolean isHostReachable(String host) {
        try {
            return InetAddress.getByName(host).isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }

    // Получение IP адреса
    public static String getIPAddress(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    // Простой HTTP GET запрос
    public static String httpGet(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return response.toString();
    }

    // Проверка валидности URL
    public static boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}