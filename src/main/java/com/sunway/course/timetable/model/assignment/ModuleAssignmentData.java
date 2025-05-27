package com.sunway.course.timetable.model.assignment;
import java.util.List;
import java.util.Set;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.programme.Programme;


public class ModuleAssignmentData {

    private SubjectPlanInfo subjectPlanInfo;
    private Module module;
    private List<Programme> programmeOfferingModules;
    private Set<Student> eligibleStudents;

    public ModuleAssignmentData(SubjectPlanInfo subjectPlanInfo, Module module, 
                                List<Programme> programmeOfferingModules, Set<Student> eligibleStudent){
        this.subjectPlanInfo = subjectPlanInfo;
        this.module = module;
        this.programmeOfferingModules = programmeOfferingModules;
        this.eligibleStudents = eligibleStudent;
    }

    public SubjectPlanInfo getSubjectPlanInfo() {
        return subjectPlanInfo;
    }

    public Module getModule() {
        return module;
    }

    public List<Programme> getProgrammeOfferingModules() {
        return programmeOfferingModules;
    }

    public Set<Student> getEligibleStudents() {
        return eligibleStudents;
    }

    public void setEligibleStudents(Set<Student> eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    @Override
    public String toString() {
        return "ModuleAssignmentData{" +
                "subjectPlanInfo=" + subjectPlanInfo +
                ", module=" + module +
                ", programmeOfferingModules=" + programmeOfferingModules +
                ", eligibleStudents=" + eligibleStudents +
                '}';
    }

}
