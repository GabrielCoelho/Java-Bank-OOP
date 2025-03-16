package br.com.devcoelho.ui;

import br.com.devcoelho.Bank;
import br.com.devcoelho.BankAccount;
import br.com.devcoelho.BankInvestmentAccount;
import br.com.devcoelho.BankSimpleAccount;
import br.com.devcoelho.Person;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Account Removal window
 */
public class AccountRemovalController {

    private Bank bank;
    private List<BankAccount> accounts;
    private TableView<AccountTableItem> accountTable;
    private Button removeButton;
    private Button cancelButton;
    private Text messageText;

    public AccountRemovalController(Bank bank, List<BankAccount> accounts) {
        this.bank = bank;
        this.accounts = accounts;
    }

    /**
     * Creates the account removal scene
     */
    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");

        // Create header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text headerText = new Text("Remove Account");
        headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        headerText.setStyle("-fx-fill: #2c3e50;");
        
        Text subHeaderText = new Text("Select an account to remove");
        subHeaderText.setFont(Font.font("Arial", 14));
        subHeaderText.setStyle("-fx-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(headerText, subHeaderText);
        mainLayout.setTop(headerBox);
        BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

        // Create account table
        accountTable = createAccountTable();
        
        // Add search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Search by account number, name or type...");
        searchField.setPrefHeight(30);
        searchField.setStyle("-fx-background-radius: 5;");
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTable(newValue);
        });
        
        // Warning message area
        messageText = new Text();
        messageText.setFont(Font.font("Arial", 12));
        messageText.setWrappingWidth(600);
        messageText.setVisible(false);
        
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(searchField, accountTable, messageText);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        tableContainer.setPadding(new Insets(15));
        
        mainLayout.setCenter(tableContainer);

        // Create buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        removeButton = new Button("Remove Account");
        removeButton.setDisable(true); // Initially disabled until an account is selected
        removeButton.setStyle(
            "-fx-background-color: #e74c3c;" +
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

        buttonBox.getChildren().addAll(cancelButton, removeButton);
        mainLayout.setBottom(buttonBox);

        // Set up event handlers
        setupEventHandlers();

        return new Scene(mainLayout, 700, 500);
    }

    /**
     * Create a table item class for accounts
     */
    public class AccountTableItem {
        private final BankAccount account;
        private final String accountNumber;
        private final String clientName;
        private final String accountType;
        private final String balance;
        private final String openingDate;

        public AccountTableItem(BankAccount account) {
            this.account = account;
            this.accountNumber = String.valueOf(account.getAccountNumber());
            this.clientName = account.getClient().getName();
            this.accountType = (account instanceof BankInvestmentAccount) ? 
                               "Investment Account" : "Simple Account";
            this.balance = String.format("$%.2f", account.getAmountStored());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            this.openingDate = dateFormat.format(account.getOpeningDate());
        }

        public BankAccount getAccount() {
            return account;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getClientName() {
            return clientName;
        }

        public String getAccountType() {
            return accountType;
        }

        public String getBalance() {
            return balance;
        }

        public String getOpeningDate() {
            return openingDate;
        }
    }

    /**
     * Creates the account table
     */
    private TableView<AccountTableItem> createAccountTable() {
        TableView<AccountTableItem> table = new TableView<>();
        table.setEditable(false);
        table.setStyle("-fx-font-size: 12px;");

        // Create columns
        TableColumn<AccountTableItem, String> accountNumberCol = new TableColumn<>("Account #");
        accountNumberCol.setMinWidth(80);
        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<AccountTableItem, String> clientNameCol = new TableColumn<>("Client Name");
        clientNameCol.setMinWidth(200);
        clientNameCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));

        TableColumn<AccountTableItem, String> accountTypeCol = new TableColumn<>("Account Type");
        accountTypeCol.setMinWidth(150);
        accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));

        TableColumn<AccountTableItem, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setMinWidth(100);
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        TableColumn<AccountTableItem, String> openingDateCol = new TableColumn<>("Opening Date");
        openingDateCol.setMinWidth(100);
        openingDateCol.setCellValueFactory(new PropertyValueFactory<>("openingDate"));

        // Add columns to table
        table.getColumns().addAll(accountNumberCol, clientNameCol, accountTypeCol, 
                                 balanceCol, openingDateCol);

        // Add data to table
        ObservableList<AccountTableItem> data = FXCollections.observableArrayList();
        for (BankAccount account : accounts) {
            data.add(new AccountTableItem(account));
        }
        table.setItems(data);

        return table;
    }

    /**
     * Sets up event handlers for the buttons and table
     */
    private void setupEventHandlers() {
        // Enable remove button when an account is selected and show warning if needed
        accountTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    removeButton.setDisable(false);
                    
                    // Show warning if account has balance
                    BankAccount selectedAccount = newSelection.getAccount();
                    double balance = selectedAccount.getAmountStored();
                    
                    if (balance > 0) {
                        messageText.setText("WARNING: This account has a balance of " + 
                                          newSelection.getBalance() + ". This amount will be lost if you " +
                                          "remove the account. Consider withdrawing or transferring " +
                                          "the funds before removal.");
                        messageText.setFill(Color.web("#e74c3c"));
                        messageText.setVisible(true);
                    } else if (selectedAccount instanceof BankInvestmentAccount) {
                        BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;
                        if (!investmentAccount.getInvestments().isEmpty()) {
                            messageText.setText("WARNING: This account has active investments. " +
                                              "All investments will be lost if you remove the account. " +
                                              "Consider liquidating investments before removal.");
                            messageText.setFill(Color.web("#e74c3c"));
                            messageText.setVisible(true);
                        } else {
                            messageText.setVisible(false);
                        }
                    } else {
                        messageText.setVisible(false);
                    }
                } else {
                    removeButton.setDisable(true);
                    messageText.setVisible(false);
                }
            }
        );

        // Double click to select
        accountTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && accountTable.getSelectionModel().getSelectedItem() != null) {
                removeAccount();
            }
        });

        // Remove button
        removeButton.setOnAction(e -> removeAccount());

        // Cancel button
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Removes the selected account after confirmation
     */
    private void removeAccount() {
        AccountTableItem selectedItem = accountTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        
        BankAccount selectedAccount = selectedItem.getAccount();
        double balance = selectedAccount.getAmountStored();
        
        // Build confirmation message
        String confirmMessage = "Are you sure you want to remove the following account?\n\n" +
                              "Account #: " + selectedAccount.getAccountNumber() + "\n" +
                              "Type: " + selectedItem.getAccountType() + "\n" +
                              "Client: " + selectedAccount.getClient().getName() + "\n" +
                              "Balance: " + selectedItem.getBalance() + "\n";
        
        if (balance > 0) {
            confirmMessage += "\nWARNING: This account has a positive balance that will be lost!";
        }
        
        if (selectedAccount instanceof BankInvestmentAccount) {
            BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;
            if (!investmentAccount.getInvestments().isEmpty()) {
                confirmMessage += "\nWARNING: Active investments will be lost!";
            }
        }
        
        confirmMessage += "\n\nThis action cannot be undone.";
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Account Removal");
        alert.setHeaderText("Account Removal Confirmation");
        alert.setContentText(confirmMessage);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove the account
            Person client = selectedAccount.getClient();
            
            // Remove account from list
            accounts.remove(selectedAccount);
            
            // Remove account from client's accounts
            List<BankAccount> clientAccounts = bank.getClientAccounts(client);
            List<BankAccount> updatedAccounts = new ArrayList<>();
            for (BankAccount acc : clientAccounts) {
                if (acc.getAccountNumber() != selectedAccount.getAccountNumber()) {
                    updatedAccounts.add(acc);
                }
            }
            
            // Update table view
            ObservableList<AccountTableItem> items = accountTable.getItems();
            items.removeIf(item -> item.getAccount().getAccountNumber() == 
                                selectedAccount.getAccountNumber());
            
            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Account Removed");
            successAlert.setHeaderText("Account Removed Successfully");
            successAlert.setContentText("Account #" + selectedAccount.getAccountNumber() + 
                                      " has been removed from the system.");
            successAlert.showAndWait();
            
            // Clear message text
            messageText.setVisible(false);
            
            // Disable remove button until next selection
            removeButton.setDisable(true);
        }
    }

    /**
     * Filters the table based on search text
     */
    private void filterTable(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            ObservableList<AccountTableItem> items = FXCollections.observableArrayList();
            for (BankAccount account : accounts) {
                items.add(new AccountTableItem(account));
            }
            accountTable.setItems(items);
            return;
        }
        
        String lowerCaseFilter = searchText.toLowerCase();
        
        ObservableList<AccountTableItem> filteredList = FXCollections.observableArrayList();
        for (BankAccount account : accounts) {
            AccountTableItem item = new AccountTableItem(account);
            
            boolean accountNumberMatch = item.getAccountNumber().contains(lowerCaseFilter);
            boolean clientNameMatch = item.getClientName().toLowerCase().contains(lowerCaseFilter);
            boolean accountTypeMatch = item.getAccountType().toLowerCase().contains(lowerCaseFilter);
            
            if (accountNumberMatch || clientNameMatch || accountTypeMatch) {
                filteredList.add(item);
            }
        }
        
        accountTable.setItems(filteredList);
    }
}
