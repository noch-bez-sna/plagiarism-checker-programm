public class ImageProcessor {

    // Конвертация в ASCII арт
    public static String convertToAsciiArt(int[][] pixels) {
        StringBuilder art = new StringBuilder();
        char[] asciiChars = {' ', '.', ':', '-', '=', '+', '*', '#', '%', '@'};

        for (int[] row : pixels) {
            for (int pixel : row) {
                int index = pixel * (asciiChars.length - 1) / 255;
                art.append(asciiChars[index]);
            }
            art.append('\n');
        }
        return art.toString();
    }

    // Инвертирование цвета
    public static int[][] invertColors(int[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;
        int[][] inverted = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                inverted[i][j] = 255 - pixels[i][j];
            }
        }
        return inverted;
    }

    // Поворот изображения на 90 градусов
    public static int[][] rotate90(int[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;
        int[][] rotated = new int[width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rotated[j][height - 1 - i] = pixels[i][j];
            }
        }
        return rotated;
    }

    // Размытие изображения
    public static int[][] blurImage(int[][] pixels, int radius) {
        int height = pixels.length;
        int width = pixels[0].length;
        int[][] blurred = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int sum = 0;
                int count = 0;

                for (int ki = -radius; ki <= radius; ki++) {
                    for (int kj = -radius; kj <= radius; kj++) {
                        int ni = i + ki;
                        int nj = j + kj;

                        if (ni >= 0 && ni < height && nj >= 0 && nj < width) {
                            sum += pixels[ni][nj];
                            count++;
                        }
                    }
                }

                blurred[i][j] = sum / count;
            }
        }
        return blurred;
    }
}