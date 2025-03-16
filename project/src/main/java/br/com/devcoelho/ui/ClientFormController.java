package br.com.devcoelho.ui;

import br.com.devcoelho.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Client Form window
 */
public class ClientFormController {

    private Bank bank;
    private List<Person> clients;
    private TextField nameField;
    private TextField cpfField;
    private TextField cepField;
    private TextField streetField;
    private TextField numberField;
    private TextField complementField;
    private TextField neighborhoodField;
    private TextField cityField;
    private ComboBox<String> stateComboBox;
    private Button saveButton;
    private Button cancelButton;
    private ProgressIndicator progressIndicator;
    private TextArea resultArea;
    
    public ClientFormController(Bank bank, List<Person> clients) {
        this.bank = bank;
        this.clients = clients;
    }
    
    /**
     * Creates the client form scene
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");
        
        // Create header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text headerText = new Text("Create New Client");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        headerText.setStyle("-fx-fill: #2c3e50;");
        headerBox.getChildren().add(headerText);
        mainLayout.setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));
        
        // Create form
        GridPane formGrid = createForm();
        mainLayout.setCenter(formGrid);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(24, 24);
        
        // Result area
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(60);
        resultArea.setWrapText(true);
        resultArea.setVisible(false);
        
        // Create buttons
        saveButton = new Button("Save Client");
        saveButton.setStyle(
            "-fx-background-color: #2ecc71;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 15;" +
            "-fx-background-radius: 5;"
        );
        
        cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #95a5a6;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 15;" +
            "-fx-background-radius: 5;"
        );
        
        buttonBox.getChildren().addAll(progressIndicator, cancelButton, saveButton);
        
        // Create bottom container
        VBox bottomBox = new VBox(10);
        bottomBox.getChildren().addAll(resultArea, buttonBox);
        BorderPane.setMargin(bottomBox, new Insets(20, 0, 0, 0));
        mainLayout.setBottom(bottomBox);
        
        // Set up event handlers
        setupEventHandlers();
        
        return new Scene(mainLayout, 600, 550);
    }
    
    /**
     * Creates the form with field labels and inputs
     */
    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Create labels
        Label nameLabel = new Label("Name:");
        Label cpfLabel = new Label("CPF:");
        Label cepLabel = new Label("CEP:");
        Label streetLabel = new Label("Street:");
        Label numberLabel = new Label("Number:");
        Label complementLabel = new Label("Complement:");
        Label neighborhoodLabel = new Label("Neighborhood:");
        Label cityLabel = new Label("City:");
        Label stateLabel = new Label("State:");
        
        // Style labels
        List<Label> labels = Arrays.asList(
            nameLabel, cpfLabel, cepLabel, streetLabel, numberLabel, 
            complementLabel, neighborhoodLabel, cityLabel, stateLabel
        );
        
        for (Label label : labels) {
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            label.setStyle("-fx-text-fill: #2c3e50;");
        }
        
        // Create input fields
        nameField = new TextField();
        nameField.setPromptText("Enter client's full name");
        
        cpfField = new TextField();
        cpfField.setPromptText("000.000.000-00");
        
        cepField = new TextField();
        cepField.setPromptText("00000-000");
        
