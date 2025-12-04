package com.example.os;

import com.example.os.ai.AIPredictor;
import com.example.os.ai.SchedulerRecommender;
import com.example.os.model.Process;
import com.example.os.model.ScheduleResult;
import com.example.os.model.GanttEntry;
import com.example.os.model.ComparisonResult;
import com.example.os.scheduler.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SchedulerController {
    @FXML ComboBox<String> algorithmCombo;
    @FXML TextField timeQuantumField;
    @FXML Button startButton;
    @FXML Button pauseButton;
    @FXML Button resetButton;
    @FXML TableView<Process> processTable;
    @FXML TableColumn<Process, Integer> idCol;
    @FXML TableColumn<Process, Integer> arrivalCol;
    @FXML TableColumn<Process, Integer> burstCol;
    @FXML TableColumn<Process, Integer> priorityCol;
    @FXML TableColumn<Process, Integer> waitingCol;
    @FXML TableColumn<Process, Integer> turnaroundCol;
    @FXML TableColumn<Process, Integer> completionCol;
    @FXML Label avgWaitingLabel;
    @FXML Label avgTurnaroundLabel;
    @FXML Label statusLabel;
    @FXML VBox ganttContainer;
    @FXML ScrollPane ganttScrollPane;
    @FXML Button compareButton;
    @FXML TableView<ComparisonResult> comparisonTable;
    @FXML TableColumn<ComparisonResult, String> algoCol;
    @FXML TableColumn<ComparisonResult, Double> waitCol;
    @FXML TableColumn<ComparisonResult, Double> turnCol;
    @FXML TableColumn<ComparisonResult, Double> scoreCol;
    @FXML Label bestSchedulerLabel;
    
    ObservableList<Process> processList = FXCollections.observableArrayList();
    ObservableList<ComparisonResult> comparisonList = FXCollections.observableArrayList();
    AIPredictor aiPredictor = new AIPredictor();
    SchedulerRecommender recommender = new SchedulerRecommender();
    boolean isRunning = false;
    Thread schedulerThread;
    Random random = new Random();
    
    @FXML
    void initialize() {
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().id).asObject());
        arrivalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().arrivalTime).asObject());
        burstCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().burstTime).asObject());
        priorityCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().priority).asObject());
        waitingCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().waitingTime).asObject());
        turnaroundCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().turnaroundTime).asObject());
        completionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().completionTime).asObject());
        
        processTable.setItems(processList);
        
        algorithmCombo.getItems().addAll("FCFS", "SJF", "Round Robin", "Priority", "Intelligent AI Scheduler");
        algorithmCombo.setValue("FCFS");
        
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        waitCol.setCellValueFactory(new PropertyValueFactory<>("avgWaitingTime"));
        turnCol.setCellValueFactory(new PropertyValueFactory<>("avgTurnaroundTime"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        
        waitCol.setCellFactory(column -> new TableCell<ComparisonResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        turnCol.setCellFactory(column -> new TableCell<ComparisonResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        scoreCol.setCellFactory(column -> new TableCell<ComparisonResult, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });
        
        comparisonTable.setItems(comparisonList);
        
        addSampleProcesses();
    }
    
    void addSampleProcesses() {
        processList.add(new Process(1, 0, 5, 3));
        processList.add(new Process(2, 1, 3, 1));
        processList.add(new Process(3, 2, 8, 4));
        processList.add(new Process(4, 3, 6, 2));
        processList.add(new Process(5, 4, 4, 5));
    }
    
    @FXML
    void handleAddProcess() {
        Dialog<Process> dialog = new Dialog<>();
        dialog.setTitle("Add Process");
        dialog.setHeaderText("Enter process details");
        
        TextField idField = new TextField();
        TextField arrivalField = new TextField();
        TextField burstField = new TextField();
        TextField priorityField = new TextField();
        
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
            new Label("Process ID:"), idField,
            new Label("Arrival Time:"), arrivalField,
            new Label("Burst Time:"), burstField,
            new Label("Priority:"), priorityField
        );
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int id = Integer.parseInt(idField.getText());
                    int arrival = Integer.parseInt(arrivalField.getText());
                    int burst = Integer.parseInt(burstField.getText());
                    int priority = Integer.parseInt(priorityField.getText());
                    return new Process(id, arrival, burst, priority);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(process -> {
            if (process != null) {
                processList.add(process);
            }
        });
    }
    
    @FXML
    void handleRemoveProcess() {
        Process selected = processTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            processList.remove(selected);
        }
    }
    
    @FXML
    void handleClearAll() {
        processList.clear();
        clearResults();
        comparisonList.clear();
        bestSchedulerLabel.setText("Run comparison first");
    }
    
    @FXML
    void handleRandomGenerate() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Generate Random Processes");
        dialog.setHeaderText("Enter number of processes to generate");
        
        TextField countField = new TextField("5");
        TextField minArrivalField = new TextField("0");
        TextField maxArrivalField = new TextField("10");
        TextField minBurstField = new TextField("1");
        TextField maxBurstField = new TextField("10");
        TextField minPriorityField = new TextField("1");
        TextField maxPriorityField = new TextField("5");
        
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
            new Label("Number of Processes:"), countField,
            new Label("Min Arrival Time:"), minArrivalField,
            new Label("Max Arrival Time:"), maxArrivalField,
            new Label("Min Burst Time:"), minBurstField,
            new Label("Max Burst Time:"), maxBurstField,
            new Label("Min Priority:"), minPriorityField,
            new Label("Max Priority:"), maxPriorityField
        );
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return Integer.parseInt(countField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(count -> {
            if (count != null && count > 0) {
                try {
                    int minArrival = Integer.parseInt(minArrivalField.getText());
                    int maxArrival = Integer.parseInt(maxArrivalField.getText());
                    int minBurst = Integer.parseInt(minBurstField.getText());
                    int maxBurst = Integer.parseInt(maxBurstField.getText());
                    int minPriority = Integer.parseInt(minPriorityField.getText());
                    int maxPriority = Integer.parseInt(maxPriorityField.getText());
                    
                    processList.clear();
                    for (int i = 1; i <= count; i++) {
                        int arrival = minArrival + random.nextInt(maxArrival - minArrival + 1);
                        int burst = minBurst + random.nextInt(maxBurst - minBurst + 1);
                        int priority = minPriority + random.nextInt(maxPriority - minPriority + 1);
                        processList.add(new Process(i, arrival, burst, priority));
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid input values");
                }
            }
        });
    }
    
    @FXML
    void handleStart() {
        if (processList.isEmpty()) {
            showAlert("No processes to schedule");
            return;
        }
        
        isRunning = true;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        statusLabel.setText("Running...");
        statusLabel.setTextFill(Color.GREEN);
        
        schedulerThread = new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> runScheduler());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        schedulerThread.start();
    }
    
    @FXML
    void handlePause() {
        isRunning = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        statusLabel.setText("Paused");
        statusLabel.setTextFill(Color.ORANGE);
    }
    
    @FXML
    void handleReset() {
        isRunning = false;
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
        startButton.setDisable(false);
        compareButton.setDisable(false);
        pauseButton.setDisable(true);
        statusLabel.setText("Ready");
        statusLabel.setTextFill(Color.GREEN);
        clearResults();
        comparisonList.clear();
        bestSchedulerLabel.setText("Run comparison first");
        aiPredictor.reset();
    }
    
    @FXML
    void handleCompareAll() {
        if (processList.isEmpty()) {
            showAlert("No processes to schedule");
            return;
        }
        
        isRunning = true;
        compareButton.setDisable(true);
        startButton.setDisable(true);
        statusLabel.setText("Comparing all algorithms...");
        statusLabel.setTextFill(Color.BLUE);
        
        schedulerThread = new Thread(() -> {
            try {
                Thread.sleep(300);
                Platform.runLater(() -> runComparison());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        schedulerThread.start();
    }
    
    void runComparison() {
        List<Process> processes = new ArrayList<>(processList);
        List<ComparisonResult> results = new ArrayList<>();
        
        String[] algorithms = {"FCFS", "SJF", "Round Robin", "Priority", "Intelligent AI Scheduler"};
        
        AIPredictor freshPredictor = new AIPredictor();
        
        for (String algo : algorithms) {
            List<Process> processCopy = new ArrayList<>();
            for (Process p : processes) {
                processCopy.add(p.copy());
            }
            
            Scheduler scheduler = createScheduler(algo, freshPredictor);
            ScheduleResult result = scheduler.schedule(processCopy);
            results.add(new ComparisonResult(algo, result));
        }
        
        comparisonList.clear();
        comparisonList.addAll(results);
        
        ComparisonResult best = recommender.getBestResult(results);
        if (best != null) {
            bestSchedulerLabel.setText(best.algorithmName + " (Score: " + String.format("%.4f", best.score) + ")");
            bestSchedulerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        }
        
        isRunning = false;
        compareButton.setDisable(false);
        startButton.setDisable(false);
        statusLabel.setText("Comparison completed");
        statusLabel.setTextFill(Color.GREEN);
    }
    
    void runScheduler() {
        List<Process> processes = new ArrayList<>(processList);
        String algorithm = algorithmCombo.getValue();
        if (algorithm == null) algorithm = "FCFS";
        
        Scheduler scheduler = createScheduler(algorithm);
        ScheduleResult result = scheduler.schedule(processes);
        
        updateUI(result);
        
        isRunning = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        statusLabel.setText("Completed");
        statusLabel.setTextFill(Color.BLUE);
    }
    
    Scheduler createScheduler(String algorithm) {
        return createScheduler(algorithm, aiPredictor);
    }
    
    Scheduler createScheduler(String algorithm, AIPredictor predictor) {
        switch (algorithm) {
            case "SJF":
                return new SJFScheduler();
            case "Round Robin":
                int quantum = 2;
                try {
                    quantum = Integer.parseInt(timeQuantumField.getText());
                } catch (NumberFormatException e) {}
                return new RoundRobinScheduler(quantum);
            case "Priority":
                return new PriorityScheduler();
            case "Intelligent AI Scheduler":
                return new IntelligentAIScheduler(predictor);
            default:
                return new FCFSScheduler();
        }
    }
    
    void updateUI(ScheduleResult result) {
        processList.clear();
        processList.addAll(result.processes);
        processTable.refresh();
        
        avgWaitingLabel.setText(String.format("%.2f", result.avgWaitingTime));
        avgTurnaroundLabel.setText(String.format("%.2f", result.avgTurnaroundTime));
        
        drawGanttChart(result.ganttChart);
    }
    
    void drawGanttChart(List<GanttEntry> ganttChart) {
        ganttContainer.getChildren().clear();
        
        if (ganttChart.isEmpty()) return;
        
        int maxTime = ganttChart.get(ganttChart.size() - 1).endTime;
        double containerWidth = ganttScrollPane.getWidth() > 0 ? ganttScrollPane.getWidth() - 40 : 700;
        double scale = Math.max(5.0, containerWidth / Math.max(1, maxTime));
        
        HBox chartBox = new HBox(0);
        chartBox.setStyle("-fx-padding: 10; -fx-alignment: center-left;");
        
        Color[] colors = {Color.web("#3498db"), Color.web("#e74c3c"), Color.web("#2ecc71"), 
                         Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"), 
                         Color.web("#e67e22"), Color.web("#34495e")};
        
        for (GanttEntry entry : ganttChart) {
            double width = Math.max(20, (entry.endTime - entry.startTime) * scale);
            Rectangle rect = new Rectangle(width, 50);
            rect.setFill(colors[Math.abs(entry.processId) % colors.length]);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1.5);
            
            VBox vbox = new VBox(2);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
            Text label = new Text("P" + entry.processId);
            label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            Text timeLabel = new Text(entry.startTime + "-" + entry.endTime);
            timeLabel.setStyle("-fx-font-size: 9px;");
            vbox.getChildren().addAll(rect, label, timeLabel);
            
            chartBox.getChildren().add(vbox);
        }
        
        HBox timeBox = new HBox();
        timeBox.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
        int step = Math.max(1, maxTime / Math.min(20, maxTime));
        for (int i = 0; i <= maxTime; i += step) {
            Text timeText = new Text(String.valueOf(i));
            timeText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            timeBox.getChildren().add(timeText);
            if (i < maxTime) {
                Region spacer = new Region();
                double spacerWidth = scale * step - 30;
                spacer.setPrefWidth(Math.max(0, spacerWidth));
                timeBox.getChildren().add(spacer);
            }
        }
        
        ganttContainer.getChildren().addAll(chartBox, new javafx.scene.control.Separator());
    }
    
    void clearResults() {
        for (Process p : processList) {
            p.waitingTime = 0;
            p.turnaroundTime = 0;
            p.completionTime = 0;
            p.startTime = -1;
            p.remainingTime = p.burstTime;
        }
        processTable.refresh();
        avgWaitingLabel.setText("0.00");
        avgTurnaroundLabel.setText("0.00");
        ganttContainer.getChildren().clear();
    }
    
    void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

