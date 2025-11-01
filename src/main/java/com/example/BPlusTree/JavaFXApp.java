package com.example.BPlusTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.BPlusTree.Storage.Block;
import com.example.BPlusTree.tree.BPlusTree;
import com.example.BPlusTree.tree.InternalNode;
import com.example.BPlusTree.tree.LeafNode;
import com.example.BPlusTree.tree.Node;

import com.example.BPlusTree.Storage.Record;
import com.example.BPlusTree.Storage.FileSystem;
import com.example.BPlusTree.Storage.CSVLoader;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class JavaFXApp extends Application {


    private BPlusTree tree;
    private Canvas canvas;
    private TextArea logArea;
    private TextField keyInput;
    private TextField deleteInput;
    private TextField orderInput;
    private final Map<LeafNode, double[]> leafPositions = new HashMap<>();
    private FileSystem fileSystem = new FileSystem();
    private List<Record> loadedRecords = new ArrayList<>();



    @Override
    public void start(Stage primaryStage) {
        tree = new BPlusTree(3);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topPanel = createControlPanel();
        root.setTop(topPanel);

        canvas = new Canvas(1200, 600);
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setFont(Font.font("Monospaced", 12));
        root.setBottom(logArea);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("B+ Tree Visualization");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        drawTree();
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Label titleLabel = new Label("B+ Tree Visualization");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setStyle("-fx-font-weight: bold;");

        HBox orderBox = new HBox(10);
        orderBox.setAlignment(Pos.CENTER_LEFT);
        Label orderLabel = new Label("Order:");
        orderInput = new TextField("3");
        orderInput.setPrefWidth(60);
        Button createButton = new Button("Create New Tree");
        createButton.setOnAction(e -> createNewTree());
        orderInput.setOnAction(e -> createButton.fire());
        orderBox.getChildren().addAll(orderLabel, orderInput, createButton);

        HBox insertBox = new HBox(10);
        insertBox.setAlignment(Pos.CENTER_LEFT);
        Label keyLabel = new Label("Key:");
        keyInput = new TextField();
        keyInput.setPromptText("Enter key");
        keyInput.setPrefWidth(80);
        Button insertButton = new Button("Insert");
        insertButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        insertButton.setOnAction(e -> insertKey());
        keyInput.setOnAction(e -> insertButton.fire());
        insertBox.getChildren().addAll(keyLabel, keyInput, insertButton);

        HBox deleteBox = new HBox(10);
        deleteBox.setAlignment(Pos.CENTER_LEFT);
        Label deleteLabel = new Label("Delete Key:");
        deleteInput = new TextField();
        deleteInput.setPromptText("Enter key");
        deleteInput.setPrefWidth(80);
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteKey(deleteInput.getText()));
        deleteInput.setOnAction(e -> deleteButton.fire());
        deleteBox.getChildren().addAll(deleteLabel, deleteInput, deleteButton);

        HBox bulkBox = new HBox(10);
        bulkBox.setAlignment(Pos.CENTER_LEFT);
        Label bulkLabel = new Label("Bulk Insert:");
        TextField bulkInput = new TextField();
        bulkInput.setPromptText("e.g., 10,20,30,40,50");
        bulkInput.setPrefWidth(200);
        Button bulkButton = new Button("Insert Multiple");
        bulkButton.setOnAction(e -> bulkInsert(bulkInput.getText()));
        bulkInput.setOnAction(e -> bulkButton.fire());
        Button clearButton = new Button("Clear Tree");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearButton.setOnAction(e -> clearTree());
        bulkBox.getChildren().addAll(bulkLabel, bulkInput, bulkButton, clearButton);

        // Add deleteBox here so the simple delete button appears in the main panel
        panel.getChildren().addAll(titleLabel, orderBox, insertBox, deleteBox, bulkBox);


        HBox loadBox = new HBox(10);
        loadBox.setAlignment(Pos.CENTER_LEFT);
        Button loadCsvButton = new Button("Load CSV and Build Index");
        loadCsvButton.setOnAction(e -> loadCsvData());
        Button insertRecords = new Button("Insert Records");
        insertRecords.setOnAction(e -> Insertions());
        Button deleteRecords = new Button("Delete Records");
        deleteRecords.setOnAction(e -> Deletions());

        TextField ssnDeleteInput = new TextField();
        ssnDeleteInput.setPromptText("Enter SSN");
        ssnDeleteInput.setPrefWidth(250);
        Button deleteBySSNButton = new Button("Delete by SSN");
        deleteBySSNButton.setOnAction(e -> deleteRecordsBySSNUI(ssnDeleteInput.getText()));

        TextField ssnInsertInput = new TextField();
        ssnInsertInput.setPromptText("Enter Line Number");
        ssnInsertInput.setPrefWidth(250);
        Button insertbySSNButton = new Button("Insert by Line Number in CSV");
        insertbySSNButton.setOnAction(e -> insertRecordsByNumberUI(ssnInsertInput.getText()));

        loadBox.getChildren().addAll(loadCsvButton, insertRecords, deleteRecords, ssnDeleteInput, deleteBySSNButton, ssnInsertInput, insertbySSNButton);
        panel.getChildren().add(loadBox);

        return panel;
    }
    private void insertRecordsByNumberUI(String text) {
        try {
            int recordNumber = Integer.parseInt(text);
            Record r = this.loadedRecords.get(recordNumber - 2); // fix -1 index problem for header

            String numericPart = r.getSSN().replaceAll("[^0-9]", "");
            int key = Integer.parseInt(numericPart);

            if (tree.contains(key)) {
                log("Record with SSN=" + r.getSSN() + " already exists!");
                showAlert("Duplicate Record", "SSN " + r.getSSN() + " is already in the system.");
                return;
            }
            int pointer = fileSystem.insertRecord(r);
            tree.insert(key, pointer);
            log("Inserted record SSN=" + r.getSSN() + " (ptr=" + pointer + ")");

            fileSystem.printBlocks();
            drawTree();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid record number");
        } catch (IndexOutOfBoundsException e) {
            showAlert("Invalid Record", "Record number out of range");
        }
    }

    private void deleteRecordsBySSNUI(String input) {
        log("Current FileSystem contents before deletion:");
        int i = 0;
        for (Block b : fileSystem.getBlocks()) {
            for (Record r : b.getRecords()) {
                log("Block " + i + " contains SSN=" + r.getSSN());
                i += 1;
            }
        }

        try {
            String ssn = input.trim();
            String numericPart = ssn.replaceAll("[^0-9]", "");
            int key = Integer.parseInt(numericPart);

            // Delete from tree and get the record pointer
            int recordPointer = tree.delete(key);

            if (recordPointer != -1) {
                // Use the pointer to directly delete from file system
                boolean deletedFromFile = fileSystem.deleteRecordByPointer(recordPointer);
                log("Deleted SSN=" + ssn + " (pointer=" + recordPointer +
                        (deletedFromFile ? ", removed from file)" : ", file delete failed)"));
            } else {
                log("SSN " + ssn + " not found in tree.");
            }

            fileSystem.printBlocks();
            drawTree();
        } catch (Exception e) {
            showAlert("Error", "Failed to process deletion: " + e.getMessage());
        }
    }


    private void createNewTree() {
        try {
            int order = Integer.parseInt(orderInput.getText());
            if (order < 3) {
                showAlert("Invalid Order", "Order must be at least 3");
                return;
            }
            tree = new BPlusTree(order);
            logArea.clear();
            log("Created new B+ Tree with order: " + order);
            drawTree();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for order");
        }
    }

    private void insertKey() {
        try {
            int key = Integer.parseInt(keyInput.getText());

            // Check for duplicate
            if (tree.contains(key)) {
                showAlert("Duplicate Key", key + " already exists");
                keyInput.clear();
                return;
            }

            Record dummyRecord = new Record(
                    "Manual Entry",           // NAME
                    String.valueOf(key),      // SSN (use the key as SSN)
                    "N/A",                    // DEPARTMENTCODE
                    "N/A",                    // ADDRESS
                    "N/A",                    // PHONE
                    "N/A",                    // BIRTHDATE
                    'U',                      // SEX
                    "N/A",                    // JOBCODE
                    0                       // SALARY
            );

            int pointer = fileSystem.insertRecord(dummyRecord);

            boolean success = tree.insert(key, pointer);

            if (success) {
                log("✓ Inserted key: " + key + " (ptr=" + pointer + ")");
                fileSystem.printBlocks();
                drawTree();
            } else {
                showAlert("Error", "Failed to insert key " + key);
            }

            keyInput.clear();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid number for key");
        } catch (Exception e) {
            showAlert("Error", "Failed to insert: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void deleteKey(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                showAlert("Invalid Input", "Please enter a key to delete");
                return;
            }

            int key = Integer.parseInt(input.trim());

            if (!tree.contains(key)) {
                showAlert("Key Not Found", "Key " + key + " does not exist in the tree.");
                deleteInput.clear();
                return;
            }

            int recordPointer = tree.delete(key);

            if (recordPointer != -1) {
                if (recordPointer >= 0) {
                    boolean deletedFromFile = fileSystem.deleteRecordByPointer(recordPointer);
                    log("✓ Deleted key: " + key + " (ptr=" + recordPointer +
                            (deletedFromFile ? ", removed from file)" : ", file delete failed)"));
                } else {
                    log("✓ Deleted key: " + key + " (tree only, no file record)");
                }
                fileSystem.printBlocks();
                drawTree();
            } else {
                showAlert("Key Not Found", "Key " + key + " does not exist in the tree.");
            }
            deleteInput.clear();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for key");
        } catch (Exception e) {
            showAlert("Error", "Failed to delete: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bulkInsert(String input) {
        try {
            String[] strings = input.split(",+");
            List<Integer> keys = new ArrayList<>();

            for (String s : strings) {
                String trimmed = s.trim();
                if (trimmed.isEmpty()) continue;
                keys.add(Integer.parseInt(trimmed));
            }

            if (keys.isEmpty()) {
                showAlert("Invalid Input", "No valid keys to insert");
                return;
            }

            for (int key : keys) {
                boolean success = tree.insert(key, -1);
                if (success) {
                    log("Inserted key: " + key);
                }
                else {
                    log(key + " exists and have been skipped");
                }
            }

            drawTree();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter comma-separated numbers");
        } catch (Exception e) {
            showAlert("Error", "Failed to insert: " + e.getMessage());
        }


    }

    private void loadCsvData() {
        try {
            String csvPath = "src\\main\\java\\com\\example\\BPlusTree\\data\\EMPLOYEE.csv";
            loadedRecords = CSVLoader.loadRecords(csvPath);
            log("Loaded " + loadedRecords.size() + " records from CSV.");

            for (int i = 0; i < Math.min(10, loadedRecords.size()); i++) {
                Record r = loadedRecords.get(i);
                int pointer = fileSystem.insertRecord(r);

                String numericPart = r.getSSN().replaceAll("[^0-9]", ""); // 3ashan el B+ tree integer
                int key = Integer.parseInt(numericPart);
                tree.insert(key, pointer);
                log("Inserted record SSN=" + r.getSSN() + " (ptr=" + pointer + ")");
            }

            fileSystem.printBlocks();
            drawTree();

        } catch (IOException e) {
            showAlert("File Error", "Could not read CSV: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to process CSV: " + e.getMessage());
        }
    }


    private void Insertions() {
        try {
            int[] OriginalLineNumbers = {27, 14, 22};

            for (int recordNumber : OriginalLineNumbers) {
                Record r = this.loadedRecords.get(recordNumber - 2); // fix -1 index problem for header
                String numericPart = r.getSSN().replaceAll("[^0-9]", "");
                int key = Integer.parseInt(numericPart);

                if (tree.contains(key)) {
                    log("Record with SSN=" + r.getSSN() + " already exists! Skipping...");
                    continue;
                }

                int pointer = fileSystem.insertRecord(r);
                tree.insert(key, pointer);
                log("Inserted record SSN=" + r.getSSN() + " (ptr=" + pointer + ")");
            }

            fileSystem.printBlocks();
            drawTree();
        } catch (Exception e) {
            showAlert("Error", "Failed to process insertions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void Deletions() {
        try {
            int[] recordNumbers = {11, 6, 3};

            for (int recordNumber : recordNumbers) {
                Record r = loadedRecords.get(recordNumber - 2);
                String ssn = r.getSSN();
                String numericPart = ssn.replaceAll("[^0-9]", "");
                int key = Integer.parseInt(numericPart);
                if (!tree.contains(key)) {
                    log("✗ SSN " + ssn + " not found in tree. Skipping...");
                    continue;
                }

                int recordPointer = tree.delete(key);

                if (recordPointer != -1) {

                    boolean deletedFromFile = fileSystem.deleteRecordByPointer(recordPointer);

                    if (deletedFromFile) {
                        log("✓ Deleted SSN=" + ssn + " (ptr=" + recordPointer + ")");
                    } else {
                        log("✗ Failed to delete SSN=" + ssn + " from file system");
                    }
                } else {
                    log("✗ SSN " + ssn + " not found in tree (unexpected error)");
                }
            }

            fileSystem.printBlocks();
            drawTree();
        } catch (Exception e) {
            showAlert("Error", "Failed to process deletions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearTree() {
        int order = tree.getOrder();
        tree = new BPlusTree(order);
        logArea.clear();
        log("Tree cleared");
        drawTree();
    }

    private void drawTree() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        leafPositions.clear();

        if (tree.getRoot() != null) {
            if (tree.getRoot().getKeys().isEmpty()) {
                return;
            }
            drawNode(gc, tree.getRoot(), canvas.getWidth() / 2, 50, canvas.getWidth() - 100);
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(1.5);

        for (LeafNode leaf : leafPositions.keySet()) {
            LeafNode next = leaf.getNext();
            if (next != null && leafPositions.containsKey(next)) {
                double[] start = leafPositions.get(leaf);
                double[] end = leafPositions.get(next);

                double startX = start[0] + start[2] / 2;
                double startY = start[1];
                double endX = end[0] - end[2] / 2;
                double endY = end[1];

                gc.setStroke(Color.RED);
                gc.setLineWidth(1.5);

                gc.strokeLine(startX, startY, endX, endY);

                double arrowSize = 6;
                gc.strokeLine(endX - arrowSize, endY - arrowSize / 2, endX, endY);
                gc.strokeLine(endX - arrowSize, endY + arrowSize / 2, endX, endY);
            }
        }
    }

    private void drawNode(GraphicsContext gc, Node node, double x, double y, double totalWidth) {

        final double nodeHeight = 70;
        final double keyWidth = 70;
        gc.setFont(Font.font("Consolas", 20));
        final int keysCount = node.getKeys().size();
        final double nodeWidth = keyWidth * Math.max(keysCount, 1);

        gc.setFill(node.isLeaf() ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        gc.fillRoundRect(x - nodeWidth / 2, y, nodeWidth, nodeHeight, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x - nodeWidth / 2, y, nodeWidth, nodeHeight, 10, 10);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < keysCount; i++) {
            double keyX = x - nodeWidth / 2 + keyWidth * i + keyWidth / 2;
            gc.fillText(String.valueOf(node.getKeys().get(i)), keyX, y + nodeHeight / 2 + 5);
            if (i > 0) {
                gc.strokeLine(x - nodeWidth / 2 + keyWidth * i, y,
                        x - nodeWidth / 2 + keyWidth * i, y + nodeHeight);
            }
        }

        if (!node.isLeaf()) {
            InternalNode internal = (InternalNode) node;
            double childY = y + nodeHeight + 80;

            int totalLeaves = countLeaves(node);
            double currentX = x - (totalWidth / 2);

            for (int i = 0; i < internal.getPointers().size(); i++) {
                Node child = internal.getPointers().get(i);
                int childLeaves = countLeaves(child);
                double childWidth = totalWidth * ((double) childLeaves / totalLeaves);
                double childCenterX = currentX + childWidth / 2;

                double pointerX;
                if (i == 0) {
                    pointerX = x - nodeWidth / 2;
                } else if (i == internal.getKeys().size()) {
                    pointerX = x + nodeWidth / 2;
                } else {
                    pointerX = x - nodeWidth / 2 + i * keyWidth;
                }

                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(1);
                gc.strokeLine(pointerX, y + nodeHeight - 5, pointerX, y + nodeHeight);

                double x1 = pointerX;
                double y1 = y + nodeHeight;
                double x2 = childCenterX;
                double y2 = childY;

                gc.setStroke(Color.GRAY);
                gc.setLineWidth(1.5);
                gc.strokeLine(x1, y1, x2, y2);

                double angle = Math.atan2(y2 - y1, x2 - x1);
                double arrowLength = 8;
                double arrowAngle = Math.toRadians(25);

                double xArrow1 = x2 - arrowLength * Math.cos(angle - arrowAngle);
                double yArrow1 = y2 - arrowLength * Math.sin(angle - arrowAngle);
                double xArrow2 = x2 - arrowLength * Math.cos(angle + arrowAngle);
                double yArrow2 = y2 - arrowLength * Math.sin(angle + arrowAngle);

                gc.strokeLine(x2, y2, xArrow1, yArrow1);
                gc.strokeLine(x2, y2, xArrow2, yArrow2);

                drawNode(gc, child, childCenterX, childY, childWidth);

                currentX += childWidth;
            }
        } else {
            LeafNode leaf = (LeafNode) node;
            leafPositions.put(leaf, new double[]{x, y + nodeHeight / 2, nodeWidth});
        }
    }

    private int countLeaves(Node node) {
        if (node.isLeaf()) {
            return 1;
        }
        InternalNode internal = (InternalNode) node;
        int sum = 0;
        for (Node child : internal.getPointers()) {
            sum += countLeaves(child);
        }
        return sum;
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}