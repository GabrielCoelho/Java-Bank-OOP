package br.com.devcoelho.ui;

import br.com.devcoelho.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Controller for the Time Simulation window */
public class TimeSimulationController {

  private List<BankAccount> accounts;
  private TableView<AccountChangeItem> resultsTable;
  private ToggleGroup timeGroup;
  private RadioButton monthRadio;
  private RadioButton yearRadio;
  private TextField feeField;
  private Button simulateButton;
  private Button closeButton;
  private VBox resultsBox;

  public TimeSimulationController(List<BankAccount> accounts) {
    this.accounts = accounts;
  }

  /** Creates the time simulation scene */
  public Scene createScene() {
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Create header
    VBox headerBox = new VBox(10);
    headerBox.setAlignment(Pos.CENTER);
    Text headerText = new Text("Time Passage Simulation");
    headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    headerText.setStyle("-fx-fill: #2c3e50;");

    Text subheaderText = new Text("Simulate interest and fees over time");
    subheaderText.setFont(Font.font("Arial", 14));
    subheaderText.setStyle("-fx-fill: #7f8c8d;");

    headerBox.getChildren().addAll(headerText, subheaderText);
    mainLayout.setTop(headerBox);
    BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

    // Create settings panel
    VBox settingsBox = createSettingsPanel();
    settingsBox.setMaxWidth(350);
    settingsBox.setMinWidth(300);

    // Create results panel (initially empty)
    resultsBox = new VBox(15);
    resultsBox.setPadding(new Insets(15));
    resultsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    Text initialText = new Text("Set simulation parameters and click 'Simulate' to see results");
    initialText.setFont(Font.font("Arial", 14));
    initialText.setStyle("-fx-fill: #7f8c8d;");
    initialText.setWrappingWidth(400);

    resultsBox.getChildren().add(initialText);
    resultsBox.setAlignment(Pos.CENTER);

    // Create layout with settings and results panels
    HBox contentBox = new HBox(20);
    contentBox.getChildren().addAll(settingsBox, resultsBox);
    HBox.setHgrow(resultsBox, Priority.ALWAYS);

    mainLayout.setCenter(contentBox);

    // Create buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    simulateButton = new Button("Simulate");
    simulateButton.setStyle(
        "-fx-background-color: #9b59b6;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 20;"
            + "-fx-background-radius: 5;");

    closeButton = new Button("Close");
    closeButton.setStyle(
        "-fx-background-color: #95a5a6;"
            + "-fx-text-fill: white;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    buttonBox.getChildren().addAll(closeButton, simulateButton);
    mainLayout.setBottom(buttonBox);

    // Set up event handlers
    setupEventHandlers();

    return new Scene(mainLayout, 800, 550);
  }

  /** Creates the simulation settings panel */
  private VBox createSettingsPanel() {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));
    panel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Title
    Text settingsTitle = new Text("Simulation Settings");
    settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Time period selection
    Text periodLabel = new Text("Time Period to Simulate:");
    periodLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    timeGroup = new ToggleGroup();

    monthRadio = new RadioButton("One Month");
    monthRadio.setToggleGroup(timeGroup);
    monthRadio.setSelected(true);

    yearRadio = new RadioButton("One Year (12 months)");
    yearRadio.setToggleGroup(timeGroup);

    VBox timeBox = new VBox(10);
    timeBox.getChildren().addAll(periodLabel, monthRadio, yearRadio);

    // Monthly fee for simple accounts
    Text feeLabel = new Text("Monthly Maintenance Fee:");
    feeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox feeBox = new HBox(10);
    feeBox.setAlignment(Pos.CENTER_LEFT);

    Label dollarLabel = new Label("$");
    dollarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    feeField = new TextField("2.00");
    feeField.setPrefWidth(100);

