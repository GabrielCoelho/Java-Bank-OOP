package br.com.devcoelho.ui;

import br.com.devcoelho.Bank;
import br.com.devcoelho.BankAccount;
import br.com.devcoelho.Person;
import br.com.devcoelho.persistence.FilePersistence;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Main JavaFX Application for the Banking System */
public class MainApplication extends Application {

  private Bank bank;
  private List<Person> clients = new ArrayList<>();
  private List<BankAccount> accounts = new ArrayList<>();

  @Override
  public void start(Stage primaryStage) {
    // Initialize bank
    bank = new Bank("Potato's Bank", "777");

    // Load data
    loadData();

    // Create main layout
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Header
    VBox headerBox = createHeader();
    mainLayout.setTop(headerBox);

    // Main menu
    VBox menuBox = createMainMenu(primaryStage);
    mainLayout.setCenter(menuBox);

    // Footer
    Label footerLabel = new Label("© 2025 Potato's Bank. All rights reserved.");
    footerLabel.setStyle("-fx-text-fill: #555555;");
    BorderPane.setAlignment(footerLabel, Pos.CENTER);
    BorderPane.setMargin(footerLabel, new Insets(20, 0, 0, 0));
    mainLayout.setBottom(footerLabel);

    // Create scene and show stage
    Scene scene = new Scene(mainLayout, 600, 500);
    primaryStage.setTitle("Potato's Bank - Banking System");
    primaryStage.setScene(scene);
    primaryStage.setResizable(true);
    primaryStage.show();
  }

  /** Create the header section with bank logo and title */
  private VBox createHeader() {
    VBox headerBox = new VBox(10);
    headerBox.setAlignment(Pos.CENTER);
    headerBox.setPadding(new Insets(0, 0, 20, 0));

    // Create a placeholder for bank logo
    ImageView logoView = new ImageView();
    try {
      // You would replace this with your actual bank logo
      Image logoImage = new Image(getClass().getResourceAsStream("/images/bank_logo.png"));
      logoView.setImage(logoImage);
      logoView.setFitHeight(80);
      logoView.setPreserveRatio(true);
    } catch (Exception e) {
      // If logo image is not available, create a text logo
      Text logoText = new Text("🏦");
      logoText.setFont(Font.font("Arial", FontWeight.BOLD, 50));
      headerBox.getChildren().add(logoText);
    }

    // Bank name label
    Label nameLabel = new Label(bank.getName());
    nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    nameLabel.setStyle("-fx-text-fill: #2c3e50;");

    // Subtitle
    Label subtitleLabel = new Label("Banking Management System");
    subtitleLabel.setFont(Font.font("Arial", 14));
    subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

    // Add components to header
    headerBox.getChildren().addAll(logoView, nameLabel, subtitleLabel);

    return headerBox;
  }

  /** Create the main menu with buttons for different operations */
  private VBox createMainMenu(Stage primaryStage) {
    VBox menuBox = new VBox(15);
    menuBox.setAlignment(Pos.CENTER);
    menuBox.setPadding(new Insets(20));
    menuBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Status summary
    Text statusText =
        new Text(
            String.format(
                "System Status: %d clients, %d accounts", clients.size(), accounts.size()));
    statusText.setFont(Font.font("Arial", 12));
    statusText.setStyle("-fx-fill: #34495e;");

    // Create menu buttons
    Button createAccountBtn = createMenuButton("Create Account", "#2ecc71");
    Button manageAccountBtn = createMenuButton("Manage Account", "#3498db");
    Button removeAccountBtn = createMenuButton("Remove Account", "#e74c3c");
    Button accountInfoBtn = createMenuButton("Account Information", "#f39c12");
    Button simulateTimeBtn = createMenuButton("Simulate Time Passage", "#9b59b6");
    Button exitBtn = createMenuButton("Exit System", "#7f8c8d");

    // Add button event handlers
    createAccountBtn.setOnAction(e -> openCreateAccountWindow(primaryStage));
    manageAccountBtn.setOnAction(e -> openManageAccountWindow(primaryStage));
    removeAccountBtn.setOnAction(e -> openRemoveAccountWindow(primaryStage));
    accountInfoBtn.setOnAction(e -> openAccountInfoWindow(primaryStage));
    simulateTimeBtn.setOnAction(e -> openSimulateTimeWindow(primaryStage));
    exitBtn.setOnAction(
        e -> {
          saveData();
          Platform.exit();
        });

    // Add all components to the menu
    menuBox
        .getChildren()
        .addAll(
            statusText,
            createAccountBtn,
            manageAccountBtn,
            removeAccountBtn,
            accountInfoBtn,
            simulateTimeBtn,
            exitBtn);

    return menuBox;
  }

