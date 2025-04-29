package com.sunway.course.timetable.util;
import javafx.scene.layout.Priority;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class DynamicGridManager {
    private final GridPane gridPane;
    private final int maxColumns;
    private final int maxRows;
    private int currentRow = 0;
    private int currentCol = 0;


    public DynamicGridManager(GridPane gridPane, int maxColumns, int maxRows) {
        this.gridPane = gridPane;
        this.maxColumns = maxColumns;
        this.maxRows = maxRows;
        setupGrid();
    }

    private void setupGrid(){
        for (int i = 0; i < maxColumns; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < maxRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rowConstraints);
        }
    }

    public void addButton(String label, String styleClass) {
        if (label == null || label.isEmpty()) return;

        Button button = new Button(label);
        // button.setMaxWidth(Region.USE_COMPUTED_SIZE);
        // button.setMaxHeight(Region.USE_COMPUTED_SIZE);
        button.getStyleClass().add(styleClass);
        gridPane.add(button, currentCol, currentRow);
        rearrangeGrid();

        button.setOnAction(e -> {
            gridPane.getChildren().remove(button);
            rearrangeGrid();
        });
    }

    private void rearrangeGrid() {
        int col = 0;
        int row = 0;

        for(int i = 0; i < gridPane.getChildren().size(); i++){
            Node node = gridPane.getChildren().get(i);
            GridPane.setColumnIndex(node, col);
            GridPane.setRowIndex(node, row);
            col++;
            if (col >= maxColumns) {
                col = 0;
                row++;
            }
        }

        currentRow = row;
        currentCol = col;
    }
}
