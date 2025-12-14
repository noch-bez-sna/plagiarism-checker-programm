package com.plagiarism.checker;

import com.plagiarism.checker.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testFindJavaFiles_EmptyDirectory() throws IOException {
        // Пустая директория
        List<Path> files = FileUtils.findJavaFiles(tempDir);
        assertTrue(files.isEmpty());
    }

    @Test
    void testFindJavaFiles_WithJavaFiles() throws IOException {
        // Создаем Java файлы
        Path javaFile1 = tempDir.resolve("Test1.java");
        Path javaFile2 = tempDir.resolve("Test2.java");
        Path txtFile = tempDir.resolve("test.txt");

        Files.writeString(javaFile1, "public class Test1 {}");
        Files.writeString(javaFile2, "public class Test2 {}");
        Files.writeString(txtFile, "not a java file");

        List<Path> files = FileUtils.findJavaFiles(tempDir);
        assertEquals(2, files.size());
        assertTrue(files.contains(javaFile1));
        assertTrue(files.contains(javaFile2));
    }

    @Test
    void testReadFile_ValidFile() throws IOException {
        // Создаем тестовый файл
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, World!";
        Files.writeString(testFile, content);

        String readContent = FileUtils.readFile(testFile);
        assertEquals(content, readContent);
    }

    @Test
    void testIsValidJavaFile_ValidFile() throws IOException {
        Path javaFile = tempDir.resolve("Valid.java");
        Files.writeString(javaFile, "public class Valid {}");

        // Используем рефлексию для доступа к приватному методу
        // или просто проверяем через findJavaFiles
        List<Path> files = FileUtils.findJavaFiles(tempDir);
        assertEquals(1, files.size());
    }
}