// BinaryFileProcessing.java - Работа с бинарными файлами
public class BinaryFileProcessing {

    // Запись примитивных типов в бинарный файл
    public void writePrimitives(String filename) {
        try (java.io.DataOutputStream dos = new java.io.DataOutputStream(
                new java.io.FileOutputStream(filename))) {

            dos.writeInt(42);
            dos.writeDouble(3.14159);
            dos.writeBoolean(true);
            dos.writeUTF("Hello, Binary World!");

            System.out.println("Primitives written to file");
        } catch (java.io.IOException e) {
            System.out.println("Error writing primitives: " + e.getMessage());
        }
    }

    // Чтение примитивных типов из бинарного файла
    public void readPrimitives(String filename) {
        try (java.io.DataInputStream dis = new java.io.DataInputStream(
                new java.io.FileInputStream(filename))) {

            int intValue = dis.readInt();
            double doubleValue = dis.readDouble();
            boolean booleanValue = dis.readBoolean();
            String stringValue = dis.readUTF();

            System.out.println("Int: " + intValue);
            System.out.println("Double: " + doubleValue);
            System.out.println("Boolean: " + booleanValue);
            System.out.println("String: " + stringValue);

        } catch (java.io.IOException e) {
            System.out.println("Error reading primitives: " + e.getMessage());
        }
    }

    // Сериализация объекта в файл
    public void serializeObject(String filename, Object obj) {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(filename))) {

            oos.writeObject(obj);
            System.out.println("Object serialized successfully");

        } catch (java.io.IOException e) {
            System.out.println("Error serializing object: " + e.getMessage());
        }
    }

    // Десериализация объекта из файла
    public Object deserializeObject(String filename) {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(filename))) {

            Object obj = ois.readObject();
            System.out.println("Object deserialized successfully");
            return obj;

        } catch (java.io.IOException | ClassNotFoundException e) {
            System.out.println("Error deserializing object: " + e.getMessage());
            return null;
        }
    }

    // Запись байтового массива в файл
    public void writeByteArray(String filename, byte[] data) {
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filename)) {
            fos.write(data);
            System.out.println("Byte array written to file");
        } catch (java.io.IOException e) {
            System.out.println("Error writing byte array: " + e.getMessage());
        }
    }

    // Чтение файла в байтовый массив
    public byte[] readByteArray(String filename) {
        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename));
        } catch (java.io.IOException e) {
            System.out.println("Error reading byte array: " + e.getMessage());
            return new byte[0];
        }
    }

    // Копирование бинарного файла с буферизацией
    public void copyBinaryFile(String source, String destination) {
        try (java.io.BufferedInputStream bis = new java.io.BufferedInputStream(
                new java.io.FileInputStream(source));
             java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(
                     new java.io.FileOutputStream(destination))) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            System.out.println("Binary file copied successfully");

        } catch (java.io.IOException e) {
            System.out.println("Error copying binary file: " + e.getMessage());
        }
    }

    // Проверка заголовка файла (магические числа)
    public boolean isPNGFile(String filename) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(filename)) {
            byte[] header = new byte[8];
            if (fis.read(header) != 8) {
                return false;
            }

            // PNG заголовок: 89 50 4E 47 0D 0A 1A 0A
            return header[0] == (byte)0x89 &&
                    header[1] == 0x50 &&
                    header[2] == 0x4E &&
                    header[3] == 0x47 &&
                    header[4] == 0x0D &&
                    header[5] == 0x0A &&
                    header[6] == 0x1A &&
                    header[7] == 0x0A;

        } catch (java.io.IOException e) {
            System.out.println("Error checking file header: " + e.getMessage());
            return false;
        }
    }
}