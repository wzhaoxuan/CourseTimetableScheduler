package com.sunway.course.timetable.interfaces;

import java.io.File;
import java.io.IOException;
import javafx.scene.layout.GridPane;

public interface PdfExportService {
    void export(GridPane gridPane, File file) throws IOException;
}
