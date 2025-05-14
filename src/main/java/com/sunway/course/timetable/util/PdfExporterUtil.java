package com.sunway.course.timetable.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PdfExporterUtil {

    // Recursively extract text from the node and its children
    public static String extractTextFromNode(Node node) {
        if (node instanceof Label) {
            return ((Label) node).getText();
        } else if (node instanceof Button) {
            return ((Button) node).getText();
        } else if (node instanceof Parent){
            for(Node child : ((Parent) node).getChildrenUnmodifiable()) {
                String result = extractTextFromNode(child);
                if (!result.isEmpty()) {
                    return result; // Return the first non-empty text found
                }
            }
        }
        return ""; // Fallback to default toString() method
    }

}
