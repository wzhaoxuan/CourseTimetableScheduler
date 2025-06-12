package com.sunway.course.timetable.result;
import org.springframework.stereotype.Component;

@Component
public class SelectionStateHolder {
    private String selectedType;

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }
}
