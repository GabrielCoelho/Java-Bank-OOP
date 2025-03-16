package br.com.devcoelho.ui;

import br.com.devcoelho.*;
import br.com.devcoelho.exceptions.InsufficientBalanceException;
import br.com.devcoelho.exceptions.InvalidAccountException;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Controller for the Account Management window */
public class AccountManagementController {

  private List<BankAccount> accounts;
  private BankAccount selectedAccount;
  private TabPane operationsTabPane;
  private VBox accountInfoBox;
  private Text balanceText;
  private TableView<TransactionItem> transactionTable;
  private Pane chartPane;

  // Operation components
  private TextField depositAmountField;
  private TextField withdrawAmountField;
  private ComboBox<AccountListItem> transferAccountCombo;
  private TextField transferAmountField;
  private ListView<InvestmentItem> investmentsListView;
  private TextField investmentNameField;
  private TextField investmentAmountField;
  private TextField investmentRateField;
  private ComboBox<String> liquidateInvestmentCombo;

  public AccountManagementController(List<BankAccount> accounts) {
    this.accounts = accounts;
  }

  /** Creates the account management scene */
  public Scene createScene() {
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Create account selection area
    VBox selectionBox = createAccountSelectionBox();
    mainLayout.setTop(selectionBox);
    BorderPane.setMargin(selectionBox, new Insets(0, 0, 20, 0));

    // Create center content (account info + operations)
    HBox contentBox = new HBox(20);

    // Account info sidebar (left)
    accountInfoBox = createAccountInfoBox();

    // Operations panel (right)
    VBox operationsBox = createOperationsBox();

    contentBox.getChildren().addAll(accountInfoBox, operationsBox);
    HBox.setHgrow(operationsBox, Priority.ALWAYS);

    mainLayout.setCenter(contentBox);

    // Create bottom area (buttons)
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    Button closeButton = new Button("Close");
    closeButton.setStyle(
        "-fx-background-color: #95a5a6;"
            + "-fx-text-fill: white;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    closeButton.setOnAction(
        e -> {
          Stage stage = (Stage) closeButton.getScene().getWindow();
          stage.close();
        });

    buttonBox.getChildren().add(closeButton);
    mainLayout.setBottom(buttonBox);

    // Initial state: show account selection message
    showAccountSelectionMessage();

    return new Scene(mainLayout, 1000, 700);
  }

  /** Creates the account selection box */
  private VBox createAccountSelectionBox() {
    VBox box = new VBox(15);
    box.setAlignment(Pos.CENTER);

    // Header
    Text headerText = new Text("Account Management");
    headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    headerText.setStyle("-fx-fill: #2c3e50;");

    // Account selection dropdown
    HBox selectionRow = new HBox(15);
    selectionRow.setAlignment(Pos.CENTER);

    Label selectLabel = new Label("Select Account:");
    selectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    ComboBox<AccountListItem> accountCombo = new ComboBox<>();
    accountCombo.setPrefWidth(400);
    accountCombo.setPromptText("Choose an account to manage");

    // Populate the combo box
    ObservableList<AccountListItem> accountItems = FXCollections.observableArrayList();
    for (BankAccount account : accounts) {
      accountItems.add(new AccountListItem(account));
    }
    accountCombo.setItems(accountItems);

    // Set selection handler
    accountCombo.setOnAction(
        e -> {
          AccountListItem selectedItem = accountCombo.getValue();
          if (selectedItem != null) {
            selectedAccount = selectedItem.getAccount();
            updateAccountDisplay();
          }
        });

    selectionRow.getChildren().addAll(selectLabel, accountCombo);

    box.getChildren().addAll(headerText, selectionRow);

    return box;
  }

  /** Account list item for the combo box */
  private class AccountListItem {
    private final BankAccount account;

    public AccountListItem(BankAccount account) {
      this.account = account;
    }

    public BankAccount getAccount() {
      return account;
    }

    @Override
    public String toString() {
      String accountType =
          (account instanceof BankInvestmentAccount) ? "Investment Account" : "Simple Account";

      return String.format(
          "Account #%d - %s - %s - Balance: $%.2f",
          account.getAccountNumber(),
          account.getClient().getName(),
          accountType,
          account.getAmountStored());
    }
  }

  /** Creates the account info box */
  private VBox createAccountInfoBox() {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
    box.setPrefWidth(300);
    box.setMinWidth(280);

    return box;
  }