        // Add a lookup button for CEP
        Button lookupButton = new Button("Lookup");
        lookupButton.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;"
        );
        
        HBox cepBox = new HBox(10);
        cepBox.getChildren().addAll(cepField, lookupButton);
        cepBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create remaining fields
        streetField = new TextField();
        numberField = new TextField();
        complementField = new TextField();
        neighborhoodField = new TextField();
        cityField = new TextField();
        
        // Create state dropdown
        stateComboBox = new ComboBox<>();
        stateComboBox.setPromptText("Select state");
        stateComboBox.setItems(FXCollections.observableArrayList(
            Arrays.stream(BrazilianState.values())
                .map(state -> state.getAbbreviation() + " - " + state.getFullName())
                .collect(Collectors.toList())
        ));
        
        // Add fields to the grid
        int row = 0;
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row, 2, 1);
        
        row++;
        grid.add(cpfLabel, 0, row);
        grid.add(cpfField, 1, row, 2, 1);
        
        row++;
        grid.add(cepLabel, 0, row);
        grid.add(cepBox, 1, row, 2, 1);
        
        row++;
        grid.add(streetLabel, 0, row);
        grid.add(streetField, 1, row, 2, 1);
        
        row++;
        grid.add(numberLabel, 0, row);
        grid.add(numberField, 1, row);
        
        grid.add(complementLabel, 2, row);
        grid.add(complementField, 3, row);
        
        row++;
        grid.add(neighborhoodLabel, 0, row);
        grid.add(neighborhoodField, 1, row, 2, 1);
        
        row++;
        grid.add(cityLabel, 0, row);
        grid.add(cityField, 1, row);
        
        grid.add(stateLabel, 2, row);
        grid.add(stateComboBox, 3, row);
        
        // Set column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(150);
        col2.setHgrow(Priority.ALWAYS);
        
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(100);
        
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setMinWidth(150);
        col4.setHgrow(Priority.ALWAYS);
        
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
        // Add CEP lookup functionality
        lookupButton.setOnAction(e -> lookupCEP());
        
        return grid;
    }
    
    /**
     * Sets up event handlers for buttons
     */
    private void setupEventHandlers() {
        // Save button event
        saveButton.setOnAction(e -> saveClient());
        
        // Cancel button event
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }
    
    /**
     * Performs CEP lookup and fills address fields
     */
    private void lookupCEP() {
        String cep = cepField.getText().trim();
        if (cep.isEmpty()) {
            showError("Please enter a CEP");
            return;
        }
        
        // Show progress indicator
        progressIndicator.setVisible(true);
        saveButton.setDisable(true);
        
        // Create a new address to validate
        Address tempAddress = new Address();
        
        // Use a new thread to avoid UI freezing
        new Thread(() -> {
            boolean valid = tempAddress.validateAndFillAddressByCep(cep);
            
            // Update UI with results on JavaFX thread
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                saveButton.setDisable(false);
                
                if (valid) {
                    // Fill form fields with address info
                    streetField.setText(tempAddress.getAddress());
                    neighborhoodField.setText(tempAddress.getNeighborhood());
                    cityField.setText(tempAddress.getCityName());
                    
                    // Set state if available
                    if (tempAddress.getState() != null) {
                        String stateOption = tempAddress.getState().getAbbreviation() + " - " + 
                                            tempAddress.getState().getFullName();
                        stateComboBox.setValue(stateOption);
                    }
                    
                    // Show success message
                    resultArea.setText("CEP found! Address details loaded.");
                    resultArea.setStyle("-fx-text-fill: #27ae60;");
                    resultArea.setVisible(true);
                } else {
                    // Show error message
                    resultArea.setText("Invalid CEP or address not found. Please enter address details manually.");
                    resultArea.setStyle("-fx-text-fill: #c0392b;");
                    resultArea.setVisible(true);
                }
            });
        }).start();
    }
    
    /**
     * Validates and saves the client information
     */
    private void saveClient() {
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            showError("Name is required");
            return;
        }
        
        if (cpfField.getText().trim().isEmpty()) {
            showError("CPF is required");
            return;
        }
        
        // Validate CPF format
        String cpf = cpfField.getText().trim();
        if (cpf.length() != 11 && cpf.length() != 14) {
            showError("Invalid CPF format. It must have 11 digits.");
            return;
        }
        
        // Check if the CPF already exists
        for (Person existingClient : clients) {
            if (existingClient.getCpf().equals(cpf)) {
                showError("A client with this CPF already exists.");
                return;
            }
        }
        
        // Create person
        Person person = new Person();
        person.setName(nameField.getText().trim());
        person.setCpf(cpf);
        
        // Create address
        Address address = new Address();
        address.setAddress(streetField.getText().trim());
        address.setHouseNumber(numberField.getText().trim());
        address.setHouseComplement(complementField.getText().trim());
        address.setNeighborhood(neighborhoodField.getText().trim());
        address.setCityName(cityField.getText().trim());
        address.setCepNumber(cepField.getText().trim());
        
        // Set state if selected
        if (stateComboBox.getValue() != null) {
            String stateCode = stateComboBox.getValue().split(" - ")[0];
            address.setState(BrazilianState.fromAbbreviation(stateCode));
        }
        
        // Set address type
        AddressType addressType = new AddressType();
        addressType.setAddressType("RESIDENTIAL");
        addressType.setAddressLocationType("HOME");
        address.setAddressLocationT(addressType);
        
        // Add address to person
        person.getAddress().add(address);
        
        // Add person to clients list and bank
        clients.add(person);
        bank.addClient(person);
        
        // Show success message
        resultArea.setText("Client created successfully!");
        resultArea.setStyle("-fx-text-fill: #27ae60;");
        resultArea.setVisible(true);
        
        // Close the window after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    
                    // Open account creation form (in a real app you'd implement this)
                    // openAccountTypeSelection(person);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Shows an error message in the result area
     */
    private void showError(String message) {
        resultArea.setText(message);
        resultArea.setStyle("-fx-text-fill: #c0392b;");
        resultArea.setVisible(true);
    }
}
