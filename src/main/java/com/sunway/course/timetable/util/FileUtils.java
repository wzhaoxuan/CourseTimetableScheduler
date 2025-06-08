package com.sunway.course.timetable.util;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.*;

public class FileUtils {
    public static void zipFiles(List<File> files, File outputZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {
            for (File file : files) {
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }
}

