package br.com.devcoelho.ui;

import br.com.devcoelho.*;
import java.text.SimpleDateFormat;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Controller for the Account Information window */
public class AccountInfoController {

  private Bank bank;
  private List<BankAccount> accounts;
  private TableView<AccountTableItem> accountTable;
  private VBox detailsBox;
  private TabPane tabPane;

  public AccountInfoController(Bank bank, List<BankAccount> accounts) {
    this.bank = bank;
    this.accounts = accounts;
  }

  /** Creates the account information scene */
  public Scene createScene() {
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Create header
    VBox headerBox = new VBox(10);
    headerBox.setAlignment(Pos.CENTER);
    Text headerText = new Text("Account Information");
    headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    headerText.setStyle("-fx-fill: #2c3e50;");

    headerBox.getChildren().add(headerText);
    mainLayout.setTop(headerBox);
    BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

    // Create split pane for account selection and details
    SplitPane splitPane = new SplitPane();

    // Create account selection panel
    VBox accountSelectionBox = createAccountSelectionPanel();

    // Create account details panel
    detailsBox = new VBox(15);
    detailsBox.setPadding(new Insets(15));
    detailsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Initial message when no account is selected
    Text initialText = new Text("Select an account from the list to view its details");
    initialText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
    initialText.setStyle("-fx-fill: #7f8c8d;");
    detailsBox.getChildren().add(initialText);
    detailsBox.setAlignment(Pos.CENTER);

    // Add panels to split pane
    splitPane.getItems().addAll(accountSelectionBox, detailsBox);
    splitPane.setDividerPositions(0.35);

    mainLayout.setCenter(splitPane);

    // Create close button
    Button closeButton = new Button("Close");
    closeButton.setStyle(
        "-fx-background-color: #3498db;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    closeButton.setOnAction(
        e -> {
          Stage stage = (Stage) closeButton.getScene().getWindow();
          stage.close();
        });

    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(closeButton);

    BorderPane.setMargin(buttonBox, new Insets(20, 0, 0, 0));
    mainLayout.setBottom(buttonBox);

    return new Scene(mainLayout, 900, 600);
  }

