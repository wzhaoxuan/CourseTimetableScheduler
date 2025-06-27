package com.sunway.course.timetable.unit.controller;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.controller.base.AbstractTimetableViewController;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;
// import com.sunway.course.timetable.unit.controller.AbstractTimetableViewControllerTest.TestController;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Unit tests for AbstractTimetableViewController.
 */
// @ExtendWith(ApplicationExtension.class)
// public class AbstractTimetableViewControllerTest {

//     private TestController controller;
//     private VBox buttonBox;
//     private TextField searchField;
//     private ScrollPane scrollPane;
//     private static final NavigationService NAV = mock(NavigationService.class);
//     private static final LoginSceneController LOGIN = mock(LoginSceneController.class);
//     private static final SelectionStateHolder STATE = mock(SelectionStateHolder.class);

//     @Start
//     public void start(Stage stage) {
//         // Initialize UI controls on JavaFX thread
//         buttonBox = new VBox();
//         searchField = new TextField();
//         scrollPane = new ScrollPane(buttonBox);

//         HostServices hostServices = mock(HostServices.class);

//         // Concrete subclass with String items
//         controller = new TestController(
//             NAV,
//             LOGIN,
//             STATE,
//             hostServices,
//             Function.identity()
//         );
//         // Inject FXML fields
//         controller.buttonBox = buttonBox;
//         controller.searchField = searchField;
//         controller.scrollPane = scrollPane;
//         // Base initialization
//         controller.initializeBase();

//         // Show scene to initialize viewport bounds
//         stage.setScene(new Scene(scrollPane, 400, 300));
//         stage.show();
//     }

//     @Test
//     @DisplayName("displayButtons populates buttonBox with correct buttons and click fires handleButtonClick")
//     void testDisplayButtonsAndHandleClick() {
//         // Load items
//         Platform.runLater(() -> controller.loadItems(List.of("One", "Two", "Three")));
//         awaitFx();
//         // Verify three buttons
//         assertEquals(3, buttonBox.getChildren().size());
//         // Buttons text and click
//         for (int i = 0; i < 3; i++) {
//             Button btn = (Button) buttonBox.getChildren().get(i);
//             assertEquals(List.of("One","Two","Three").get(i), btn.getText());
//             // Fire click
//             Platform.runLater(btn::fire);
//         }
//         awaitFx();
//         // Ensure controller captured clicks
//         assertEquals(List.of("One","Two","Three"), controller.clickedItems);
//     }

//     @Test
//     @DisplayName("filterButtons filters displayed buttons based on search field text")
//     void testFilterButtons() {
//         // Load items
//         Platform.runLater(() -> controller.loadItems(List.of("Apple", "Banana", "Apricot")));
//         awaitFx();
//         // Initially three
//         assertEquals(3, buttonBox.getChildren().size());
//         // Apply filter 'ap'
//         Platform.runLater(() -> searchField.setText("ap"));
//         awaitFx();
//         // Should show "Apple" and "Apricot"
//         assertEquals(2, buttonBox.getChildren().size());
//         assertEquals("Apple", ((Button) buttonBox.getChildren().get(0)).getText());
//         assertEquals("Apricot", ((Button) buttonBox.getChildren().get(1)).getText());
//         // Clearing filter restores all
//         Platform.runLater(() -> searchField.setText(""));
//         awaitFx();
//         assertEquals(3, buttonBox.getChildren().size());
//     }

//     // Utility to wait for JavaFX tasks
//     private void awaitFx() {
//         try {
//             CountDownLatch latch = new CountDownLatch(1);
//             Platform.runLater(latch::countDown);
//             latch.await(1000, TimeUnit.MILLISECONDS);
//         } catch (InterruptedException ignored) {}
//     }

//     /**
//      * Concrete subclass of the abstract controller for testing.
//      */
//     public static class TestController extends AbstractTimetableViewController<String> {
//         public final List<String> clickedItems = new java.util.ArrayList<>();
//         public TestController(NavigationService navService,
//                               LoginSceneController loginController,
//                               SelectionStateHolder stateHolder,
//                               javafx.application.HostServices hostServices,
//                               Function<String,String> nameExtractor) {
//             super(navService, loginController, stateHolder, hostServices, nameExtractor);
//         }
//         @Override
//         protected void handleButtonClick(String item) {
//             clickedItems.add(item);
//         }
//     }
// }

