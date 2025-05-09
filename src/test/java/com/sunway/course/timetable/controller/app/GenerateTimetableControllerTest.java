package com.sunway.course.timetable.controller.app;
// package com.sunway.course.timetable.controller;

// import java.util.concurrent.CountDownLatch;

// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.MockitoAnnotations;

// import com.sunway.course.timetable.controller.app.GenerateTimetableController;
// import com.sunway.course.timetable.controller.authentication.LoginSceneController;
// import com.sunway.course.timetable.event.LecturerConstraintConfirmedEvent;
// import com.sunway.course.timetable.event.VenueAddedEvent;
// import com.sunway.course.timetable.model.Lecturer;
// import com.sunway.course.timetable.model.Venue;
// import com.sunway.course.timetable.service.NavigationService;
// import com.sunway.course.timetable.service.VenueService;
// import com.sunway.course.timetable.store.VenueSessionStore;
// import com.sunway.course.timetable.store.WeekdaySessionStore;
// import com.sunway.course.timetable.util.grid.DynamicGridManager;

// import javafx.application.Platform;
// import javafx.scene.control.Button;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Label;
// import javafx.scene.control.ScrollPane;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.GridPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.Priority;
// import javafx.scene.layout.Region;

// class GenerateTimetableControllerTest {

//     @Mock private NavigationService navigationService;
//     @Mock private LoginSceneController loginController;
//     @Mock private VenueService venueService;
//     @Mock private VenueSessionStore venueStore;
//     @Mock private WeekdaySessionStore weekdayStore;
//     @Mock private DynamicGridManager venueGridManager;
//     @Mock private DynamicGridManager weekdayGridManager;

//     @InjectMocks
//     private GenerateTimetableController controller;

//     @BeforeAll
//     static void initJFX() throws Exception {
//         CountDownLatch latch = new CountDownLatch(1);
//         Platform.startup(latch::countDown);
//         latch.await();
//     }

//     @BeforeEach
//     void setUp() throws Exception {
//         MockitoAnnotations.openMocks(this);

//         // Inject fake grid managers (or refactor to allow this)
//         controller = new GenerateTimetableController(
//             navigationService, 
//             loginController, 
//             venueService, 
//             null, 
//             venueStore, 
//             weekdayStore
//         ){
//             @Override
//             protected DynamicGridManager createVenueGridManager() {
//                 return venueGridManager;
//             }

//             @Override
//             protected DynamicGridManager createWeekdayGridManager() {
//                 return weekdayGridManager;
//             }
//         }; // Call initialize to set up the controller

//         // Inject mock or fake Label into the superclass's private field
//         Label mockLabel = mock(Label.class);
//         Button mockButton = mock(Button.class);
//         TextField mockTextField = mock(TextField.class);
//         GridPane mockGridPane = mock(GridPane.class);
//         ScrollPane mockScrollPane = mock(ScrollPane.class);
//         Region mockSpacer1 = mock(Region.class);
//         Region mockSpacer2 = mock(Region.class);
//         Region mockSpacer3 = mock(Region.class);
//         Region mockSpacer4 = mock(Region.class);

//         try (MockedStatic<HBox> hboxMockedStatic = mockStatic(HBox.class)) {
//         // Arrange: Call the setup for spacers
//             HBox.setHgrow(mockSpacer1, Priority.ALWAYS);
//             HBox.setHgrow(mockSpacer2, Priority.ALWAYS);
//             HBox.setHgrow(mockSpacer3, Priority.ALWAYS);
//             HBox.setHgrow(mockSpacer4, Priority.ALWAYS);

//             // Act: Verify that the static method was called with the correct arguments
//             hboxMockedStatic.verify(() -> HBox.setHgrow(mockSpacer1, Priority.ALWAYS));
//             hboxMockedStatic.verify(() -> HBox.setHgrow(mockSpacer2, Priority.ALWAYS));
//             hboxMockedStatic.verify(() -> HBox.setHgrow(mockSpacer3, Priority.ALWAYS));
//             hboxMockedStatic.verify(() -> HBox.setHgrow(mockSpacer4, Priority.ALWAYS));
//         }

