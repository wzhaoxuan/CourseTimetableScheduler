package com.sunway.course.timetable.controller.app;
import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.ContentController;
import com.sunway.course.timetable.interfaces.PdfExportService;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.service.PdfExporterService;
import com.sunway.course.timetable.util.GridManagerUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

@Component
public class TimetableController extends ContentController {

    @FXML Button downloadTimetable;
    @FXML Label satisfaction, score, monday, tuesday, wednesday, thursday, friday;
    @FXML GridPane timetableGrid;

    private GridManagerUtil timetableGridManager;
    private FileChooser fileChooser = new FileChooser(); 
    private PdfExportService pdfExportService;

    public TimetableController(NavigationService navService, LoginSceneController loginController, 
                                PdfExportService pdfExportService) {
        super(navService, loginController);
        this.pdfExportService = pdfExportService;
    }

    @Override
    protected void initialize() {
        super.initialize(); 
        setupLabelsText();

        timetableGridManager = createTimetableGridManager();
        timetableGridManager.setupGridBorders();

    }

    private void setupLabelsText() {
        subheading.setText("Timetable");
        downloadTimetable.setText("Download");
        satisfaction.setText("Satisfaction");
        score.setText("99%");
        monday.setText("Monday");
        tuesday.setText("Tuesday");
        wednesday.setText("Wednesday");
        thursday.setText("Thursday");
        friday.setText("Friday");
    }

    @FXML
    public void downloadTimetable() {
        fileChooser.setTitle("Save Timetable");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDf files", "*.pdf"));
        fileChooser.setInitialFileName("timetable.pdf");

        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try {
                System.out.println("Exporting timetable to: " + selectedFile.getAbsolutePath());
                pdfExportService.export(timetableGrid, selectedFile);
                System.out.println("PDF saved successfully to: " + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected GridManagerUtil createTimetableGridManager() {
        return new GridManagerUtil(timetableGrid); 
    }

    public void setFileChooser(FileChooser chooser) {
        this.fileChooser = chooser;
    }
}
