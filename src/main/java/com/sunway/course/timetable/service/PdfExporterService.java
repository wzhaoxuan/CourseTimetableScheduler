package com.sunway.course.timetable.service;
import com.sunway.course.timetable.interfaces.PdfExportService;
import com.sunway.course.timetable.util.GridManagerUtil;
import com.sunway.course.timetable.util.PdfExporterUtil;

import javafx.scene.Node;

import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;

import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import javafx.scene.Parent;

@Service
public class PdfExporterService implements PdfExportService {

    @Override
    public void export(GridPane gridPane, File outputFile) throws IOException {
        PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
        PdfDocument pdfDoc = new PdfDocument(writer);

        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdfDoc, pageSize);

        int cols = gridPane.getColumnConstraints().size();
        int rows = gridPane.getRowConstraints().size();

        float[] columnWidths = new float[cols];
        for (int i = 0; i < cols; i++) {
            // Set default width for each column
            columnWidths[i] = 100f;
        }

        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                GridManagerUtil gridManager = new GridManagerUtil(gridPane);
                Node cellNode = gridManager.getNodeByRowColumnIndex(col, row, gridPane);
                String text = PdfExporterUtil.extractTextFromNode(cellNode);
                Cell cell = new Cell().add(new Paragraph(text)).setPadding(30f);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
    }
    
}