  /** Creates the account selection panel */
  private VBox createAccountSelectionPanel() {
    VBox accountSelectionBox = new VBox(10);
    accountSelectionBox.setPadding(new Insets(15));
    accountSelectionBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Title
    Text selectionTitle = new Text("Select Account");
    selectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    // Search field
    TextField searchField = new TextField();
    searchField.setPromptText("Search by account # or client name");
    searchField.setPrefHeight(30);

    // Create account table
    accountTable = createAccountTable();

    // Add search functionality
    searchField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              filterTable(newValue);
            });

    accountSelectionBox.getChildren().addAll(selectionTitle, searchField, accountTable);

    return accountSelectionBox;
  }

  /** Table item class for accounts */
  public class AccountTableItem {
    private final BankAccount account;
    private final String accountNumber;
    private final String clientName;
    private final String accountType;
    private final String balance;

    public AccountTableItem(BankAccount account) {
      this.account = account;
      this.accountNumber = String.valueOf(account.getAccountNumber());
      this.clientName = account.getClient().getName();
      this.accountType = (account instanceof BankInvestmentAccount) ? "Investment" : "Simple";
      this.balance = String.format("$%.2f", account.getAmountStored());
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
  }

  /** Creates the account table */
  private TableView<AccountTableItem> createAccountTable() {
    TableView<AccountTableItem> table = new TableView<>();
    table.setEditable(false);

    // Create columns
    TableColumn<AccountTableItem, String> accountNumberCol = new TableColumn<>("Account #");
    accountNumberCol.setMinWidth(70);
    accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

    TableColumn<AccountTableItem, String> accountTypeCol = new TableColumn<>("Type");
    accountTypeCol.setMinWidth(80);
    accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));

    // Add the client name column
    TableColumn<AccountTableItem, String> clientNameCol = new TableColumn<>("Client");
    clientNameCol.setMinWidth(120); // Provide enough space for names
    clientNameCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));    // Add columns to table
                                                                                    //
    table.getColumns().addAll(accountNumberCol, clientNameCol, accountTypeCol);

    // Add data to table
    ObservableList<AccountTableItem> data = FXCollections.observableArrayList();
    for (BankAccount account : accounts) {
      data.add(new AccountTableItem(account));
    }
    table.setItems(data);

    // Set selection handler
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              if (newSelection != null) {
                showAccountDetails(newSelection.getAccount());
              }
            });

    return table;
  }

  /** Transaction table item class */
  public static class TransactionTableItem {
    private final String date;
    private final String type;
    private final String amount;
    private final String description;

    public TransactionTableItem(Transaction transaction) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      this.date = dateFormat.format(transaction.getDate());
      this.type = transaction.getType().getDescription();

      // Format amount with color hint by adding prefix
      double value = transaction.getAmount();
      if (value >= 0) {
        this.amount = String.format("$%.2f", value);
      } else {
        this.amount = String.format("-$%.2f", Math.abs(value));
      }

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

    public String getDescription() {
      return description;
    }
  }

  /** Investment table item class */
  public static class InvestmentTableItem {
    private final String name;
    private final String principal;
    private final String currentValue;
    private final String annualRate;
    private final String profit;

    public InvestmentTableItem(String name, BankInvestmentAccount.Investment investment) {
      this.name = name;
      this.principal = String.format("$%.2f", investment.getPrincipal());
      double current = investment.getCurrentValue();
      this.currentValue = String.format("$%.2f", current);
      this.annualRate = String.format("%.2f%%", investment.getAnnualRate() * 100);

      double profitValue = current - investment.getPrincipal();
      this.profit =
          String.format(
              "$%.2f (%.2f%%)", profitValue, (profitValue / investment.getPrincipal() * 100));
    }

    public String getName() {
      return name;
    }

    public String getPrincipal() {
      return principal;
    }

    public String getCurrentValue() {
      return currentValue;
    }

    public String getAnnualRate() {
      return annualRate;
    }

    public String getProfit() {
      return profit;
    }
  }

  /** Shows account details in the details panel */
  private void showAccountDetails(BankAccount account) {
    detailsBox.getChildren().clear();
    detailsBox.setAlignment(Pos.TOP_LEFT);

    // Account Header
    HBox headerBox = new HBox(10);
    headerBox.setAlignment(Pos.CENTER_LEFT);

    // Account type icon
    Text iconText = new Text(account instanceof BankInvestmentAccount ? "📈" : "💰");
    iconText.setFont(Font.font("Arial", 36));

    // Account title and number
    VBox titleBox = new VBox(5);
    Text accountTypeText =
        new Text(
            account instanceof BankInvestmentAccount ? "Investment Account" : "Simple Account");
    accountTypeText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    Text accountNumberText = new Text("Account #" + account.getAccountNumber());
    accountNumberText.setFont(Font.font("Arial", 14));

    titleBox.getChildren().addAll(accountTypeText, accountNumberText);

    // Balance
    VBox balanceBox = new VBox(5);
    balanceBox.setAlignment(Pos.CENTER_RIGHT);
    Text balanceLabel = new Text("Current Balance");
    balanceLabel.setFont(Font.font("Arial", 12));

    Text balanceValue = new Text(String.format("$%.2f", account.getAmountStored()));
    balanceValue.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    balanceValue.setStyle("-fx-fill: #2c3e50;");

    balanceBox.getChildren().addAll(balanceLabel, balanceValue);

    // Add elements to header
    headerBox.getChildren().addAll(iconText, titleBox);
    HBox.setHgrow(titleBox, Priority.ALWAYS);
    headerBox.getChildren().add(balanceBox);

    // Tab pane for different information sections
    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    // General Info Tab
    Tab generalTab = new Tab("General Info");
    generalTab.setContent(createGeneralInfoPanel(account));

    // Client Info Tab
    Tab clientTab = new Tab("Client Info");
    clientTab.setContent(createClientInfoPanel(account.getClient()));

    // Transaction History Tab
    Tab transactionsTab = new Tab("Transactions");
    transactionsTab.setContent(createTransactionsPanel(account));

    // Add tabs to tab pane
    tabPane.getTabs().addAll(generalTab, clientTab, transactionsTab);

    // Add investment tab for investment accounts
    if (account instanceof BankInvestmentAccount) {
      Tab investmentsTab = new Tab("Investments");
      investmentsTab.setContent(createInvestmentsPanel((BankInvestmentAccount) account));
      tabPane.getTabs().add(investmentsTab);
    }

    // Add elements to details box
    detailsBox.getChildren().addAll(headerBox, new Separator(), tabPane);
    VBox.setVgrow(tabPane, Priority.ALWAYS);
  }

  /** Creates the general information panel */
  private VBox createGeneralInfoPanel(BankAccount account) {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");

    // Create grid for account details
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(10);

    // Create labels
    Label accountNumberLabel = new Label("Account Number:");
    Label agencyLabel = new Label("Agency:");
    Label openingDateLabel = new Label("Opening Date:");
    Label accountTypeLabel = new Label("Account Type:");

    // Set label styles
    List<Label> labels =
        List.of(accountNumberLabel, agencyLabel, openingDateLabel, accountTypeLabel);
    for (Label label : labels) {
      label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    }

    // Create value texts
    Text accountNumberValue = new Text(String.valueOf(account.getAccountNumber()));
    Text agencyValue = new Text(account.getAgency());
    Text openingDateValue = new Text(dateFormat.format(account.getOpeningDate()));
    Text accountTypeValue =
        new Text(
            account instanceof BankInvestmentAccount ? "Investment Account" : "Simple Account");

    // Add investment-specific details
    if (account instanceof BankInvestmentAccount) {
      BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;

      Label interestRateLabel = new Label("Annual Interest Rate:");
      interestRateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

      Text interestRateValue =
          new Text(String.format("%.2f%%", investmentAccount.getInterestRate() * 100));

      grid.addRow(4, interestRateLabel, interestRateValue);
    }

    // Add rows to grid
    grid.addRow(0, accountNumberLabel, accountNumberValue);
    grid.addRow(1, agencyLabel, agencyValue);
    grid.addRow(2, openingDateLabel, openingDateValue);
    grid.addRow(3, accountTypeLabel, accountTypeValue);

    panel.getChildren().add(grid);

    return panel;
  }

  /** Creates the client information panel */
  private VBox createClientInfoPanel(Person client) {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Client header
    Text clientTitle = new Text("Account Holder Information");
    clientTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Create grid for client details
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(10);

    // Create labels
    Label nameLabel = new Label("Name:");
    Label cpfLabel = new Label("CPF:");
    Label addressCountLabel = new Label("Registered Addresses:");

    // Set label styles
    List<Label> labels = List.of(nameLabel, cpfLabel, addressCountLabel);
    for (Label label : labels) {
      label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    }

    // Create value texts
    Text nameValue = new Text(client.getName());
    Text cpfValue = new Text(client.getCpf());
    Text addressCountValue = new Text(String.valueOf(client.getAddress().size()));

    // Add rows to grid
    grid.addRow(0, nameLabel, nameValue);
    grid.addRow(1, cpfLabel, cpfValue);
    grid.addRow(2, addressCountLabel, addressCountValue);

    // Addresses section
    Text addressesTitle = new Text("Addresses");
    addressesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    VBox addressesBox = new VBox(10);

    List<Address> addresses = client.getAddress();
    if (addresses.isEmpty()) {
      Text noAddressText = new Text("No addresses registered");
      noAddressText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
      addressesBox.getChildren().add(noAddressText);
    } else {
      for (int i = 0; i < addresses.size(); i++) {
        Address address = addresses.get(i);

        VBox addressBox = new VBox(5);
        addressBox.setPadding(new Insets(10));
        addressBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        Text addressTitle = new Text("Address " + (i + 1));
        addressTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Text addressText = new Text(address.getFormattedAddress());
        addressText.setWrappingWidth(400);

        addressBox.getChildren().addAll(addressTitle, addressText);
        addressesBox.getChildren().add(addressBox);
      }
    }

    // Add all elements to panel
    panel.getChildren().addAll(clientTitle, grid, new Separator(), addressesTitle, addressesBox);

    return panel;
  }

  /** Creates the transactions panel */
  private VBox createTransactionsPanel(BankAccount account) {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Transactions header
    Text transactionsTitle = new Text("Transaction History");
    transactionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Create transaction table
    TableView<TransactionTableItem> transactionTable = new TableView<>();

    // Create columns
    TableColumn<TransactionTableItem, String> dateCol = new TableColumn<>("Date & Time");
    dateCol.setMinWidth(150);
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

    TableColumn<TransactionTableItem, String> typeCol = new TableColumn<>("Type");
    typeCol.setMinWidth(120);
    typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

    TableColumn<TransactionTableItem, String> amountCol = new TableColumn<>("Amount");
    amountCol.setMinWidth(100);
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

    TableColumn<TransactionTableItem, String> descriptionCol = new TableColumn<>("Description");
    descriptionCol.setMinWidth(200);
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

    // Add columns to table
    transactionTable.getColumns().addAll(dateCol, typeCol, amountCol, descriptionCol);

    // Add data to table
    ObservableList<TransactionTableItem> data = FXCollections.observableArrayList();
    List<Transaction> transactions = account.getTransactionHistory();

    if (transactions != null) {
      for (Transaction transaction : transactions) {
        data.add(new TransactionTableItem(transaction));
      }
    }

    transactionTable.setItems(data);

    // Add elements to panel
    panel.getChildren().addAll(transactionsTitle, transactionTable);
    VBox.setVgrow(transactionTable, Priority.ALWAYS);

    return panel;
  }

  /** Creates the investments panel */
  private VBox createInvestmentsPanel(BankInvestmentAccount account) {
    VBox panel = new VBox(15);
    panel.setPadding(new Insets(15));

    // Investments header
    Text investmentsTitle = new Text("Active Investments");
    investmentsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Interest rate info
    Text interestRateText =
        new Text(
            String.format("Base Annual Interest Rate: %.2f%%", account.getInterestRate() * 100));
    interestRateText.setFont(Font.font("Arial", 12));

    // Create investments table
    TableView<InvestmentTableItem> investmentTable = new TableView<>();

    // Create columns
    TableColumn<InvestmentTableItem, String> nameCol = new TableColumn<>("Investment Name");
    nameCol.setMinWidth(150);
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<InvestmentTableItem, String> principalCol = new TableColumn<>("Principal");
    principalCol.setMinWidth(100);
    principalCol.setCellValueFactory(new PropertyValueFactory<>("principal"));

    TableColumn<InvestmentTableItem, String> currentValueCol = new TableColumn<>("Current Value");
    currentValueCol.setMinWidth(100);
    currentValueCol.setCellValueFactory(new PropertyValueFactory<>("currentValue"));

    TableColumn<InvestmentTableItem, String> rateCol = new TableColumn<>("Annual Rate");
    rateCol.setMinWidth(100);
    rateCol.setCellValueFactory(new PropertyValueFactory<>("annualRate"));

    TableColumn<InvestmentTableItem, String> profitCol = new TableColumn<>("Profit/Loss");
    profitCol.setMinWidth(150);
    profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));

    // Add columns to table
    investmentTable.getColumns().addAll(nameCol, principalCol, currentValueCol, rateCol, profitCol);

    // Add data to table
    ObservableList<InvestmentTableItem> data = FXCollections.observableArrayList();
    Map<String, BankInvestmentAccount.Investment> investments = account.getInvestments();

    if (investments != null && !investments.isEmpty()) {
      for (Map.Entry<String, BankInvestmentAccount.Investment> entry : investments.entrySet()) {
        data.add(new InvestmentTableItem(entry.getKey(), entry.getValue()));
      }
      investmentTable.setItems(data);
    } else {
      Text noInvestmentsText = new Text("No active investments");
      noInvestmentsText.setFont(Font.font("Arial", 12));
      panel.getChildren().add(noInvestmentsText);
    }

    // Add elements to panel
    panel.getChildren().addAll(investmentsTitle, interestRateText, investmentTable);
    VBox.setVgrow(investmentTable, Priority.ALWAYS);

    return panel;
  }

  /** Filters the account table based on search text */
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
