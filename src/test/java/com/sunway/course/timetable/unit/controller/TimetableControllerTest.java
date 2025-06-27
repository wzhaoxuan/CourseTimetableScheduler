package com.sunway.course.timetable.unit.controller;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sunway.course.timetable.controller.app.TimetableController;
import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.service.NavigationService;
import com.sunway.course.timetable.view.MainApp;

import javafx.application.HostServices;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

// public class TimetableControllerTest {

//     private TimetableController controller;
//     private VBox timetableList;
//     private ScrollPane timetableScrollPane;
//     private HostServices hostServices;

//     @BeforeEach
//     void setUp() throws Exception {
//         // Mock dependencies
//         hostServices = mock(HostServices.class);

//         // Override the static used by the controller
//         MainApp.hostServices = hostServices;
        
//         // Instantiate controller
//         controller = new TimetableController(
//             mock(NavigationService.class),
//             mock(LoginSceneController.class),
//             hostServices
//         );
//         // Using reflection to inject private FXML fields
//         timetableList = new VBox();
//         timetableScrollPane = new ScrollPane();
//         setField(controller, "timetableList", timetableList);
//         setField(controller, "timetableScrollPane", timetableScrollPane);
//     }

//     @Test
//     @DisplayName("loadExportedTimetables populates sections and buttons correctly")
//     void testLoadExportedTimetables() throws Exception {
//         // Prepare three lists
//         File semFile = new File("semester.xlsx");
//         File lecFile = new File("lecturer.xlsx");
//         File modFile = new File("module.xlsx");
//         List<File> sems = List.of(semFile);
//         List<File> lecs = List.of(lecFile);
//         List<File> mods = List.of(modFile);

//         // Call method
//         controller.loadExportedTimetables(sems, lecs, mods, 90.0);

//         // Expect labels + one button per list
//         // 3 sections: Semester, Lecturer, Module
//         assertEquals(6, timetableList.getChildren().size()); // 3 labels + 3 buttons
//         // Check label texts
//         assertEquals("Semester Timetables", ((Label) timetableList.getChildren().get(0)).getText());
//         assertEquals("Lecturer Timetables", ((Label) timetableList.getChildren().get(2)).getText());
//         assertEquals("Module Timetables", ((Label) timetableList.getChildren().get(4)).getText());
//         // Check button labels (without .xlsx)
//         assertEquals("semester", ((Button) timetableList.getChildren().get(1)).getText());
//         assertEquals("lecturer", ((Button) timetableList.getChildren().get(3)).getText());
//         assertEquals("module", ((Button) timetableList.getChildren().get(5)).getText());
//     }

//     @Test
//     @DisplayName("addDownloadButton fires HostServices.showDocument correctly")
//     void testAddDownloadButtonCallsHostServices() throws Exception {
//         // Inject list
//         timetableList.getChildren().clear();

//         File file = new File("C:/tmp/test.xlsx");
//         controller.addDownloadButton(file);
//         // Last child is the button
//         Node last = timetableList.getChildren().get(timetableList.getChildren().size() - 1);
//         assertTrue(last instanceof Button);
//         Button btn = (Button) last;
//         // Simulate click
//         btn.getOnAction().handle(null);
//         // Verify hostServices called with file URI
//         String expectedUri = file.toURI().toString();
//         verify(hostServices).showDocument(expectedUri);
//     }

//     // Helper to set private fields
//     private static void setField(Object target, String name, Object value) throws Exception {
//         Field field = TimetableController.class.getDeclaredField(name);
//         field.setAccessible(true);
//         field.set(target, value);
//     }
// }
