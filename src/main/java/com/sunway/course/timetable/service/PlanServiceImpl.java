package com.sunway.course.timetable.service;
import com.sunway.course.timetable.interfaces.services.PlanService;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.repository.PlanRepostiory;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepostiory planRepostiory;

    public PlanServiceImpl(PlanRepostiory planRepostiory) {
        this.planRepostiory = planRepostiory;
    }
}