//         ComboBox<String> programmeChoice = new ComboBox<>();
//         programmeChoice.getItems().addAll("Diploma in IT", "Diploma in Business", "Diploma in Communication");

//         setPrivateField(controller, "title", mockLabel);
//         setPrivateField(controller, "username", mockLabel);
//         setPrivateField(controller, "subheading", mockLabel);
//         setPrivateField(controller, "programme", mockLabel);
//         setPrivateField(controller, "year", mockLabel);
//         setPrivateField(controller, "intake", mockLabel);
//         setPrivateField(controller, "semester", mockLabel);
//         setPrivateField(controller, "venue", mockLabel);
//         setPrivateField(controller, "lecturerAvailable", mockLabel);
//         setPrivateField(controller, "venueField", mockTextField);
//         setPrivateField(controller, "generateButton", mockButton);
//         setPrivateField(controller, "sectionButton", mockButton);
//         setPrivateField(controller, "homeButton", mockButton);
//         setPrivateField(controller, "generateTimetable", mockButton);
//         setPrivateField(controller, "viewTimetable", mockButton);
//         setPrivateField(controller, "logOutButton", mockButton);
//         setPrivateField(controller, "programmeChoice", programmeChoice);
//         setPrivateField(controller, "yearChoice", programmeChoice);
//         setPrivateField(controller, "intakeChoice", programmeChoice);
//         setPrivateField(controller, "semesterChoice", programmeChoice);
//         setPrivateField(controller, "venueGrid", mockGridPane);
//         setPrivateField(controller, "weekdayGrid", mockGridPane);
//         setPrivateField(controller, "venueScroll", mockScrollPane);
//         setPrivateField(controller, "weekdayScroll", mockScrollPane);
//         setPrivateField(controller, "spacer1", mockSpacer1);
//         setPrivateField(controller, "spacer2", mockSpacer2);
//         setPrivateField(controller, "spacer3", mockSpacer3);
//         setPrivateField(controller, "spacer4", mockSpacer4);


//         controller.initialize();
//     }

//     @Test
//     void shouldAddVenueWhenVenueAddedEventHandled() {
//         Venue venue = new Venue("Room", "UW2-1", 35, "Uni West", "Level 2");
//         VenueAddedEvent event = new VenueAddedEvent(venue);

//         when(venueStore.add("UW2-1")).thenReturn(true);

//         controller.handleVenueAdded(event);

//         verify(venueStore).add("UW2-1");
//         verify(venueGridManager).addButton("UW2-1", "venue-button");
//     }

//     @Test
//     void shouldAddLecturerWhenLecturerConstraintEventHandled() {
//         Lecturer lecturer = new Lecturer();
//         lecturer.setId(42L);
//         lecturer.setName("Dr. Smith");

//         LecturerConstraintConfirmedEvent event = new LecturerConstraintConfirmedEvent(lecturer);

//         when(weekdayStore.add("Dr. Smith")).thenReturn(true);

//         controller.handleLecturerConstraintConfirmed(event);

//         verify(weekdayStore).add("Dr. Smith");
//         verify(weekdayGridManager).addButton("Dr. Smith", "lecturer-button");
//     }

//     private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
//         Class<?> clazz = target.getClass();

//         while(clazz != null) {
//             try {
//                 var field = clazz.getDeclaredField(fieldName);
//                 field.setAccessible(true);
//                 field.set(target, value);
//                 return;
//             } catch (NoSuchFieldException e) {
//                 clazz = clazz.getSuperclass();
//             }
//         }
//         throw new NoSuchFieldException("Field " + fieldName + " not found in class hierarchy of " + target.getClass().getName());
//     }
// }
