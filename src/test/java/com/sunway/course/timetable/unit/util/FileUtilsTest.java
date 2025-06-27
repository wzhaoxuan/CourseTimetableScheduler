package com.sunway.course.timetable.unit.util;
import com.sunway.course.timetable.util.FileUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("zipFilesWithStructure creates ZIP with correct directory structure and file contents")
    void testZipFilesWithStructure(@TempDir Path tempDir) throws IOException {
        // Create sample files
        Path semesterFile = tempDir.resolve("semester1.xlsx");
        Path lecturerFile = tempDir.resolve("lecturerA.xlsx");

        Files.writeString(semesterFile, "semester-data");
        Files.writeString(lecturerFile, "lecturer-data");

        // Map categories to files
        Map<String, List<File>> input = Map.of(
            "semester", List.of(semesterFile.toFile()),
            "lecturer", List.of(lecturerFile.toFile())
        );

        // Destination ZIP
        File zipFile = tempDir.resolve("output.zip").toFile();

        // Execute
        FileUtils.zipFilesWithStructure(input, zipFile);

        // Assertions on ZIP
        try (ZipFile zf = new ZipFile(zipFile)) {
            // Expect exactly two entries
            assertEquals(2, zf.size(), "ZIP should contain two entries");

            // Check semester entry
            ZipEntry semEntry = zf.getEntry("semester/" + semesterFile.getFileName());
            assertNotNull(semEntry, "Semester entry must exist in ZIP");
            String semContent = new String(zf.getInputStream(semEntry).readAllBytes());
            assertEquals("semester-data", semContent);

            // Check lecturer entry
            ZipEntry lecEntry = zf.getEntry("lecturer/" + lecturerFile.getFileName());
            assertNotNull(lecEntry, "Lecturer entry must exist in ZIP");
            String lecContent = new String(zf.getInputStream(lecEntry).readAllBytes());
            assertEquals("lecturer-data", lecContent);
        }
    }
}
