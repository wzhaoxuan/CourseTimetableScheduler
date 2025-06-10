package com.sunway.course.timetable.util;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.*;
import java.util.Map;

public class FileUtils {
    public static void zipFilesWithStructure(Map<String, List<File>> categoryToFiles, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (Map.Entry<String, List<File>> entry : categoryToFiles.entrySet()) {
                String folder = entry.getKey(); // "semester", "lecturer", etc.
                for (File file : entry.getValue()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        String entryName = folder + "/" + file.getName(); // e.g., "semester/BCS-S1.xlsx"
                        zos.putNextEntry(new ZipEntry(entryName));
                        fis.transferTo(zos);
                        zos.closeEntry();
                    }
                }
            }
        }
    }
}