  /** Creates the operations box with tabs */
  private VBox createOperationsBox() {
    VBox box = new VBox(15);
    box.setPadding(new Insets(15));
    box.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Operations title
    Text operationsTitle = new Text("Account Operations");
    operationsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    // Create tab pane for different operations
    operationsTabPane = new TabPane();
    operationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    // Create tabs for operations
    Tab depositTab = new Tab("Deposit");
    depositTab.setContent(createDepositPanel());

    Tab withdrawTab = new Tab("Withdraw");
    withdrawTab.setContent(createWithdrawPanel());

    Tab transferTab = new Tab("Transfer");
    transferTab.setContent(createTransferPanel());

    Tab transactionsTab = new Tab("Transactions");
    transactionsTab.setContent(createTransactionsPanel());

    Tab balanceChartTab = new Tab("Balance Chart");
    chartPane = createBalanceChartPanel();
    balanceChartTab.setContent(chartPane);

    // Add tabs to tab pane
    operationsTabPane
        .getTabs()
        .addAll(depositTab, withdrawTab, transferTab, transactionsTab, balanceChartTab);

    // Add elements to box
    box.getChildren().addAll(operationsTitle, operationsTabPane);
    VBox.setVgrow(operationsTabPane, Priority.ALWAYS);

    return box;
  }

