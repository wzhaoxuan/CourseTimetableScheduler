package com.sunway.course.timetable.controller.base;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.sunway.course.timetable.controller.authentication.LoginSceneController;
import com.sunway.course.timetable.result.SelectionStateHolder;
import com.sunway.course.timetable.service.NavigationService;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public abstract class AbstractTimetableViewController<T> extends SelectionController {

    @FXML protected VBox buttonBox;
    @FXML protected TextField searchField;
    @FXML protected ScrollPane scrollPane;

    protected final HostServices hostServices;

    protected List<T> allItems = new ArrayList<>();
    protected final Function<T, String> nameExtractor;

    public AbstractTimetableViewController(
        NavigationService navService,
        LoginSceneController loginController,
        SelectionStateHolder stateHolder,
        HostServices hostServices,
        Function<T, String> nameExtractor
    ) {
        super(navService, loginController, stateHolder);
        this.hostServices = hostServices;
        this.nameExtractor = nameExtractor;
    }

    protected void initializeBase() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            buttonBox.setPrefWidth(newBounds.getWidth());
        });

        searchField.textProperty().addListener((obs, oldText, newText) -> filterButtons(newText));
    }

    protected void loadItems(List<T> items) {
        this.allItems = items;
        displayButtons(allItems);
    }

    private void displayButtons(List<T> items) {
        buttonBox.getChildren().clear();
        for (T item : items) {
            String displayName = nameExtractor.apply(item);
            Button btn = new Button(displayName);
            btn.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(btn, new Insets(5));
            btn.getStyleClass().add("timetable-button");

            btn.setOnAction(e -> handleButtonClick(item));
            buttonBox.getChildren().add(btn);
        }
    }

    private void filterButtons(String filter) {
        if (filter == null || filter.isEmpty()) {
            displayButtons(allItems);
            return;
        }
        List<T> filtered = allItems.stream()
                .filter(item -> nameExtractor.apply(item).toLowerCase().contains(filter.toLowerCase()))
                .toList();
        displayButtons(filtered);
    }

    protected abstract void handleButtonClick(T item);
}
