// package com.sunway.course.timetable.controller.app;

// import javafx.scene.Node;
// import java.io.File;
// import java.io.IOException;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.TimeUnit;

// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;

// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.when;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.sunway.course.timetable.controller.authentication.LoginSceneController;
// import com.sunway.course.timetable.interfaces.PdfExportService;
// import com.sunway.course.timetable.service.NavigationService;
// import com.sunway.course.timetable.util.grid.DynamicGridManager;
// import com.sunway.course.timetable.util.pdf.PdfExporter;

// import javafx.application.Platform;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.scene.layout.GridPane;
// import javafx.stage.FileChooser;

// @ExtendWith(MockitoExtension.class)
// public class TimetableControllerTest {

//     @Mock private NavigationService navigationService;
//     @Mock private LoginSceneController loginController;
//     @Mock private PdfExportService pdfExportService;
//     @Mock private DynamicGridManager gridManager; 
//     @Mock private FileChooser fileChooser;
//     @Mock private File file;
//     @Mock private GridPane timetableGrid;

//     private TimetableController timetableController;

//     @BeforeAll
//     static void initJFX() throws Exception {
//         Platform.startup(() -> {}); // Initialize the JavaFX toolkit
//     }

//     @BeforeEach
//     void setUp() {
//         // Initialize mocks and other setup code here
//         timetableController = new TimetableController(navigationService, loginController, pdfExportService) {
//             @Override
//             protected DynamicGridManager createTimetableGridManager() {
//                 return gridManager;
//             }
//         };

//         timetableController.timetableGrid = timetableGrid;
//         timetableController.setFileChooser(fileChooser);

//         // Mock the column and row constraints to avoid the NullPointerException
//         ObservableList<javafx.scene.layout.ColumnConstraints> columnConstraints = FXCollections.observableArrayList();
//         ObservableList<javafx.scene.layout.RowConstraints> rowConstraints = FXCollections.observableArrayList();

//         // Add dummy constraints to avoid NPE
//         columnConstraints.add(new javafx.scene.layout.ColumnConstraints());
//         rowConstraints.add(new javafx.scene.layout.RowConstraints());

//         when(timetableGrid.getColumnConstraints()).thenReturn(columnConstraints);
//         when(timetableGrid.getRowConstraints()).thenReturn(rowConstraints);

//         // Mock the getChildren() method to return a non-null list
//         ObservableList<Node> children = FXCollections.observableArrayList();
//         Node mockNode = mock(Node.class); // Mock a simple node
//         children.add(mockNode);

//         when(timetableGrid.getChildren()).thenReturn(children);

//     }


//     @Test
//     @DisplayName("Test downloadTimetable - successful export")
//     void testDownloadTimetable() throws IOException, InterruptedException {
//         File mockFile = mock(File.class);
//         when(mockFile.getAbsolutePath()).thenReturn("dummy.pdf");
//         when(fileChooser.showSaveDialog(null)).thenReturn(mockFile);

//         CountDownLatch latch = new CountDownLatch(1);

//         Platform.runLater(() -> {
//             timetableController.downloadTimetable();
//             latch.countDown();
//         });

//         latch.await(3, TimeUnit.SECONDS);

//         verify(pdfExportService, times(1)).export(timetableGrid, mockFile);
//     }

//     // @Test
//     // @DisplayName("Test downloadTimetable - no file selected")
//     // void testDownloadTimetableNoFileSelected() throws InterruptedException {
//     //     when(fileChooser.showSaveDialog(null)).thenReturn(null);

//     //     CountDownLatch latch = new CountDownLatch(1);

//     //     Platform.runLater(() -> {
//     //         timetableController.downloadTimetable();
//     //         latch.countDown();
//     //     });

//     //     latch.await(3, TimeUnit.SECONDS);

//     //     verify(pdfExportService, never()).export(any(), any());
//     // }

//     // @Test
//     // @DisplayName("Test downloadTimetable - IOException handling")
//     // void testDownloadTimetableIOException() throws InterruptedException, IOException {
//     //     when(fileChooser.showSaveDialog(null)).thenReturn(file);
//     //     when(file.getAbsolutePath()).thenReturn("timetable.pdf");

//     //     doThrow(new IOException("Simulated IO error")).when(pdfExportService).export(timetableGrid, file);

//     //     CountDownLatch latch = new CountDownLatch(1);

//     //     Platform.runLater(() -> {
//     //         timetableController.downloadTimetable();
//     //         latch.countDown();
//     //     });

//     //     latch.await(3, TimeUnit.SECONDS);

//     //     verify(pdfExportService, times(1)).export(timetableGrid, file);
//     // }
// }