  /** Create a styled menu button */
  private Button createMenuButton(String text, String color) {
    Button button = new Button(text);
    button.setPrefWidth(250);
    button.setPrefHeight(40);
    button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    button.setStyle(
        "-fx-background-color: "
            + color
            + ";"
            + "-fx-text-fill: white;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;");

    // Add hover effect
    button.setOnMouseEntered(
        e ->
            button.setStyle(
                "-fx-background-color: derive("
                    + color
                    + ", 20%);"
                    + "-fx-text-fill: white;"
                    + "-fx-background-radius: 5;"
                    + "-fx-cursor: hand;"));

    button.setOnMouseExited(
        e ->
            button.setStyle(
                "-fx-background-color: "
                    + color
                    + ";"
                    + "-fx-text-fill: white;"
                    + "-fx-background-radius: 5;"
                    + "-fx-cursor: hand;"));

    return button;
  }

  /** Opens the Create Account window */
  private void openCreateAccountWindow(Stage primaryStage) {
    // First we need to determine if we'll create a client first or use existing
    if (clients.isEmpty()) {
      // If no clients exist, open client creation form directly
      openCreateClientWindow(primaryStage);
    } else {
      // Create a custom dialog stage
      final Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(primaryStage);
      dialog.setTitle("Client Selection");

      // Create the dialog content
      VBox dialogVbox = new VBox(20);
      dialogVbox.setPadding(new Insets(20));
      dialogVbox.setAlignment(Pos.CENTER);

      // Add title and message
      Text titleText = new Text("Client Selection");
      titleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

      Text messageText = new Text("You have existing clients. What would you like to do?");
      messageText.setFont(Font.font("Arial", 14));

      // Create buttons
      Button existingClientBtn = new Button("Use Existing Client");
      existingClientBtn.setPrefWidth(200);
      existingClientBtn.setPrefHeight(40);
      existingClientBtn.setStyle(
          "-fx-background-color: #3498db;"
              + "-fx-text-fill: white;"
              + "-fx-font-weight: bold;"
              + "-fx-background-radius: 5;");

      Button newClientBtn = new Button("Create New Client");
      newClientBtn.setPrefWidth(200);
      newClientBtn.setPrefHeight(40);
      newClientBtn.setStyle(
          "-fx-background-color: #2ecc71;"
              + "-fx-text-fill: white;"
              + "-fx-font-weight: bold;"
              + "-fx-background-radius: 5;");

      Button cancelBtn = new Button("Cancel");
      cancelBtn.setPrefWidth(200);
      cancelBtn.setPrefHeight(40);
      cancelBtn.setStyle(
          "-fx-background-color: #7f8c8d;"
              + "-fx-text-fill: white;"
              + "-fx-font-weight: bold;"
              + "-fx-background-radius: 5;");

      // Set button actions
      existingClientBtn.setOnAction(
          e -> {
            dialog.close();
            openSelectClientWindow(primaryStage);
          });

      newClientBtn.setOnAction(
          e -> {
            dialog.close();
            openCreateClientWindow(primaryStage);
          });

      cancelBtn.setOnAction(e -> dialog.close());

      // Add all elements to dialog
      dialogVbox
          .getChildren()
          .addAll(
              titleText, messageText, new Separator(), existingClientBtn, newClientBtn, cancelBtn);

      // Create scene and show dialog
      Scene dialogScene = new Scene(dialogVbox, 350, 300);
      dialog.setScene(dialogScene);
      dialog.show();
    }
  }

  /** Opens the Create Client window */
  private void openCreateClientWindow(Stage primaryStage) {
    Stage clientStage = new Stage();
    clientStage.setTitle("Create New Client");

    ClientFormController formController = new ClientFormController(bank, clients);
    Scene clientScene = formController.createScene();

    clientStage.setScene(clientScene);
    clientStage.show();
  }

