package com.sunway.course.timetable.unit.controller;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sunway.course.timetable.controller.authentication.SignUpSceneController;
import com.sunway.course.timetable.interfaces.services.UserService;
import com.sunway.course.timetable.service.NavigationService;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

// public class SignUpSceneControllerTest {
//     private SignUpSceneController controller;
//     private UserService userService;
//     private PasswordField confirmPasswordField;
//     private Button toLoginButton;

//     @BeforeEach
//     void setUp() throws Exception {
//         // Spy the controller to stub internal navigation
//         NavigationService navService = mock(NavigationService.class);
//         controller = spy(new SignUpSceneController(navService));

//         // Inject SignUpSceneController's own FXML fields
//         confirmPasswordField = new PasswordField();
//         toLoginButton = new Button();
//         setFieldInHierarchy(controller, "confirmPasswordField", confirmPasswordField);
//         setFieldInHierarchy(controller, "toLoginButton", toLoginButton);

//         // Inject AuthBaseController FXML fields
//         TextField usernameField = new TextField();
//         PasswordField passwordField = new PasswordField();
//         Button signUpButtonAuth = new Button();
//         Label descriptionLabel = new Label();
//         ImageView logoImageView = mock(ImageView.class);
//         Label titleLabel = new Label();

//         setFieldInHierarchy(controller, "usernameField", usernameField);
//         setFieldInHierarchy(controller, "passwordField", passwordField);
//         setFieldInHierarchy(controller, "signUpButton", signUpButtonAuth);
//         setFieldInHierarchy(controller, "description", descriptionLabel);
//         setFieldInHierarchy(controller, "logo", logoImageView);
//         setFieldInHierarchy(controller, "title", titleLabel);

//         // Inject UserService
//         userService = mock(UserService.class);
//         setFieldInHierarchy(controller, "userService", userService);

//     }

//     @Test
//     @DisplayName("initialize sets button text and prompt text correctly")
//     void testInitialize() {
//         controller.initialize();
//         assertEquals("Back to Login", toLoginButton.getText());
//         assertEquals("Confirm Password", confirmPasswordField.getPromptText());
//     }

//     @Test
//     @DisplayName("signUp calls addUser and navigates on successful validation")
//     void testSignUpSuccess() throws Exception {
//         // Stub trimUsername/Password
//         doReturn("user").when(controller).trimUsername();
//         doReturn("pass").when(controller).trimPassword();
//         confirmPasswordField.setText(" pass ");

//         // Stub validation passes
//         when(userService.validateSignUpField("user", "pass", "pass")).thenReturn(true);
//         doNothing().when(controller).navigateToPage("LoginPage");

//         controller.signUp();

//         verify(userService).addUser("user", "pass");
//         verify(controller).navigateToPage("LoginPage");
//     }

//     @Test
//     @DisplayName("signUp does not addUser or navigate on validation failure")
//     void testSignUpFailure() throws Exception {
//         doReturn("user").when(controller).trimUsername();
//         doReturn("pass").when(controller).trimPassword();
//         confirmPasswordField.setText("pass");

//         when(userService.validateSignUpField(any(), any(), any())).thenReturn(false);
//         doNothing().when(controller).navigateToPage(anyString());

//         controller.signUp();

//         verify(userService, never()).addUser(anyString(), anyString());
//         verify(controller, never()).navigateToPage(anyString());
//     }

//     @Test
//     @DisplayName("navigateToLogin invokes navigateToPage with LoginPage")
//     void testNavigateToLogin() {
//         doNothing().when(controller).navigateToPage("LoginPage");
//         controller.navigateToLogin();
//         verify(controller).navigateToPage("LoginPage");
//     }

//     /**
//      * Recursively searches class hierarchy for the field and sets it.
//      */
//     private static void setFieldInHierarchy(Object target, String fieldName, Object value) throws Exception {
//         Class<?> clazz = target.getClass();
//         while (clazz != null) {
//             try {
//                 Field field = clazz.getDeclaredField(fieldName);
//                 field.setAccessible(true);
//                 field.set(target, value);
//                 return;
//             } catch (NoSuchFieldException e) {
//                 clazz = clazz.getSuperclass();
//             }
//         }
//         throw new NoSuchFieldException("Field '" + fieldName + "' not found on " + target.getClass());
//     }
// }

