package com.sunway.course.timetable.util.grid;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public class DynamicGridManager {
    private final GridPane gridPane;
    private int maxColumns;
    private int maxRows;
    private int currentRow = 0;
    private int currentCol = 0;


    public DynamicGridManager(GridPane gridPane) {
        this.gridPane = gridPane;
    }

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

    public void setupGridBorders() {
        int totalCol = gridPane.getColumnConstraints().size();
        int totalRow = gridPane.getRowConstraints().size();

        for(int col = 0; col < totalCol; col++){
            for(int row = 0; row < totalRow; row++){
                //Get existing node
                Node content = getNodeByRowColumnIndex(col, row, gridPane);

                if(content != null && !(content instanceof StackPane)) {
                    gridPane.getChildren().remove(content);

                    StackPane cell = new StackPane(content);
                    cell.getStyleClass().add("grid-cell");
                    gridPane.add(cell, col, row);
                }
            }
        }
    } 

    public Node getNodeByRowColumnIndex(int column, int row, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            int col = (colIndex == null) ? 0 : colIndex;
            int rw = (rowIndex == null) ? 0 : rowIndex;

            if (col == column && rw == row) {
                return node;
            }
        }
        return null;
    }
}
