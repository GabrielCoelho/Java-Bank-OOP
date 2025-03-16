package br.com.devcoelho.ui;

import br.com.devcoelho.Bank;
import br.com.devcoelho.BankAccount;
import br.com.devcoelho.Person;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/** Controller for the Account Type Selection window */
public class AccountTypeController {

  private Bank bank;
  private List<BankAccount> accounts;
  private Person client;

  public AccountTypeController(Bank bank, List<BankAccount> accounts, Person client) {
    this.bank = bank;
    this.accounts = accounts;
    this.client = client;
  }

  /** Creates the account type selection scene */
  public Scene createScene() {
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));
    mainLayout.setStyle("-fx-background-color: #f0f0f0;");

    // Create header
    VBox headerBox = new VBox(10);
    headerBox.setAlignment(Pos.CENTER);

    Text headerText = new Text("Select Account Type");
    headerText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    headerText.setStyle("-fx-fill: #2c3e50;");

    Text clientText = new Text("Client: " + client.getName() + " (CPF: " + client.getCpf() + ")");
    clientText.setFont(Font.font("Arial", 14));
    clientText.setStyle("-fx-fill: #7f8c8d;");

    headerBox.getChildren().addAll(headerText, clientText);
    mainLayout.setTop(headerBox);
    BorderPane.setMargin(headerBox, new Insets(0, 0, 20, 0));

    // Create account type options
    HBox optionsBox = new HBox(20);
    optionsBox.setAlignment(Pos.CENTER);
    optionsBox.setPadding(new Insets(30, 20, 30, 20));
    optionsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    // Simple Account Option
    VBox simpleAccountBox =
        createAccountTypeBox(
            "Simple Account",
            "A basic account for everyday banking needs. No interest rates, simple and easy to"
                + " use.",
            "#3498db",
            "💰");

    // Investment Account Option
    VBox investmentAccountBox =
        createAccountTypeBox(
            "Investment Account",
            "Earn interest on your money and manage investments with higher returns.",
            "#9b59b6",
            "📈");

    optionsBox.getChildren().addAll(simpleAccountBox, investmentAccountBox);
    mainLayout.setCenter(optionsBox);

    // Initial Deposit Box
    VBox depositBox = new VBox(15);
    depositBox.setAlignment(Pos.CENTER);
    depositBox.setPadding(new Insets(20));
    depositBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

    Text depositTitle = new Text("Initial Deposit (Optional)");
    depositTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    HBox amountBox = new HBox(10);
    amountBox.setAlignment(Pos.CENTER);

    Label dollarLabel = new Label("$");
    dollarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    TextField amountField = new TextField();
    amountField.setPromptText("0.00");
    amountField.setPrefWidth(150);
    amountField.setStyle("-fx-font-size: 14px;");

    // Force numeric input only
    amountField
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldValue);
              }
            });

    amountBox.getChildren().addAll(dollarLabel, amountField);
    depositBox.getChildren().addAll(depositTitle, amountBox);

    mainLayout.setBottom(depositBox);
    BorderPane.setMargin(depositBox, new Insets(20, 0, 0, 0));

    return new Scene(mainLayout, 650, 500);
  }

  /** Creates a box for an account type option */
  private VBox createAccountTypeBox(String title, String description, String color, String icon) {
    VBox box = new VBox(15);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20));
    box.setPrefWidth(280);
    box.setStyle(
        "-fx-border-color: "
            + color
            + ";"
            + "-fx-border-width: 2px;"
            + "-fx-border-radius: 10px;"
            + "-fx-background-radius: 10px;"
            + "-fx-cursor: hand;");

    // Add hover effect
    box.setOnMouseEntered(
        e ->
            box.setStyle(
                "-fx-border-color: "
                    + color
                    + ";"
                    + "-fx-border-width: 2px;"
                    + "-fx-border-radius: 10px;"
                    + "-fx-background-radius: 10px;"
                    + "-fx-background-color: derive("
                    + color
                    + ", 90%);"
                    + "-fx-cursor: hand;"));

    box.setOnMouseExited(
        e ->
            box.setStyle(
                "-fx-border-color: "
                    + color
                    + ";"
                    + "-fx-border-width: 2px;"
                    + "-fx-border-radius: 10px;"
                    + "-fx-background-radius: 10px;"
                    + "-fx-cursor: hand;"));

    // Icon
    Text iconText = new Text(icon);
    iconText.setFont(Font.font("Arial", 50));

    // Title
    Text titleText = new Text(title);
    titleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    titleText.setStyle("-fx-fill: " + color + ";");

    // Description
    Text descText = new Text(description);
    descText.setWrappingWidth(240);
    descText.setFont(Font.font("Arial", 12));
    descText.setStyle("-fx-fill: #7f8c8d;");

    // Select button
    Button selectButton = new Button("Select");
    selectButton.setStyle(
        "-fx-background-color: "
            + color
            + ";"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;");

    // Add click handler
    box.setOnMouseClicked(e -> handleAccountTypeSelection(title));
    selectButton.setOnAction(e -> handleAccountTypeSelection(title));

    box.getChildren().addAll(iconText, titleText, descText, selectButton);

    return box;
  }

  /** Handles account type selection */
  private void handleAccountTypeSelection(String accountType) {
    try {
      // Get the initial deposit amount from the text field
      TextField amountField = findTextField();
      double initialDeposit = 0.0;

      if (amountField != null && !amountField.getText().isEmpty()) {
        try {
          initialDeposit = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException ex) {
          showAlert(
              Alert.AlertType.ERROR,
              "Invalid Amount",
              "Please enter a valid number for the initial deposit.");
          return;
        }
      }

      // Create appropriate account type
      BankAccount newAccount;
      if (accountType.equals("Simple Account")) {
        newAccount = bank.createAccount(client, Bank.AccountType.SIMPLE);
      } else {
        newAccount = bank.createAccount(client, Bank.AccountType.INVESTMENT);
      }

      // Make initial deposit if needed
      if (initialDeposit > 0) {
        newAccount.depositAmount(initialDeposit);
      }

      // Add account to the list
      accounts.add(newAccount);

      // Show success message
      showAlert(
          Alert.AlertType.INFORMATION,
          "Account Created",
          "Account created successfully!\n\n"
              + "Account Number: "
              + newAccount.getAccountNumber()
              + "\n"
              + "Account Type: "
              + accountType
              + "\n"
              + "Initial Balance: $"
              + String.format("%.2f", newAccount.getAmountStored()));

      // Close this window
      Stage stage = findStage();
      if (stage != null) {
        stage.close();
      }

    } catch (Exception ex) {
      showAlert(
          Alert.AlertType.ERROR,
          "Error",
          "An error occurred while creating the account: " + ex.getMessage());
    }
  }

  /** Helper to find the deposit amount text field */
  private TextField findTextField() {
    Scene scene = null;
    Stage stage = findStage();

    if (stage != null) {
      scene = stage.getScene();
    }

    if (scene != null) {
      return (TextField) scene.lookup("TextField");
    }

    return null;
  }

  /** Helper to find the current stage */
  private Stage findStage() {
    for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
      if (window instanceof Stage) {
        Stage stage = (Stage) window;
        if (stage.getTitle().equals("Select Account Type")) {
          return stage;
        }
      }
    }
    return null;
  }

  /** Shows an alert dialog */
  private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
