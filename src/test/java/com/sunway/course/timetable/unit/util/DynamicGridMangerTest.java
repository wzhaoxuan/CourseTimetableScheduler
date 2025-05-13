package com.sunway.course.timetable.unit.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.framework.junit5.ApplicationTest;
import org.junit.jupiter.api.Test;
import com.sunway.course.timetable.util.grid.DynamicGridManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import javafx.scene.Node;
import javafx.scene.control.Button;


public class DynamicGridMangerTest extends ApplicationTest {

    private DynamicGridManager dynamicGridManager;
    private GridPane gridPane;


    @Override
    public void start(Stage stage) {
        gridPane = new GridPane();
        dynamicGridManager = new DynamicGridManager(gridPane, 3, 3);
        Scene scene = new Scene(gridPane, 300, 300);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        Platform.runLater(() -> {
            gridPane.getChildren().clear();
            gridPane.getColumnConstraints().clear();
            gridPane.getRowConstraints().clear();
            dynamicGridManager = new DynamicGridManager(gridPane, 3, 3);
        });
    }

    @Test
    public void testgridSetUpAddsCorrectConstraints(){
        Platform.runLater(()-> {
            assertEquals(3, gridPane.getColumnConstraints().size());
            assertEquals(3, gridPane.getRowConstraints().size());
        });
    }

    @Test
    public void testAddButtonAddsButtonToGrid() {
        Platform.runLater(() -> {
            dynamicGridManager.addButton("Test Button", "test-style");

            assertEquals(1, gridPane.getChildren().size());
            Node node = gridPane.getChildren().get(0);
            assertTrue(node instanceof Button);
            Button button = (Button) node;
            assertEquals("Test Button", button.getText());
        });
    }

    @Test
    public void testAddButtonWithEmptyLabelDoesNothing(){
        Platform.runLater(()-> {
            dynamicGridManager.addButton("", "test-style");
            assertEquals(0, gridPane.getChildren().size());
        });
    }

    @Test
    public void testGridRearrangesCorrectly(){
        Platform.runLater(() -> {
            for(int i = 0; i < 5; i++){
                dynamicGridManager.addButton("Button " + i, "test-style");
            }

            assertEquals(5, gridPane.getChildren().size());

            for(Node node :gridPane.getChildren()){
                assertTrue(node instanceof Button);
                assertTrue(GridPane.getColumnIndex(node) != null);
                assertTrue(GridPane.getRowIndex(node) != null);
            }
        });
    }

    @Test
    public void testButtonRemovalUpdatesGrid() {
        Platform.runLater(() -> {
            dynamicGridManager.addButton("ToRemove", "test-style");
            Button btn = (Button) gridPane.getChildren().get(0);

            btn.fire(); // triggers removal
            assertEquals(0, gridPane.getChildren().size(), "Button should be removed after click");
        });
    }

     @Test
    public void testGetNodeByRowColumnIndexReturnsCorrectNode() {
        Platform.runLater(() -> {
            dynamicGridManager.addButton("Test", "btn");

            Node node = dynamicGridManager.getNodeByRowColumnIndex(0, 0, gridPane);
            assertNotNull(node, "Node at (0,0) should be found");
            assertTrue(node instanceof Button, "Node should be a Button");
        });
    }

    @Test
    public void testGetNodeByRowColumnIndexReturnsNullIfNotFound() {
        Platform.runLater(() -> {
            Node node = dynamicGridManager.getNodeByRowColumnIndex(0, 0, gridPane);
            assertNull(node, "Should return null when no node found");
        });
    }
}