  /** Creates the deposit operation panel */
  private VBox createDepositPanel() {
    VBox panel = new VBox(20);
    panel.setPadding(new Insets(20));

    // Deposit title
    Text depositTitle = new Text("Make a Deposit");
    depositTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Deposit amount field
    Label amountLabel = new Label("Deposit Amount:");
    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox amountBox = new HBox(10);
    amountBox.setAlignment(Pos.CENTER_LEFT);

    Label dollarLabel = new Label("$");
    dollarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    depositAmountField = new TextField();
    depositAmountField.setPrefWidth(150);
    depositAmountField.setPromptText("Enter amount");

    // Force numeric input only
    depositAmountField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                depositAmountField.setText(oldValue);
              }
            });

    amountBox.getChildren().addAll(dollarLabel, depositAmountField);

    // Deposit button
    Button depositButton = new Button("Make Deposit");
    depositButton.setStyle(
        "-fx-background-color: #2ecc71;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 10 20;"
            + "-fx-background-radius: 5;");

    // Result message
    Text depositResult = new Text();
    depositResult.setFont(Font.font("Arial", 14));

    // Set action for deposit button
    depositButton.setOnAction(
        e -> {
          try {
            if (selectedAccount == null) {
              depositResult.setText("No account selected");
              depositResult.setFill(Color.RED);
              return;
            }

            double amount = Double.parseDouble(depositAmountField.getText());

            if (amount <= 0) {
              depositResult.setText("Please enter a positive amount");
              depositResult.setFill(Color.RED);
              return;
            }

            selectedAccount.depositAmount(amount);

            depositResult.setText(
                String.format(
                    "Successfully deposited $%.2f. New balance: $%.2f",
                    amount, selectedAccount.getAmountStored()));
            depositResult.setFill(Color.GREEN);

            // Update account info display
            updateAccountDisplay();

            // Clear input field
            depositAmountField.clear();

          } catch (NumberFormatException ex) {
            depositResult.setText("Please enter a valid amount");
            depositResult.setFill(Color.RED);
          } catch (Exception ex) {
            depositResult.setText("Error: " + ex.getMessage());
            depositResult.setFill(Color.RED);
          }
        });

    // Deposit info
    Text depositInfo =
        new Text(
            "Deposits increase your account balance immediately. "
                + "There are no fees for deposits.");
    depositInfo.setFont(Font.font("Arial", 12));
    depositInfo.setWrappingWidth(400);
    depositInfo.setStyle("-fx-fill: #7f8c8d;");

    // Add all elements to panel
    panel
        .getChildren()
        .addAll(
            depositTitle,
            amountLabel,
            amountBox,
            depositButton,
            depositResult,
            new Separator(),
            depositInfo);

    return panel;
  }

  /** Creates the withdraw operation panel */
  private VBox createWithdrawPanel() {
    VBox panel = new VBox(20);
    panel.setPadding(new Insets(20));

    // Withdraw title
    Text withdrawTitle = new Text("Make a Withdrawal");
    withdrawTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Withdraw amount field
    Label amountLabel = new Label("Withdrawal Amount:");
    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox amountBox = new HBox(10);
    amountBox.setAlignment(Pos.CENTER_LEFT);

    Label dollarLabel = new Label("$");
    dollarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    withdrawAmountField = new TextField();
    withdrawAmountField.setPrefWidth(150);
    withdrawAmountField.setPromptText("Enter amount");

    // Force numeric input only
    withdrawAmountField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                withdrawAmountField.setText(oldValue);
              }
            });

    amountBox.getChildren().addAll(dollarLabel, withdrawAmountField);

    // Available balance
    Text availableBalanceText = new Text();
    availableBalanceText.setFont(Font.font("Arial", 14));

    // Withdraw button
    Button withdrawButton = new Button("Make Withdrawal");
    withdrawButton.setStyle(
        "-fx-background-color: #e74c3c;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 10 20;"
            + "-fx-background-radius: 5;");

    // Result message
    Text withdrawResult = new Text();
    withdrawResult.setFont(Font.font("Arial", 14));

    // Set action for withdraw button
    withdrawButton.setOnAction(
        e -> {
          try {
            if (selectedAccount == null) {
              withdrawResult.setText("No account selected");
              withdrawResult.setFill(Color.RED);
              return;
            }

            double amount = Double.parseDouble(withdrawAmountField.getText());

            if (amount <= 0) {
              withdrawResult.setText("Please enter a positive amount");
              withdrawResult.setFill(Color.RED);
              return;
            }

            selectedAccount.withdrawAmount(amount);

            withdrawResult.setText(
                String.format(
                    "Successfully withdrew $%.2f. New balance: $%.2f",
                    amount, selectedAccount.getAmountStored()));
            withdrawResult.setFill(Color.GREEN);

            // Update account info display
            updateAccountDisplay();

            // Update available balance
            updateAvailableBalance(availableBalanceText);

            // Clear input field
            withdrawAmountField.clear();

          } catch (NumberFormatException ex) {
            withdrawResult.setText("Please enter a valid amount");
            withdrawResult.setFill(Color.RED);
          } catch (InsufficientBalanceException ex) {
            withdrawResult.setText("Error: Insufficient balance for withdrawal");
            withdrawResult.setFill(Color.RED);
          } catch (Exception ex) {
            withdrawResult.setText("Error: " + ex.getMessage());
            withdrawResult.setFill(Color.RED);
          }
        });

    // Withdraw info
    Text withdrawInfo =
        new Text(
            "Withdrawals decrease your account balance immediately. "
                + "You cannot withdraw more than your available balance.");
    withdrawInfo.setFont(Font.font("Arial", 12));
    withdrawInfo.setWrappingWidth(400);
    withdrawInfo.setStyle("-fx-fill: #7f8c8d;");

    // Add all elements to panel
    panel
        .getChildren()
        .addAll(
            withdrawTitle,
            amountLabel,
            amountBox,
            availableBalanceText,
            withdrawButton,
            withdrawResult,
            new Separator(),
            withdrawInfo);

    // Update available balance text
    updateAvailableBalance(availableBalanceText);

    return panel;
  }

  /** Creates the transfer operation panel */
  private VBox createTransferPanel() {
    VBox panel = new VBox(20);
    panel.setPadding(new Insets(20));

    // Transfer title
    Text transferTitle = new Text("Transfer Funds");
    transferTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Destination account field
    Label destAccountLabel = new Label("Destination Account:");
    destAccountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    transferAccountCombo = new ComboBox<>();
    transferAccountCombo.setPrefWidth(400);
    transferAccountCombo.setPromptText("Select destination account");

    // Transfer amount field
    Label amountLabel = new Label("Transfer Amount:");
    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox amountBox = new HBox(10);
    amountBox.setAlignment(Pos.CENTER_LEFT);

    Label dollarLabel = new Label("$");
    dollarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    transferAmountField = new TextField();
    transferAmountField.setPrefWidth(150);
    transferAmountField.setPromptText("Enter amount");

    // Force numeric input only
    transferAmountField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                transferAmountField.setText(oldValue);
              }
            });

    amountBox.getChildren().addAll(dollarLabel, transferAmountField);

    // Available balance
    Text availableBalanceText = new Text();
    availableBalanceText.setFont(Font.font("Arial", 14));

    // Transfer button
    Button transferButton = new Button("Make Transfer");
    transferButton.setStyle(
        "-fx-background-color: #3498db;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 10 20;"
            + "-fx-background-radius: 5;");

    // Result message
    Text transferResult = new Text();
    transferResult.setFont(Font.font("Arial", 14));

    // Set action for transfer button
    transferButton.setOnAction(
        e -> {
          try {
            if (selectedAccount == null) {
              transferResult.setText("No source account selected");
              transferResult.setFill(Color.RED);
              return;
            }

            AccountListItem destAccountItem = transferAccountCombo.getValue();
            if (destAccountItem == null) {
              transferResult.setText("Please select a destination account");
              transferResult.setFill(Color.RED);
              return;
            }

            BankAccount destAccount = destAccountItem.getAccount();

            if (selectedAccount.getAccountNumber() == destAccount.getAccountNumber()) {
              transferResult.setText("Cannot transfer to the same account");
              transferResult.setFill(Color.RED);
              return;
            }

            double amount = Double.parseDouble(transferAmountField.getText());

            if (amount <= 0) {
              transferResult.setText("Please enter a positive amount");
              transferResult.setFill(Color.RED);
              return;
            }

            selectedAccount.transferAmount(amount, destAccount);

            transferResult.setText(
                String.format(
                    "Successfully transferred $%.2f. New balance: $%.2f",
                    amount, selectedAccount.getAmountStored()));
            transferResult.setFill(Color.GREEN);

            // Update account info display
            updateAccountDisplay();

            // Update available balance
            updateAvailableBalance(availableBalanceText);

            // Clear input field
            transferAmountField.clear();

          } catch (NumberFormatException ex) {
            transferResult.setText("Please enter a valid amount");
            transferResult.setFill(Color.RED);
          } catch (InsufficientBalanceException ex) {
            transferResult.setText("Error: Insufficient balance for transfer");
            transferResult.setFill(Color.RED);
          } catch (InvalidAccountException ex) {
            transferResult.setText("Error: Invalid destination account");
            transferResult.setFill(Color.RED);
          } catch (Exception ex) {
            transferResult.setText("Error: " + ex.getMessage());
            transferResult.setFill(Color.RED);
          }
        });

    // Transfer info
    Text transferInfo =
        new Text(
            "Transfers move funds from your account to another account immediately. "
                + "You cannot transfer more than your available balance.");
    transferInfo.setFont(Font.font("Arial", 12));
    transferInfo.setWrappingWidth(400);
    transferInfo.setStyle("-fx-fill: #7f8c8d;");

    // Add all elements to panel
    panel
        .getChildren()
        .addAll(
            transferTitle,
            destAccountLabel,
            transferAccountCombo,
            amountLabel,
            amountBox,
            availableBalanceText,
            transferButton,
            transferResult,
            new Separator(),
            transferInfo);

    // Update available balance text
    updateAvailableBalance(availableBalanceText);

    return panel;
  }

  /** Creates the transactions panel */
  private Pane createTransactionsPanel() {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Transactions title
    Text transactionsTitle = new Text("Transaction History");
    transactionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Create transaction table
    transactionTable = new TableView<>();

    // Create columns
    TableColumn<TransactionItem, String> dateCol = new TableColumn<>("Date & Time");
    dateCol.setMinWidth(150);
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

    TableColumn<TransactionItem, String> typeCol = new TableColumn<>("Type");
    typeCol.setMinWidth(100);
    typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

    TableColumn<TransactionItem, String> amountCol = new TableColumn<>("Amount");
    amountCol.setMinWidth(100);
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

    TableColumn<TransactionItem, String> balanceCol = new TableColumn<>("Balance");
    balanceCol.setMinWidth(100);
    balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

    TableColumn<TransactionItem, String> descriptionCol = new TableColumn<>("Description");
    descriptionCol.setMinWidth(200);
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

    // Add columns to table
    transactionTable.getColumns().addAll(dateCol, typeCol, amountCol, balanceCol, descriptionCol);

    // Export transactions button
    Button exportButton = new Button("Export Transactions");
    exportButton.setStyle(
        "-fx-background-color: #34495e;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    exportButton.setOnAction(
        e -> {
          // In a real application, this would export to CSV or PDF
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Export Transactions");
          alert.setHeaderText("Export Feature");
          alert.setContentText(
              "In a production environment, this would export your transactions to a file.");
          alert.showAndWait();
        });

    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(exportButton);

    // Add all elements to panel
    panel.getChildren().addAll(transactionsTitle, transactionTable, buttonBox);
    VBox.setVgrow(transactionTable, Priority.ALWAYS);

    return panel;
  }

  /** Creates the balance chart panel */
  private Pane createBalanceChartPanel() {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Balance chart title
    Text chartTitle = new Text("Balance History Chart");
    chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Create chart axes
    NumberAxis xAxis = new NumberAxis();
    xAxis.setLabel("Transaction Number");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Balance ($)");

    // Create line chart
    LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Account Balance History");

    // No data message
    Text noDataText = new Text("Select an account to view balance history");
    noDataText.setFont(Font.font("Arial", 14));

    // Add elements to panel
    panel.getChildren().addAll(chartTitle, lineChart);
    VBox.setVgrow(lineChart, Priority.ALWAYS);

    return panel;
  }

  /** Creates the investment management panel */
  private Pane createInvestmentPanel() {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Panel title
    Text investmentTitle = new Text("Investment Management");
    investmentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Current investments section
    Text currentInvestmentsTitle = new Text("Current Investments");
    currentInvestmentsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    investmentsListView = new ListView<>();

    // Create investment section
    Text createInvestmentTitle = new Text("Create New Investment");
    createInvestmentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Investment name field
    Label nameLabel = new Label("Investment Name:");
    investmentNameField = new TextField();
    investmentNameField.setPromptText("E.g., Treasury Bonds, Stock Fund");

    // Investment amount field
    Label amountLabel = new Label("Investment Amount ($):");
    investmentAmountField = new TextField();
    investmentAmountField.setPromptText("Enter amount to invest");

    // Force numeric input only
    investmentAmountField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                investmentAmountField.setText(oldValue);
              }
            });

    // Investment rate field
    Label rateLabel = new Label("Annual Interest Rate (%):");
    investmentRateField = new TextField();
    investmentRateField.setPromptText("E.g., 5.5 for 5.5%");

    // Force numeric input only
    investmentRateField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                investmentRateField.setText(oldValue);
              }
            });

    // Create investment button
    Button createInvestmentButton = new Button("Create Investment");
    createInvestmentButton.setStyle(
        "-fx-background-color: #2ecc71;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    // Result message
    Text createResult = new Text();
    createResult.setFont(Font.font("Arial", 14));

    // Set action for create button
    createInvestmentButton.setOnAction(
        e -> {
          try {
            if (selectedAccount == null || !(selectedAccount instanceof BankInvestmentAccount)) {
              createResult.setText("No investment account selected");
              createResult.setFill(Color.RED);
              return;
            }

            BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;

            String name = investmentNameField.getText().trim();
            if (name.isEmpty()) {
              createResult.setText("Please enter an investment name");
              createResult.setFill(Color.RED);
              return;
            }

            double amount = Double.parseDouble(investmentAmountField.getText());
            double rate = Double.parseDouble(investmentRateField.getText()) / 100.0;

            if (amount <= 0) {
              createResult.setText("Please enter a positive amount");
              createResult.setFill(Color.RED);
              return;
            }

            if (rate <= 0) {
              createResult.setText("Please enter a positive interest rate");
              createResult.setFill(Color.RED);
              return;
            }

            investmentAccount.createInvestment(name, amount, rate);

            createResult.setText(
                String.format(
                    "Successfully created investment of $%.2f at %.2f%% annual rate",
                    amount, rate * 100));
            createResult.setFill(Color.GREEN);

            // Update account display
            updateAccountDisplay();

            // Update investments list
            updateInvestmentsList();

            // Clear input fields
            investmentNameField.clear();
            investmentAmountField.clear();
            investmentRateField.clear();

          } catch (NumberFormatException ex) {
            createResult.setText("Please enter valid numeric values");
            createResult.setFill(Color.RED);
          } catch (InsufficientBalanceException ex) {
            createResult.setText("Error: Insufficient balance for investment");
            createResult.setFill(Color.RED);
          } catch (Exception ex) {
            createResult.setText("Error: " + ex.getMessage());
            createResult.setFill(Color.RED);
          }
        });

    // Liquidate investment section
    Text liquidateTitle = new Text("Liquidate Investment");
    liquidateTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Investment selection
    Label selectInvestmentLabel = new Label("Select Investment:");
    liquidateInvestmentCombo = new ComboBox<>();
    liquidateInvestmentCombo.setPromptText("Select investment to liquidate");
    liquidateInvestmentCombo.setPrefWidth(300);

    // Liquidate button
    Button liquidateButton = new Button("Liquidate Investment");
    liquidateButton.setStyle(
        "-fx-background-color: #e74c3c;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    // Result message
    Text liquidateResult = new Text();
    liquidateResult.setFont(Font.font("Arial", 14));

    // Set action for liquidate button
    liquidateButton.setOnAction(
        e -> {
          try {
            if (selectedAccount == null || !(selectedAccount instanceof BankInvestmentAccount)) {
              liquidateResult.setText("No investment account selected");
              liquidateResult.setFill(Color.RED);
              return;
            }

            BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;

            String investmentName = liquidateInvestmentCombo.getValue();
            if (investmentName == null || investmentName.isEmpty()) {
              liquidateResult.setText("Please select an investment to liquidate");
              liquidateResult.setFill(Color.RED);
              return;
            }

            double amount = investmentAccount.liquidateInvestment(investmentName);

            liquidateResult.setText(
                String.format(
                    "Successfully liquidated investment for $%.2f. New balance: $%.2f",
                    amount, investmentAccount.getAmountStored()));
            liquidateResult.setFill(Color.GREEN);

            // Update account display
            updateAccountDisplay();

            // Update investments list and combo box
            updateInvestmentsList();

          } catch (Exception ex) {
            liquidateResult.setText("Error: " + ex.getMessage());
            liquidateResult.setFill(Color.RED);
          }
        });

    // Create layout for the different sections
    GridPane createGrid = new GridPane();
    createGrid.setHgap(10);
    createGrid.setVgap(10);
    createGrid.addRow(0, nameLabel, investmentNameField);
    createGrid.addRow(1, amountLabel, investmentAmountField);
    createGrid.addRow(2, rateLabel, investmentRateField);

    GridPane liquidateGrid = new GridPane();
    liquidateGrid.setHgap(10);
    liquidateGrid.setVgap(10);
    liquidateGrid.addRow(0, selectInvestmentLabel, liquidateInvestmentCombo);

    // Add all elements to panel with separators
    panel
        .getChildren()
        .addAll(
            investmentTitle,
            new Separator(),
            currentInvestmentsTitle,
            investmentsListView,
            new Separator(),
            createInvestmentTitle,
            createGrid,
            createInvestmentButton,
            createResult,
            new Separator(),
            liquidateTitle,
            liquidateGrid,
            liquidateButton,
            liquidateResult);

    VBox.setVgrow(investmentsListView, Priority.ALWAYS);

    return panel;
  }

  /** Transaction item for the transaction table */
  public static class TransactionItem {
    private final String date;
    private final String type;
    private final String amount;
    private final String balance;
    private final String description;

    public TransactionItem(Transaction transaction, double runningBalance) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      this.date = dateFormat.format(transaction.getDate());
      this.type = transaction.getType().getDescription();

      // Format amount with proper sign
      double value = transaction.getAmount();
      if (value >= 0) {
        this.amount = String.format("$%.2f", value);
      } else {
        this.amount = String.format("-$%.2f", Math.abs(value));
      }

      // Format running balance
      this.balance = String.format("$%.2f", runningBalance);

      // Create description based on transaction type
      StringBuilder desc = new StringBuilder();
      if (transaction.getType() == TransactionType.TRANSFER
          && transaction.getDestinationAccount() != null) {
        BankAccount destAccount = (BankAccount) transaction.getDestinationAccount();
        desc.append("Transfer to Account #").append(destAccount.getAccountNumber());
      } else if (transaction.getType() == TransactionType.DEPOSIT) {
        desc.append("Deposit");
      } else if (transaction.getType() == TransactionType.WITHDRAWAL) {
        desc.append("Withdrawal");
      } else if (transaction.getType() == TransactionType.INTEREST) {
        desc.append("Interest Applied");
      } else if (transaction.getType() == TransactionType.FEE) {
        desc.append("Account Fee");
      }

      this.description = desc.toString();
    }

    public String getDate() {
      return date;
    }

    public String getType() {
      return type;
    }

    public String getAmount() {
      return amount;
    }

    public String getBalance() {
      return balance;
    }

    public String getDescription() {
      return description;
    }
  }

  /** Investment item for the investments list */
  public static class InvestmentItem {
    private final String name;
    private final BankInvestmentAccount.Investment investment;

    public InvestmentItem(String name, BankInvestmentAccount.Investment investment) {
      this.name = name;
      this.investment = investment;
    }

    public String getName() {
      return name;
    }

    public BankInvestmentAccount.Investment getInvestment() {
      return investment;
    }

    @Override
    public String toString() {
      return String.format(
          "%s: $%.2f at %.2f%% - Current Value: $%.2f (Profit: $%.2f)",
          name,
          investment.getPrincipal(),
          investment.getAnnualRate() * 100,
          investment.getCurrentValue(),
          investment.getCurrentValue() - investment.getPrincipal());
    }
  }

  /** Shows account selection message when no account is selected */
  private void showAccountSelectionMessage() {
    accountInfoBox.getChildren().clear();
    accountInfoBox.setAlignment(Pos.CENTER);

    Text messageText = new Text("Select an account to manage");
    messageText.setFont(Font.font("Arial", 14));
    messageText.setStyle("-fx-fill: #7f8c8d;");

    accountInfoBox.getChildren().add(messageText);

    // Disable transaction table
    if (transactionTable != null) {
      transactionTable.setItems(FXCollections.observableArrayList());
    }

    // Clear chart
    if (chartPane instanceof VBox) {
      VBox chartVBox = (VBox) chartPane;
      for (int i = 0; i < chartVBox.getChildren().size(); i++) {
        if (chartVBox.getChildren().get(i) instanceof LineChart) {
          LineChart<Number, Number> chart =
              (LineChart<Number, Number>) chartVBox.getChildren().get(i);
          chart.getData().clear();
        }
      }
    }

    // Clear investment options
    if (transferAccountCombo != null) {
      transferAccountCombo.getItems().clear();
    }

    // Clear investment controls
    if (liquidateInvestmentCombo != null) {
      liquidateInvestmentCombo.getItems().clear();
    }

    if (investmentsListView != null) {
      investmentsListView.getItems().clear();
    }
  }

  /** Updates the account display with the selected account's information */
  private void updateAccountDisplay() {
    if (selectedAccount == null) {
      showAccountSelectionMessage();
      return;
    }

    accountInfoBox.getChildren().clear();
    accountInfoBox.setAlignment(Pos.TOP_LEFT);

    // Account header with icon
    HBox headerBox = new HBox(10);
    headerBox.setAlignment(Pos.CENTER_LEFT);

    // Account type icon
    Text iconText = new Text(selectedAccount instanceof BankInvestmentAccount ? "📈" : "💰");
    iconText.setFont(Font.font("Arial", 36));

    // Account details
    VBox detailsBox = new VBox(5);
    Text accountTypeText =
        new Text(
            selectedAccount instanceof BankInvestmentAccount
                ? "Investment Account"
                : "Simple Account");
    accountTypeText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    Text accountNumberText = new Text("Account #" + selectedAccount.getAccountNumber());
    accountNumberText.setFont(Font.font("Arial", 14));

    detailsBox.getChildren().addAll(accountTypeText, accountNumberText);

    headerBox.getChildren().addAll(iconText, detailsBox);

    // Client information
    VBox clientBox = new VBox(5);
    clientBox.setPadding(new Insets(10, 0, 10, 0));

    Text clientLabel = new Text("Account Holder");
    clientLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    clientLabel.setStyle("-fx-fill: #7f8c8d;");

    Text clientName = new Text(selectedAccount.getClient().getName());
    clientName.setFont(Font.font("Arial", 14));

    Text cpfLabel = new Text("CPF");
    cpfLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    cpfLabel.setStyle("-fx-fill: #7f8c8d;");

    Text cpf = new Text(selectedAccount.getClient().getCpf());
    cpf.setFont(Font.font("Arial", 14));

    clientBox.getChildren().addAll(clientLabel, clientName, cpfLabel, cpf);

    // Balance information
    VBox balanceBox = new VBox(5);
    balanceBox.setPadding(new Insets(10, 0, 10, 0));

    Text balanceLabel = new Text("Current Balance");
    balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    balanceLabel.setStyle("-fx-fill: #7f8c8d;");

    balanceText = new Text(String.format("$%.2f", selectedAccount.getAmountStored()));
    balanceText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    balanceText.setStyle("-fx-fill: #2c3e50;");

    balanceBox.getChildren().addAll(balanceLabel, balanceText);

    // Add investment info for investment accounts
    VBox investmentInfoBox = new VBox();

    if (selectedAccount instanceof BankInvestmentAccount) {
      BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;

      Text interestRateLabel = new Text("Base Interest Rate");
      interestRateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
      interestRateLabel.setStyle("-fx-fill: #7f8c8d;");

      Text interestRate =
          new Text(String.format("%.2f%% per year", investmentAccount.getInterestRate() * 100));
      interestRate.setFont(Font.font("Arial", 14));

      Text investmentCountLabel = new Text("Active Investments");
      investmentCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
      investmentCountLabel.setStyle("-fx-fill: #7f8c8d;");

      int investmentCount = investmentAccount.getInvestments().size();
      Text investmentCount_Text = new Text(String.valueOf(investmentCount));
      investmentCount_Text.setFont(Font.font("Arial", 14));

      investmentInfoBox
          .getChildren()
          .addAll(interestRateLabel, interestRate, investmentCountLabel, investmentCount_Text);
      investmentInfoBox.setPadding(new Insets(10, 0, 10, 0));

      // Add investment tab to operation tabs if it doesn't exist yet
      boolean hasInvestmentTab = false;
      for (Tab tab : operationsTabPane.getTabs()) {
        if (tab.getText().equals("Investments")) {
          hasInvestmentTab = true;
          break;
        }
      }

      if (!hasInvestmentTab) {
        Tab investmentsTab = new Tab("Investments");
        investmentsTab.setContent(createInvestmentPanel());
        operationsTabPane.getTabs().add(investmentsTab);
      }

      // Update investments list
      updateInvestmentsList();

    } else {
      // Remove investment tab if it exists
      operationsTabPane.getTabs().removeIf(tab -> tab.getText().equals("Investments"));
    }

    // Account creation date
    Text dateLabel = new Text("Opening Date");
    dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    dateLabel.setStyle("-fx-fill: #7f8c8d;");

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
    Text dateText = new Text(dateFormat.format(selectedAccount.getOpeningDate()));
    dateText.setFont(Font.font("Arial", 14));

    VBox dateBox = new VBox(5);
    dateBox.getChildren().addAll(dateLabel, dateText);
    dateBox.setPadding(new Insets(10, 0, 10, 0));

    // Add all elements to account info box
    accountInfoBox
        .getChildren()
        .addAll(headerBox, new Separator(), clientBox, new Separator(), balanceBox);

    if (!investmentInfoBox.getChildren().isEmpty()) {
      accountInfoBox.getChildren().addAll(new Separator(), investmentInfoBox);
    }

    accountInfoBox.getChildren().addAll(new Separator(), dateBox);

    // Update transaction table
    updateTransactionTable();

    // Update balance chart
    updateBalanceChart();

    // Update transfer account options
    updateTransferOptions();
  }

  /** Updates the transaction table with transactions from the selected account */
  private void updateTransactionTable() {
    if (selectedAccount == null || transactionTable == null) {
      return;
    }

    List<Transaction> transactions = selectedAccount.getTransactionHistory();

    if (transactions == null || transactions.isEmpty()) {
      transactionTable.setItems(FXCollections.observableArrayList());
      return;
    }

    // Sort transactions by date (most recent first)
    transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

    // Calculate running balance for each transaction
    double runningBalance = selectedAccount.getAmountStored();
    ObservableList<TransactionItem> tableItems = FXCollections.observableArrayList();

    for (Transaction transaction : transactions) {
      // For deposits and interest, subtract amount to get previous balance
      if (transaction.getAmount() > 0) {
        runningBalance -= transaction.getAmount();
      }
      // For withdrawals, fees, and transfers, add the absolute amount
      else {
        runningBalance += Math.abs(transaction.getAmount());
      }

      tableItems.add(new TransactionItem(transaction, runningBalance));
    }

    transactionTable.setItems(tableItems);
  }

  /** Updates the balance chart with transaction data */
  private void updateBalanceChart() {
    if (selectedAccount == null || chartPane == null) {
      return;
    }

    if (!(chartPane instanceof VBox)) {
      return;
    }

    VBox chartVBox = (VBox) chartPane;
    LineChart<Number, Number> lineChart = null;

    // Find the line chart in the chart pane
    for (int i = 0; i < chartVBox.getChildren().size(); i++) {
      if (chartVBox.getChildren().get(i) instanceof LineChart) {
        lineChart = (LineChart<Number, Number>) chartVBox.getChildren().get(i);
        break;
      }
    }

    if (lineChart == null) {
      return;
    }

    // Clear previous data
    lineChart.getData().clear();

    List<Transaction> transactions = selectedAccount.getTransactionHistory();

    if (transactions == null || transactions.isEmpty()) {
      return;
    }

    // Sort transactions by date (oldest first)
    transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

    // Create data series for balance over time
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Account Balance");

    double balance = 0;
    int index = 0;

    for (Transaction transaction : transactions) {
      balance += transaction.getAmount();
      series.getData().add(new XYChart.Data<>(index++, balance));
    }

    lineChart.getData().add(series);
  }

  /** Updates the transfer account options */
  private void updateTransferOptions() {
    if (selectedAccount == null || transferAccountCombo == null) {
      return;
    }

    // Clear previous options
    transferAccountCombo.getItems().clear();

    // Add all accounts except the selected one
    for (BankAccount account : accounts) {
      if (account.getAccountNumber() != selectedAccount.getAccountNumber()) {
        transferAccountCombo.getItems().add(new AccountListItem(account));
      }
    }
  }

  /** Updates the available balance text for withdraw/transfer */
  private void updateAvailableBalance(Text balanceText) {
    if (selectedAccount == null) {
      balanceText.setText("");
      return;
    }

    balanceText.setText(
        String.format("Available Balance: $%.2f", selectedAccount.getAmountStored()));
    balanceText.setStyle("-fx-fill: #2c3e50;");
  }

  /** Updates the investments list and combo box */
  private void updateInvestmentsList() {
    if (selectedAccount == null
        || !(selectedAccount instanceof BankInvestmentAccount)
        || investmentsListView == null
        || liquidateInvestmentCombo == null) {
      return;
    }

    BankInvestmentAccount investmentAccount = (BankInvestmentAccount) selectedAccount;
    Map<String, BankInvestmentAccount.Investment> investments = investmentAccount.getInvestments();

    // Clear previous items
    investmentsListView.getItems().clear();
    liquidateInvestmentCombo.getItems().clear();

    if (investments.isEmpty()) {
      investmentsListView.getItems().add(new InvestmentItem("No active investments", null));
      return;
    }

    // Add investments to list view
    for (Map.Entry<String, BankInvestmentAccount.Investment> entry : investments.entrySet()) {
      investmentsListView.getItems().add(new InvestmentItem(entry.getKey(), entry.getValue()));

      // Also add to liquidate combo
      liquidateInvestmentCombo.getItems().add(entry.getKey());
    }
  }
}
