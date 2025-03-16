package br.com.devcoelho.ui;

import br.com.devcoelho.Bank;
import br.com.devcoelho.BankAccount;
import br.com.devcoelho.Person;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Controller for the Client Selection window */
public class ClientSelectionController {

  private Bank bank;
  private List<Person> clients;
  private List<BankAccount> accounts;
  private TableView<Person> clientTable;
  private Button selectButton;
  private Button cancelButton;

  public ClientSelectionController(Bank bank, List<Person> clients, List<BankAccount> accounts) {
    this.bank = bank;
    this.clients = clients;
    this.accounts = accounts;
  }

  /** Creates the client selection scene */
  public Scene createScene() {
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Create header
    VBox headerBox = new VBox(10);
    headerBox.setAlignment(Pos.CENTER);
    Text headerText = new Text("Select Client");
    headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    headerText.setStyle("-fx-fill: #2c3e50;");

    Text subHeaderText = new Text("Select a client to create a new account");
    subHeaderText.setFont(Font.font("Arial", 14));
    subHeaderText.setStyle("-fx-fill: #7f8c8d;");

    headerBox.getChildren().addAll(headerText, subHeaderText);
    mainLayout.setTop(headerBox);
    BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

    // Create client table
    clientTable = createClientTable();

    // Add search functionality
    TextField searchField = new TextField();
    searchField.setPromptText("Search by name or CPF...");
    searchField.setPrefHeight(30);
    searchField.setStyle("-fx-background-radius: 5;");

    searchField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              filterTable(newValue);
            });

    VBox tableContainer = new VBox(10);
    tableContainer.getChildren().addAll(searchField, clientTable);
    tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
    tableContainer.setPadding(new Insets(15));

    mainLayout.setCenter(tableContainer);

    // Create buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    selectButton = new Button("Select Client");
    selectButton.setDisable(true); // Initially disabled until a client is selected
    selectButton.setStyle(
        "-fx-background-color: #2ecc71;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    cancelButton = new Button("Cancel");
    cancelButton.setStyle(
        "-fx-background-color: #95a5a6;"
            + "-fx-text-fill: white;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    buttonBox.getChildren().addAll(cancelButton, selectButton);
    mainLayout.setBottom(buttonBox);

    // Set up event handlers
    setupEventHandlers();

    return new Scene(mainLayout, 700, 500);
  }

  /** Creates the client table */
  private TableView<Person> createClientTable() {
    TableView<Person> table = new TableView<>();
    table.setEditable(false);
    table.setStyle("-fx-font-size: 12px;");

    // Create columns
    TableColumn<Person, String> nameCol = new TableColumn<>("Name");
    nameCol.setMinWidth(250);
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<Person, String> cpfCol = new TableColumn<>("CPF");
    cpfCol.setMinWidth(150);
    cpfCol.setCellValueFactory(new PropertyValueFactory<>("cpf"));

    TableColumn<Person, Integer> addressCountCol = new TableColumn<>("Addresses");
    addressCountCol.setMinWidth(100);
    addressCountCol.setCellValueFactory(
        param -> {
          Person client = param.getValue();
          int addressCount = client.getAddress().size(); // Get size directly
          return new javafx.beans.property.SimpleIntegerProperty(addressCount).asObject();
        });

    TableColumn<Person, Integer> accountCountCol = new TableColumn<>("Accounts");
    accountCountCol.setMinWidth(100);
    accountCountCol.setCellValueFactory(
        param -> {
          Person client = param.getValue();
          long count =
              accounts.stream()
                  .filter(acc -> acc.getClient().getCpf().equals(client.getCpf()))
                  .count();
          return new javafx.beans.property.SimpleIntegerProperty((int) count).asObject();
        });

    // Add columns to table
    table.getColumns().addAll(nameCol, cpfCol, addressCountCol, accountCountCol);

    // Add data to table
    ObservableList<Person> data = FXCollections.observableArrayList(clients);
    table.setItems(data);

    return table;
  }

  /** Sets up event handlers for the buttons and table */
  private void setupEventHandlers() {
    // Enable select button when a client is selected
    clientTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              selectButton.setDisable(newSelection == null);
            });

    // Double click to select
    clientTable.setOnMouseClicked(
        event -> {
          if (event.getClickCount() == 2
              && clientTable.getSelectionModel().getSelectedItem() != null) {
            selectClient();
          }
        });

    // Select button
    selectButton.setOnAction(e -> selectClient());

    // Cancel button
    cancelButton.setOnAction(
        e -> {
          Stage stage = (Stage) cancelButton.getScene().getWindow();
          stage.close();
        });
  }

  /** Handles the client selection */
  private void selectClient() {
    Person selectedClient = clientTable.getSelectionModel().getSelectedItem();
    if (selectedClient != null) {
      // Close this window
      Stage stage = (Stage) selectButton.getScene().getWindow();
      stage.close();

      // Open account type selection window
      openAccountTypeSelection(selectedClient);
    }
  }

  /** Filters the table based on search text */
  private void filterTable(String searchText) {
    if (searchText == null || searchText.isEmpty()) {
      clientTable.setItems(FXCollections.observableArrayList(clients));
      return;
    }

    String lowerCaseFilter = searchText.toLowerCase();

    ObservableList<Person> filteredList = FXCollections.observableArrayList();
    for (Person client : clients) {
      boolean nameMatch = client.getName().toLowerCase().contains(lowerCaseFilter);
      boolean cpfMatch = client.getCpf().toLowerCase().contains(lowerCaseFilter);

      if (nameMatch || cpfMatch) {
        filteredList.add(client);
      }
    }

    clientTable.setItems(filteredList);
  }

  /** Opens the account type selection window */
  private void openAccountTypeSelection(Person client) {
    Stage accountTypeStage = new Stage();
    accountTypeStage.setTitle("Select Account Type");

    AccountTypeController accountTypeController = new AccountTypeController(bank, accounts, client);
    Scene accountTypeScene = accountTypeController.createScene();

    accountTypeStage.setScene(accountTypeScene);
    accountTypeStage.show();
  }
}
