package com.sunway.course.timetable.edge.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.repository.LecturerRepository;
import com.sunway.course.timetable.service.LecturerServiceImpl;

@ExtendWith(MockitoExtension.class)
public class LecturerEdgeCaseTest {

    @Mock private LecturerRepository lecturerRepository;
    @InjectMocks private LecturerServiceImpl lecturerService;

    
}