  /** Opens the Select Client window */
  private void openSelectClientWindow(Stage primaryStage) {
    Stage clientSelectionStage = new Stage();
    clientSelectionStage.setTitle("Select Client");

    ClientSelectionController selectionController =
        new ClientSelectionController(bank, clients, accounts);
    Scene selectionScene = selectionController.createScene();

    clientSelectionStage.setScene(selectionScene);
    clientSelectionStage.show();
  }

  /** Opens the Manage Account window */
  private void openManageAccountWindow(Stage primaryStage) {
    if (accounts.isEmpty()) {
      // Show message if no accounts exist
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Accounts");
      alert.setHeaderText("No Accounts Available");
      alert.setContentText(
          "There are no accounts in the system to manage. Please create an account first.");
      alert.showAndWait();
      return;
    }

    Stage managementStage = new Stage();
    managementStage.setTitle("Account Management");

    AccountManagementController managementController = new AccountManagementController(accounts);
    Scene managementScene = managementController.createScene();

    managementStage.setScene(managementScene);
    managementStage.show();
  }

  /** Opens the Remove Account window */
  private void openRemoveAccountWindow(Stage primaryStage) {
    if (accounts.isEmpty()) {
      // Show message if no accounts exist
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Accounts");
      alert.setHeaderText("No Accounts Available");
      alert.setContentText("There are no accounts in the system to remove.");
      alert.showAndWait();
      return;
    }

    Stage removalStage = new Stage();
    removalStage.setTitle("Remove Account");

    AccountRemovalController removalController = new AccountRemovalController(bank, accounts);
    Scene removalScene = removalController.createScene();

    removalStage.setScene(removalScene);
    removalStage.show();
  }

  /** Opens the Account Information window */
  private void openAccountInfoWindow(Stage primaryStage) {
    if (accounts.isEmpty()) {
      // Show message if no accounts exist
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Accounts");
      alert.setHeaderText("No Accounts Available");
      alert.setContentText("There are no accounts in the system to display.");
      alert.showAndWait();
      return;
    }

    Stage infoStage = new Stage();
    infoStage.setTitle("Account Information");

    AccountInfoController infoController = new AccountInfoController(bank, accounts);
    Scene infoScene = infoController.createScene();

    infoStage.setScene(infoScene);
    infoStage.show();
  }

  /** Opens the Simulate Time Passage window */
  private void openSimulateTimeWindow(Stage primaryStage) {
    if (accounts.isEmpty()) {
      // Show message if no accounts exist
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Accounts");
      alert.setHeaderText("No Accounts Available");
      alert.setContentText("There are no accounts in the system to simulate time passage.");
      alert.showAndWait();
      return;
    }

    Stage simulationStage = new Stage();
    simulationStage.setTitle("Time Passage Simulation");

    TimeSimulationController simulationController = new TimeSimulationController(accounts);
    Scene simulationScene = simulationController.createScene();

    simulationStage.setScene(simulationScene);
    simulationStage.show();
  }

  /** Load data from text files */
  private void loadData() {
    try {
      clients = FilePersistence.loadClients();
      accounts = FilePersistence.loadAccounts(clients, bank);
      System.out.println(
          "Data loaded successfully: "
              + clients.size()
              + " clients, "
              + accounts.size()
              + " accounts");
    } catch (Exception e) {
      System.err.println("Error loading data: " + e.getMessage());

      // Show error in UI
      Platform.runLater(
          () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Loading Error");
            alert.setHeaderText("Error Loading Data");
            alert.setContentText("Could not load data from files: " + e.getMessage());
            alert.showAndWait();
          });
    }
  }

  /** Save data to text files */
  private void saveData() {
    try {
      FilePersistence.saveClients(clients);
      FilePersistence.saveAccounts(accounts);
      System.out.println("Data saved successfully");
    } catch (Exception e) {
      System.err.println("Error saving data: " + e.getMessage());

      // Show error in UI
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Data Saving Error");
      alert.setHeaderText("Error Saving Data");
      alert.setContentText("Could not save data to files: " + e.getMessage());
      alert.showAndWait();
    }
  }

  /** Main method to launch the application */
  public static void main(String[] args) {
    launch(args);
  }
}