    // Force numeric input only
    feeField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                feeField.setText(oldValue);
              }
            });

    feeBox.getChildren().addAll(dollarLabel, feeField);

    // Affected accounts section
    Text accountsLabel = new Text("Accounts Affected:");
    accountsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text simpleAccountsText = new Text(countAccountsByType(false) + " Simple Accounts");
    simpleAccountsText.setFont(Font.font("Arial", 12));

    Text investmentAccountsText = new Text(countAccountsByType(true) + " Investment Accounts");
    investmentAccountsText.setFont(Font.font("Arial", 12));

    VBox accountsBox = new VBox(5);
    accountsBox.getChildren().addAll(accountsLabel, simpleAccountsText, investmentAccountsText);

    // Information text
    Text infoTitle = new Text("What Will Happen:");
    infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text infoText =
        new Text(
            "• Simple accounts will be charged the monthly maintenance fee\n"
                + "• Investment accounts will earn interest at their base rate\n"
                + "• Individual investments will grow according to their rates\n"
                + "• Account balances will be updated accordingly");
    infoText.setFont(Font.font("Arial", 12));
    infoText.setWrappingWidth(280);

    // Add all sections to panel
    panel
        .getChildren()
        .addAll(
            settingsTitle,
            new Separator(),
            timeBox,
            new Separator(),
            feeLabel,
            feeBox,
            new Separator(),
            accountsBox,
            new Separator(),
            infoTitle,
            infoText);

    return panel;
  }

  /** Sets up event handlers for the buttons */
  private void setupEventHandlers() {
    // Simulate button
    simulateButton.setOnAction(e -> simulateTimePassage());

    // Close button
    closeButton.setOnAction(
        e -> {
          Stage stage = (Stage) closeButton.getScene().getWindow();
          stage.close();
        });
  }

  /** Table item for account changes */
  public static class AccountChangeItem {
    private final String accountNumber;
    private final String clientName;
    private final String accountType;
    private final String previousBalance;
    private final String currentBalance;
    private final String change;
    private final String notes;

    public AccountChangeItem(
        BankAccount account, double oldBalance, double newBalance, String notes) {
      this.accountNumber = String.valueOf(account.getAccountNumber());
      this.clientName = account.getClient().getName();
      this.accountType = (account instanceof BankInvestmentAccount) ? "Investment" : "Simple";
      this.previousBalance = String.format("$%.2f", oldBalance);
      this.currentBalance = String.format("$%.2f", newBalance);

      double changeAmount = newBalance - oldBalance;
      String changePrefix = changeAmount >= 0 ? "+" : "";
      this.change = String.format("%s$%.2f", changePrefix, changeAmount);

      this.notes = notes;
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

    public String getPreviousBalance() {
      return previousBalance;
    }

    public String getCurrentBalance() {
      return currentBalance;
    }

    public String getChange() {
      return change;
    }

    public String getNotes() {
      return notes;
    }
  }

  /** Simulates time passage based on selected parameters */
  private void simulateTimePassage() {
    // Get parameters
    int months = monthRadio.isSelected() ? 1 : 12;

    double monthlyFee = 0.0;
    try {
      monthlyFee = Double.parseDouble(feeField.getText());
    } catch (NumberFormatException e) {
      showError("Please enter a valid amount for the monthly fee.");
      return;
    }

    // Store original balances
    Map<Integer, Double> originalBalances = new java.util.HashMap<>();
    for (BankAccount account : accounts) {
      originalBalances.put(account.getAccountNumber(), account.getAmountStored());
    }

    // Create lists for tracking results
    List<String> feeWarnings = new ArrayList<>();
    List<String> investmentNotes = new ArrayList<>();

    // Apply time simulation to all accounts
    int simpleAccountsUpdated = 0;
    int investmentAccountsUpdated = 0;

    for (BankAccount account : accounts) {
      if (account instanceof BankInvestmentAccount) {
        BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;

        // Apply interest for each month
        for (int i = 0; i < months; i++) {
          investmentAccount.applyMonthlyInterest();
        }

        // Get count of investments
        int investmentCount = investmentAccount.getInvestments().size();
        if (investmentCount > 0) {
          investmentNotes.add(
              String.format(
                  "Account #%d: %d investment(s) updated",
                  account.getAccountNumber(), investmentCount));
        }

        // Advance time for investments
        try {
          // Use reflection to call simulateInvestmentTimePassage
          java.lang.reflect.Method method =
              BankInvestmentAccount.class.getDeclaredMethod(
                  "simulateInvestmentTimePassage", int.class);
          method.setAccessible(true);
          method.invoke(investmentAccount, months);
        } catch (Exception e) {
          System.out.println(
              "Warning: Could not update investments for account #"
                  + account.getAccountNumber()
                  + ": "
                  + e.getMessage());
        }

        investmentAccountsUpdated++;
      } else if (account instanceof BankSimpleAccount) {
        // Apply monthly fee to simple accounts
        BankSimpleAccount simpleAccount = (BankSimpleAccount) account;

        for (int i = 0; i < months; i++) {
          try {
            // Check if account has sufficient funds
            if (simpleAccount.getAmountStored() >= monthlyFee) {
              simpleAccount.withdrawAmount(monthlyFee);
              // Record as a fee transaction
              simpleAccount.recordTransaction(TransactionType.FEE, -monthlyFee, null);
            } else {
              feeWarnings.add(
                  String.format(
                      "Account #%d: Insufficient funds for fee on month %d",
                      simpleAccount.getAccountNumber(), i + 1));
              break; // Stop charging this account
            }
          } catch (Exception e) {
            feeWarnings.add(
                String.format(
                    "Account #%d: Error applying fee - %s",
                    simpleAccount.getAccountNumber(), e.getMessage()));
          }
        }

        simpleAccountsUpdated++;
      }
    }

    // Display results
    showResults(
        originalBalances,
        months,
        monthlyFee,
        simpleAccountsUpdated,
        investmentAccountsUpdated,
        feeWarnings,
        investmentNotes);
  }

  /** Shows the simulation results */
  private void showResults(
      Map<Integer, Double> originalBalances,
      int months,
      double monthlyFee,
      int simpleAccountsUpdated,
      int investmentAccountsUpdated,
      List<String> feeWarnings,
      List<String> investmentNotes) {

    // Clear previous results
    resultsBox.getChildren().clear();
    resultsBox.setAlignment(Pos.TOP_LEFT);

    // Results header
    Text resultsTitle = new Text("Simulation Results");
    resultsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    // Summary text
    Text summaryText =
        new Text(
            String.format(
                "Simulated Period: %d %s\n"
                    + "Simple Accounts Updated: %d (Fee: $%.2f per month, Total: $%.2f)\n"
                    + "Investment Accounts Updated: %d",
                months,
                months == 1 ? "month" : "months",
                simpleAccountsUpdated,
                monthlyFee,
                monthlyFee * months,
                investmentAccountsUpdated));
    summaryText.setFont(Font.font("Arial", 12));

    // Create results table
    resultsTable = new TableView<>();

    // Create columns
    TableColumn<AccountChangeItem, String> accountNumberCol = new TableColumn<>("Account #");
    accountNumberCol.setMinWidth(70);
    accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

    TableColumn<AccountChangeItem, String> clientNameCol = new TableColumn<>("Client");
    clientNameCol.setMinWidth(120);
    clientNameCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));

    TableColumn<AccountChangeItem, String> accountTypeCol = new TableColumn<>("Type");
    accountTypeCol.setMinWidth(80);
    accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));

    TableColumn<AccountChangeItem, String> previousBalanceCol =
        new TableColumn<>("Previous Balance");
    previousBalanceCol.setMinWidth(100);
    previousBalanceCol.setCellValueFactory(new PropertyValueFactory<>("previousBalance"));

    TableColumn<AccountChangeItem, String> currentBalanceCol = new TableColumn<>("Current Balance");
    currentBalanceCol.setMinWidth(100);
    currentBalanceCol.setCellValueFactory(new PropertyValueFactory<>("currentBalance"));

    TableColumn<AccountChangeItem, String> changeCol = new TableColumn<>("Change");
    changeCol.setMinWidth(80);
    changeCol.setCellValueFactory(new PropertyValueFactory<>("change"));

    TableColumn<AccountChangeItem, String> notesCol = new TableColumn<>("Notes");
    notesCol.setMinWidth(150);
    notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

    // Add columns to table
    resultsTable
        .getColumns()
        .addAll(
            accountNumberCol,
            clientNameCol,
            accountTypeCol,
            previousBalanceCol,
            currentBalanceCol,
            changeCol,
            notesCol);

    // Create table data
    ObservableList<AccountChangeItem> data = FXCollections.observableArrayList();

    for (BankAccount account : accounts) {
      // Get original balance
      double originalBalance = originalBalances.getOrDefault(account.getAccountNumber(), 0.0);

      // Get current balance
      double currentBalance = account.getAmountStored();

      // Create notes
      String notes = "";

      if (account instanceof BankInvestmentAccount) {
        BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;
        int investmentCount = investmentAccount.getInvestments().size();

        if (investmentCount > 0) {
          notes = String.format("%d investment(s) updated", investmentCount);
        } else {
          notes = "Base interest applied";
        }
      } else {
        if (currentBalance < originalBalance) {
          notes = String.format("Monthly fee: $%.2f", monthlyFee);
        } else {
          notes = "No fees applied";
        }

        // Check for warnings
        for (String warning : feeWarnings) {
          if (warning.contains("Account #" + account.getAccountNumber())) {
            notes = "WARNING: " + warning.split(": ")[1];
            break;
          }
        }
      }

      data.add(new AccountChangeItem(account, originalBalance, currentBalance, notes));
    }

    resultsTable.setItems(data);

    // Add warning section if needed
    VBox warningsBox = new VBox(5);

    if (!feeWarnings.isEmpty()) {
      Text warningsTitle = new Text("Warnings:");
      warningsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
      warningsTitle.setFill(Color.RED);

      warningsBox.getChildren().add(warningsTitle);

      for (String warning : feeWarnings) {
        Text warningText = new Text("• " + warning);
        warningText.setFill(Color.RED);
        warningsBox.getChildren().add(warningText);
      }
    }

    // Add all elements to results box
    resultsBox.getChildren().addAll(resultsTitle, summaryText, new Separator(), resultsTable);

    if (!warningsBox.getChildren().isEmpty()) {
      resultsBox.getChildren().addAll(new Separator(), warningsBox);
    }

    VBox.setVgrow(resultsTable, Priority.ALWAYS);
  }

  /** Shows an error dialog */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Counts accounts by type
   *
   * @param investment true to count investment accounts, false for simple accounts
   * @return the count
   */
  private int countAccountsByType(boolean investment) {
    int count = 0;
    for (BankAccount account : accounts) {
      if ((account instanceof BankInvestmentAccount) == investment) {
        count++;
      }
    }
    return count;
  }
}
