package com.sunway.course.timetable.model;

public class SubjectPlanInfo {
    private String subjectCode;
    private String subjectName;
    private boolean lecture;
    private boolean practical;
    private boolean tutorial;
    private boolean workshop;
    private int totalStudents;
    private String mainLecturer;
    private String practicalTutor;
    private String tutorialTutor;
    private String workshopTutor;

    public SubjectPlanInfo(){}

    public SubjectPlanInfo(String subjectCode, String subjectName, boolean lecture, boolean practical, boolean tutorial, boolean workshop,
                            int totalStudents, String mainLecturer, String practicalTutor, String tutorialTutor, 
                            String workshopTutor) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.lecture = lecture;
        this.practical = practical;
        this.tutorial = tutorial;
        this.workshop = workshop;
        this.totalStudents = totalStudents;
        this.mainLecturer = mainLecturer;
        this.practicalTutor = practicalTutor;
        this.tutorialTutor = tutorialTutor;
        this.workshopTutor = workshopTutor;
    }


    public String getSubjectCode() {
        return subjectCode;
    }
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    public boolean hasLecture() {
        return lecture;
    }
    public void setLecture(boolean lecture) {
        this.lecture = lecture;
    }
    public boolean hasPractical() {
        return practical;
    }
    public void setPractical(boolean practical) {
        this.practical = practical;
    }
    public boolean hasTutorial() {
        return tutorial;
    }
    public void setTutorial(boolean tutorial) {
        this.tutorial = tutorial;
    }
    public boolean hasWorkshop() {
        return workshop;
    }
    public void setWorkshop(boolean workshop) {
        this.workshop = workshop;
    }
    public int getTotalStudents() {
        return totalStudents;
    }
    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
    public String getMainLecturer() {
        return mainLecturer;
    }
    public void setMainLecturer(String mainLecturer) {
        this.mainLecturer = mainLecturer;
    }
    public String getPracticalTutor() {
        return practicalTutor;
    }
    public void setPracticalTutor(String practicalTutor) {
        this.practicalTutor = practicalTutor;
    }
    public String getTutorialTutor() {
        return tutorialTutor;
    }
    public void setTutorialTutor(String tutorialTutor) {
        this.tutorialTutor = tutorialTutor;
    }
    public String getWorkshopTutor() {
        return workshopTutor;
    }
    public void setWorkshopTutor(String workshopTutor) {
        this.workshopTutor = workshopTutor;
    }

    @Override
    public String toString() {
        return "SubjectPlanInfo{" +
                "subjectCode='" + subjectCode + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", lecture=" + lecture +
                ", practical=" + practical +
                ", tutorial=" + tutorial +
                ", workshop=" + workshop +
                ", totalStudents=" + totalStudents +
                ", mainlecturer='" + mainLecturer + '\'' +
                ", practicalTutor='" + practicalTutor + '\'' +
                ", tutorialTutor='" + tutorialTutor + '\'' +
                ", workshopTutor='" + workshopTutor + '\'' +
                '}';
    }
}
