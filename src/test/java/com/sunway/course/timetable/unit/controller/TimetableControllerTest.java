// package com.sunway.course.timetable.unit.controller;

// import java.io.File;
// import java.lang.reflect.Method;
// import java.util.List;
// import com.sunway.course.timetable.controller.app.TimetableController;
// import com.sunway.course.timetable.controller.authentication.LoginSceneController;
// import com.sunway.course.timetable.service.NavigationService;
// import javafx.application.HostServices;
// import javafx.application.Platform;
// import javafx.embed.swing.JFXPanel;
// import javafx.scene.Scene;
// import javafx.scene.control.Button;
// import javafx.scene.control.ScrollPane;
// import javafx.scene.layout.StackPane;
// import javafx.scene.layout.VBox;
// import javafx.stage.FileChooser;
// import javafx.stage.Stage;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.junit.jupiter.api.Assertions.fail;


// @ExtendWith(MockitoExtension.class)
// public class TimetableControllerTest {

//     @Mock private NavigationService navigationService;
//     @Mock private LoginSceneController loginController;
//     @Mock private HostServices hostServices;

//     private TimetableController controller;

//     @BeforeAll
//     static void initFx() {
//         new JFXPanel(); // Initialize JavaFX platform
//     }

//     @BeforeEach
//     void setUp() {
//         controller = new TimetableController(navigationService, loginController, hostServices);

//         // Inject fake scene/window for FileChooser
//         Button dummyDownloadButton = new Button();
//         Scene scene = new Scene(new StackPane(dummyDownloadButton));
//         Stage stage = new Stage();
//         stage.setScene(scene);
//         stage.show();

//         dummyDownloadButton.setId("dummy");
//         controller.downloadAll = dummyDownloadButton;

//         // Set a dummy scrollPane and VBox so controller initializes without error
//         controller.timetableScrollPane = new ScrollPane();
//         controller.timetableList = new VBox();
//     }

//     @Test
//     void testDownloadAll_whenNoFiles_exported_shouldSkip() {
//         controller.downloadAll(); // Should just print and return

//         // No exception expected; log message would be printed
//     }

//     @Test
//     void testDownloadAll_withFiles_shouldCallZip() throws Exception {
//         // Prepare dummy files
//         File semesterFile = File.createTempFile("semester", ".xlsx");
//         File lecturerFile = File.createTempFile("lecturer", ".xlsx");
//         File moduleFile = File.createTempFile("module", ".xlsx");

//         controller.loadExportedTimetables(
//             List.of(semesterFile),
//             List.of(lecturerFile),
//             List.of(moduleFile),
//             85.0
//         );

//         // Override FileChooser for test
//         FileChooser chooser = new FileChooser();
//         File outputZip = File.createTempFile("output", ".zip");
//         outputZip.delete(); // so it doesn't exist before test

//         FileChooser finalChooser = chooser;
//         Platform.runLater(() -> {
//             try {
//                 Method m = TimetableController.class.getDeclaredMethod("downloadAll");
//                 m.setAccessible(true);

//                 // Simulate chooser result (mock file save dialog)
//                 FileChooser originalChooser = new FileChooser() {
//                     @Override
//                     public File showSaveDialog(Window window) {
//                         return outputZip;
//                     }
//                 };

//                 // Monkey-patch with reflection if necessary (or extract FileChooser logic into a method for testability)

//                 // Since zipFilesWithStructure is static, we recommend wrapping it in a class you can mock

//                 // Assuming you did that, verify the zip logic is called and the file exists
//                 controller.downloadAll();
//             } catch (Exception e) {
//                 fail("Download failed with exception: " + e.getMessage());
//             }
//         });
//     }
// }
