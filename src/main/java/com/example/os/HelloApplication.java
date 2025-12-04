package com.example.os;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    
    @Override
    public void start(Stage stage) {
        try {
            // Load FXML with proper error handling
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("scheduler-view.fxml"));
            
            if (fxmlLoader.getLocation() == null) {
                showErrorAndExit("FXML file not found", 
                    "The scheduler-view.fxml file could not be located.\n" +
                    "Please ensure the resource file exists in the resources directory.");
                return;
            }
            
            Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Apply modern styling if available (optional)
            try {
                String stylesheet = Objects.requireNonNull(
                    HelloApplication.class.getResource("/com/example/os/styles.css")).toExternalForm();
                scene.getStylesheets().add(stylesheet);
            } catch (Exception e) {
                // Stylesheet is optional, continue without it
                System.out.println("Custom stylesheet not found, using default styling.");
            }
            
            // Configure stage properties
            stage.setTitle("AI-Based Process Scheduler");
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.setScene(scene);
            
            // Center window on screen
            stage.centerOnScreen();
            
            // Set application icon if available
            try {
                Image icon = new Image(Objects.requireNonNull(
                    HelloApplication.class.getResourceAsStream("/com/example/os/icon.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon is optional, continue without it
                System.out.println("Application icon not found, continuing without icon.");
            }
            
            // Handle window close request gracefully
            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
            
            // Show stage
            stage.show();
            
            // Log successful initialization
            System.out.println("AI-Based Process Scheduler initialized successfully.");
            
        } catch (IOException e) {
            showErrorAndExit("Failed to load FXML", 
                "An error occurred while loading the user interface:\n" + 
                e.getMessage() + "\n\nPlease check that all resource files are present.");
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAndExit("Application Error", 
                "An unexpected error occurred during application startup:\n" + 
                e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays an error alert and exits the application
     */
    private void showErrorAndExit(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fatal Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.initStyle(StageStyle.UTILITY);
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
        });
    }
    
    /**
     * Main entry point for the application
     */
    public static void main(String[] args) {
        // Set system properties for better performance
        System.setProperty("javafx.animation.framerate", "60");
        System.setProperty("prism.lcdtext", "false");
        
        // Launch JavaFX application
        launch(args);
    }
}
