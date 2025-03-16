package br.com.devcoelho;

import br.com.devcoelho.exceptions.InsufficientBalanceException;
import br.com.devcoelho.exceptions.InvalidAccountException;
import br.com.devcoelho.persistence.FilePersistence;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Main application for the banking system with interactive menu interface */
public class App {

  private static Bank bank;
  private static Scanner scanner;
  private static List<Person> clients = new ArrayList<>();
  private static List<BankAccount> accounts = new ArrayList<>();

  public static void main(String[] args) {
    // Initialize bank and scanner
    bank = new Bank("Potato's Bank", "777");
    scanner = new Scanner(System.in);
    // Load clients and accounts from files
    try {
        clients = FilePersistence.loadClients();
        accounts = FilePersistence.loadAccounts(clients, bank);
        
        System.out.println("Data loaded successfully.");
    } catch (Exception e) {
        System.out.println("Error loading data: " + e.getMessage());
        System.out.println("Starting with empty system.");
    }
    System.out.println(String.format("===== Welcome to %s! =====", bank.getName()));

    
    int option = -1;
    do {
      displayMainMenu();
      try {
        option = Integer.parseInt(scanner.nextLine());
        processMainMenuOption(option);
      } catch (NumberFormatException e) {
        System.out.println("Please enter a valid number.");
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    } while (option != 0);

    // Save data before exiting
    System.out.println("Saving data to files...");
    try {
        FilePersistence.saveClients(clients);
        FilePersistence.saveAccounts(accounts);
        
        System.out.println("Data saved successfully.");
    } catch (Exception e) {
        System.out.println("Error saving data: " + e.getMessage());
    }

    scanner.close();
    System.out.println("Thank you for using " + bank.getName() + ". See you soon!");
  }

  /** Display the main menu */
  private static void displayMainMenu() {
    System.out.println("\n===== MAIN MENU =====");
    System.out.println("1. Create an Account");
    System.out.println("2. Manage an Account");
    System.out.println("3. Remove an Account");
    System.out.println("4. Show Account Information");
    System.out.println("5. Simulate Time Passage");
    System.out.println("0. Exit the program");
    System.out.print("Enter your choice: ");
  }

  /** Process the selected option from the main menu */
  private static void processMainMenuOption(int option) {
    switch (option) {
      case 1:
        createAccount();
        break;
      case 2:
        manageAccount();
        break;
      case 3:
        removeAccount();
        break;
      case 4:
        showAccountInfo();
        break;
      case 5:
        simulateTimePassage();
        break;
      case 0:
        // Exit option, does nothing here as the loop will end
        break;
      default:
        System.out.println("Invalid option. Please try again.");
        break;
    }
  }

  /** Implements account creation (option 1) */
  private static void createAccount() {
    System.out.println("\n===== CREATE ACCOUNT =====");

    // First check if there's an existing client or need to create a new one
    Person client = getOrCreateClient();
    if (client == null) {
      return; // Operation cancelled by user
    }

    // Add the client to the bank if not already registered
    if (!bank.getClients().contains(client)) {
      bank.addClient(client);
    }

    // Choose account type
    System.out.println("\nSelect account type:");
    System.out.println("1. Simple Account");
    System.out.println("2. Investment Account");
    System.out.print("Enter your choice: ");

    try {
      int accountType = Integer.parseInt(scanner.nextLine());
      BankAccount newAccount = null;

      switch (accountType) {
        case 1:
          newAccount = bank.createAccount(client, Bank.AccountType.SIMPLE);
          System.out.println("Simple Account created successfully!");
          break;
        case 2:
          newAccount = bank.createAccount(client, Bank.AccountType.INVESTMENT);
          System.out.println("Investment Account created successfully!");
          break;
        default:
          System.out.println("Invalid option. Operation cancelled.");
          return;
      }

      // Store the new account in the accounts list
      accounts.add(newAccount);

      // Show created account details
      System.out.println("\nAccount details:");
      System.out.println("Account Number: " + newAccount.getAccountNumber());
      System.out.println("Agency: " + newAccount.getAgency());
      System.out.println("Account Holder: " + newAccount.getClient().getName());

      // Ask if user wants to make an initial deposit
      System.out.print("\nWould you like to make an initial deposit? (Y/N): ");
      String response = scanner.nextLine().trim().toUpperCase();

      if (response.equals("Y")) {
        System.out.print("Enter initial deposit amount: $ ");
        try {
          double amount = Double.parseDouble(scanner.nextLine());
          newAccount.depositAmount(amount);
          System.out.println(
              "Deposit successful! Current balance: $ "
                  + String.format("%.2f", newAccount.getAmountStored()));
        } catch (NumberFormatException e) {
          System.out.println("Invalid amount. Deposit not processed.");
        } catch (IllegalArgumentException e) {
          System.out.println("Error: " + e.getMessage());
        }
      }

    } catch (NumberFormatException e) {
      System.out.println("Invalid input. Operation cancelled.");
    }
  }

  /** Implements account management (option 2) */
  private static void manageAccount() {
    BankAccount account = selectAccount();
    if (account == null) {
      return; // Operation cancelled or no account found
    }

    int option = -1;
    do {
      System.out.println("\n===== MANAGE ACCOUNT #" + account.getAccountNumber() + " =====");
      System.out.println("1. Deposit");
      System.out.println("2. Withdraw");
      System.out.println("3. Transfer");
      System.out.println("4. View Statement");
      System.out.println("5. View Transaction History");

      // Specific options for investment account
      if (account instanceof BankInvestmentAccount) {
        System.out.println("6. Create Investment");
        System.out.println("7. Liquidate Investment");
        System.out.println("8. View Investment Details");
      }

      System.out.println("0. Return to Main Menu");
      System.out.print("Enter your choice: ");

      try {
        option = Integer.parseInt(scanner.nextLine());

        switch (option) {
          case 1: // Deposit
            deposit(account);
            break;
          case 2: // Withdraw
            withdraw(account);
            break;
          case 3: // Transfer
            transfer(account);
            break;
          case 4: // View Statement
            account.printExtract();
            break;
          case 5: // View Transaction History
            account.printTransactionHistory();
            break;
          case 6: // Create Investment (investment account only)
            if (account instanceof BankInvestmentAccount) {
              createInvestment((BankInvestmentAccount) account);
            } else {
              System.out.println("Invalid option for this account type.");
            }
            break;
          case 7: // Liquidate Investment (investment account only)
            if (account instanceof BankInvestmentAccount) {
              liquidateInvestment((BankInvestmentAccount) account);
            } else {
              System.out.println("Invalid option for this account type.");
            }
            break;
          case 8: // View Investment Details (investment account only)
            if (account instanceof BankInvestmentAccount) {
              showInvestments((BankInvestmentAccount) account);
            } else {
              System.out.println("Invalid option for this account type.");
            }
            break;
          case 0: // Return to main menu
            break;
          default:
            System.out.println("Invalid option. Please try again.");
            break;
        }

      } catch (NumberFormatException e) {
        System.out.println("Please enter a valid number.");
        option = -1; // Continue in the loop
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }

    } while (option != 0);
  }

  /** Implements account removal (option 3) */
  private static void removeAccount() {
    System.out.println("\n===== REMOVE ACCOUNT =====");

    BankAccount account = selectAccount();
    if (account == null) {
      return; // Operation cancelled or no account found
    }

    // Removal confirmation
    System.out.println("\nDetails of account to be removed:");
    System.out.println("Account Number: " + account.getAccountNumber());
    System.out.println("Agency: " + account.getAgency());
    System.out.println("Account Holder: " + account.getClient().getName());
    System.out.println("Current Balance: $ " + String.format("%.2f", account.getAmountStored()));

    System.out.print("\nAre you sure you want to remove this account? (Y/N): ");
    String response = scanner.nextLine().trim().toUpperCase();

    if (response.equals("Y")) {
      // Remove account from client's account list
      Person client = account.getClient();
      List<BankAccount> clientAccounts = bank.getClientAccounts(client);

      // Since we don't have a direct remove method, create a new list
      List<BankAccount> updatedAccounts = new ArrayList<>();
      for (BankAccount acc : clientAccounts) {
        if (acc.getAccountNumber() != account.getAccountNumber()) {
          updatedAccounts.add(acc);
        }
      }

      // Remove account from our control list
      accounts.remove(account);

      System.out.println("Account successfully removed!");
    } else {
      System.out.println("Operation cancelled by user.");
    }
  }

  /** Implements showing account information (option 4) */
  private static void showAccountInfo() {
    System.out.println("\n===== ACCOUNT INFORMATION =====");

    BankAccount account = selectAccount();
    if (account == null) {
      return; // Operation cancelled or no account found
    }

    // Display detailed account information
    System.out.println("\n----- ACCOUNT DETAILS #" + account.getAccountNumber() + " -----");
    System.out.println("Account Type: " + account.getClass().getSimpleName());
    System.out.println("Account Number: " + account.getAccountNumber());
    System.out.println("Agency: " + account.getAgency());
    System.out.println("Opening Date: " + account.getOpeningDate());
    System.out.println("Current Balance: $ " + String.format("%.2f", account.getAmountStored()));

    // Account holder information
    Person client = account.getClient();
    System.out.println("\n----- ACCOUNT HOLDER INFORMATION -----");
    System.out.println("Name: " + client.getName());
    System.out.println("CPF: " + client.getCpf());

    // Account holder addresses
    System.out.println("\n----- ACCOUNT HOLDER ADDRESSES -----");
    List<Address> addresses = client.getAddress();
    if (addresses.isEmpty()) {
      System.out.println("No registered addresses.");
    } else {
      for (int i = 0; i < addresses.size(); i++) {
        Address address = addresses.get(i);
        System.out.println("Address " + (i + 1) + ":");
        System.out.println(address.getFormattedAddress());
      }
    }

    // If investment account, show additional information
    if (account instanceof BankInvestmentAccount) {
      BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;
      System.out.println("\n----- INVESTMENT INFORMATION -----");
      System.out.println(
          "Base Interest Rate: "
              + String.format("%.2f%%", investmentAccount.getInterestRate() * 100)
              + " per year");

      // Show active investments, if any
      showInvestments(investmentAccount);
    }

    // Ask if user wants to see transaction history
    System.out.print("\nWould you like to see the transaction history? (Y/N): ");
    String response = scanner.nextLine().trim().toUpperCase();

    if (response.equals("Y")) {
      account.printTransactionHistory();
    }
  }

  /** Utility to get an existing client or create a new one */
  private static Person getOrCreateClient() {
    if (!clients.isEmpty() || !bank.getClients().isEmpty()) {
      System.out.println("\nDo you want to use an existing client or create a new one?");
      System.out.println("1. Existing Client");
      System.out.println("2. New Client");
      System.out.println("0. Cancel");
      System.out.print("Enter your choice: ");

      try {
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
          case 1: // Existing client
            return selectClient();
          case 2: // New client
            return createClient();
          case 0: // Cancel
            return null;
          default:
            System.out.println("Invalid option. Operation cancelled.");
            return null;
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Operation cancelled.");
        return null;
      }
    } else {
      // If no clients registered, automatically create a new one
      System.out.println("\nNo clients registered. Creating a new client.");
      return createClient();
    }
  }

  /** Utility to create a new client */
  private static Person createClient() {
    System.out.println("\n===== CLIENT REGISTRATION =====");

    System.out.print("Client name: ");
    String name = scanner.nextLine().trim();

    System.out.print("CPF (numbers only): ");
    String cpf = scanner.nextLine().trim();

    // Simple CPF validation
    if (cpf.length() != 11 && cpf.length() != 14) { // 14 pelos CPFs formatados com pontuação
      System.out.println("Invalid CPF. It must have 11 digits.");
      return null;
    }

    // Check if CPF is already registered
    Person existingClient = bank.findClientByCpf(cpf);
    if (existingClient != null) {
      System.out.println("CPF already registered. Using existing client.");
      return existingClient;
    }

    // Get address
    System.out.println("\n----- ADDRESS REGISTRATION -----");
    System.out.print("ZIP Code (format 00000-000): ");
    String cep = scanner.nextLine().trim();

    // Create client
    Person person = new Person();
    person.setName(name);
    person.setCpf(cpf);

    // Create and add address
    Address address = new Address();
    boolean cepValid = address.validateAndFillAddressByCep(cep);

    if (!cepValid) {
      System.out.println("Invalid or not found ZIP Code. Please enter address manually.");

      System.out.print("Street: ");
      address.setAddress(scanner.nextLine().trim());

      System.out.print("Number: ");
      address.setHouseNumber(scanner.nextLine().trim());

      System.out.print("Complement: ");
      address.setHouseComplement(scanner.nextLine().trim());

      System.out.print("Neighborhood: ");
      address.setNeighborhood(scanner.nextLine().trim());

      System.out.print("City: ");
      address.setCityName(scanner.nextLine().trim());

      System.out.print("State (UF): ");
      String uf = scanner.nextLine().trim().toUpperCase();
      address.setState(BrazilianState.fromAbbreviation(uf));

      System.out.print("ZIP Code: ");
      address.setCepNumber(scanner.nextLine().trim());
    } else {
      // If valid ZIP code, only need number and complement
      System.out.print("House Number: ");
      address.setHouseNumber(scanner.nextLine().trim());

      System.out.print("Complement: ");
      address.setHouseComplement(scanner.nextLine().trim());
    }

    // Set address type
    AddressType addressType = new AddressType();
    addressType.setAddressType("RESIDENTIAL");
    addressType.setAddressLocationType("HOME");
    address.setAddressLocationT(addressType);

    // Add address to client
    person.getAddress().add(address);

    // Add client to our control list
    clients.add(person);

    System.out.println("\nClient registered successfully!");
    return person;
  }

  /** Utility to select an existing client */
  private static Person selectClient() {
    List<Person> allClients = bank.getClients();
    if (allClients.isEmpty()) {
      allClients = clients; // If no clients in bank, use our local list
    }

    if (allClients.isEmpty()) {
      System.out.println("No clients registered. Operation cancelled.");
      return null;
    }

    System.out.println("\n===== REGISTERED CLIENTS =====");
    for (int i = 0; i < allClients.size(); i++) {
      Person client = allClients.get(i);
      System.out.println((i + 1) + ". " + client.getName() + " (CPF: " + client.getCpf() + ")");
    }

    System.out.print("\nSelect client (1-" + allClients.size() + "): ");
    try {
      int index = Integer.parseInt(scanner.nextLine()) - 1;

      if (index >= 0 && index < allClients.size()) {
        return allClients.get(index);
      } else {
        System.out.println("Invalid option. Operation cancelled.");
        return null;
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input. Operation cancelled.");
      return null;
    }
  }

  /** Utility to select an account */
  private static BankAccount selectAccount() {
    // Check if there are registered accounts
    if (accounts.isEmpty()) {
      System.out.println("No accounts registered. Operation cancelled.");
      return null;
    }

    System.out.println("\n===== REGISTERED ACCOUNTS =====");
    for (int i = 0; i < accounts.size(); i++) {
      BankAccount account = accounts.get(i);
      System.out.println(
          (i + 1)
              + ". Account #"
              + account.getAccountNumber()
              + " - "
              + account.getClient().getName()
              + " ("
              + account.getClass().getSimpleName()
              + ")"
              + " - Balance: $ "
              + String.format("%.2f", account.getAmountStored()));
    }

    System.out.print("\nSelect account (1-" + accounts.size() + "): ");
    try {
      int index = Integer.parseInt(scanner.nextLine()) - 1;

      if (index >= 0 && index < accounts.size()) {
        return accounts.get(index);
      } else {
        System.out.println("Invalid option. Operation cancelled.");
        return null;
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input. Operation cancelled.");
      return null;
    }
  }

  /** Utility to make a deposit */
  private static void deposit(BankAccount account) {
    System.out.print("\nEnter deposit amount: $ ");
    try {
      double amount = Double.parseDouble(scanner.nextLine());
      account.depositAmount(amount);
      System.out.println("Deposit successful!");
      System.out.println("New balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (NumberFormatException e) {
      System.out.println("Invalid amount. Operation cancelled.");
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Utility to make a withdrawal */
  private static void withdraw(BankAccount account) {
    System.out.print("\nEnter withdrawal amount: $ ");
    try {
      double amount = Double.parseDouble(scanner.nextLine());
      account.withdrawAmount(amount);
      System.out.println("Withdrawal successful!");
      System.out.println("New balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (NumberFormatException e) {
      System.out.println("Invalid amount. Operation cancelled.");
    } catch (InsufficientBalanceException e) {
      System.out.println("Error: " + e.getMessage());
      System.out.println("Current balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Utility to make a transfer */
  private static void transfer(BankAccount sourceAccount) {
    System.out.println("\n===== TRANSFER =====");

    // First, select destination account
    System.out.println("Select destination account:");
    BankAccount destinationAccount = selectAccount();
    if (destinationAccount == null) {
      return; // Operation cancelled
    }

    // Check not transferring to the same account
    if (sourceAccount.getAccountNumber() == destinationAccount.getAccountNumber()) {
      System.out.println("Cannot transfer to the same account. Operation cancelled.");
      return;
    }

    // Request transfer amount
    System.out.print("\nEnter transfer amount: $ ");
    try {
      double amount = Double.parseDouble(scanner.nextLine());
      sourceAccount.transferAmount(amount, destinationAccount);
      System.out.println("Transfer successful!");
      System.out.println(
          "New source account balance: $ "
              + String.format("%.2f", sourceAccount.getAmountStored()));
    } catch (NumberFormatException e) {
      System.out.println("Invalid amount. Operation cancelled.");
    } catch (InsufficientBalanceException e) {
      System.out.println("Error: " + e.getMessage());
      System.out.println(
          "Current balance: $ " + String.format("%.2f", sourceAccount.getAmountStored()));
    } catch (InvalidAccountException e) {
      System.out.println("Error: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Utility to create an investment */
  private static void createInvestment(BankInvestmentAccount account) {
    System.out.println("\n===== CREATE INVESTMENT =====");

    System.out.print("Investment name: ");
    String name = scanner.nextLine().trim();

    System.out.print("Investment amount: $ ");
    try {
      double amount = Double.parseDouble(scanner.nextLine());

      System.out.print("Annual interest rate (%): ");
      double ratePercentage = Double.parseDouble(scanner.nextLine());
      double rate = ratePercentage / 100.0;

      account.createInvestment(name, amount, rate);
      System.out.println("Investment created successfully!");
      System.out.println(
          "Current account balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (NumberFormatException e) {
      System.out.println("Invalid amount. Operation cancelled.");
    } catch (InsufficientBalanceException e) {
      System.out.println("Error: " + e.getMessage());
      System.out.println(
          "Available balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Utility to liquidate an investment */
  private static void liquidateInvestment(BankInvestmentAccount account) {
    System.out.println("\n===== LIQUIDATE INVESTMENT =====");

    // Check if there are investments
    if (account.getInvestments().isEmpty()) {
      System.out.println("No active investments. Operation cancelled.");
      return;
    }

    // List available investments
    showInvestments(account);

    System.out.print("\nEnter the name of the investment to liquidate: ");
    String name = scanner.nextLine().trim();

    try {
      double amount = account.liquidateInvestment(name);
      System.out.println("Investment liquidated successfully!");
      System.out.println("Returned amount: $ " + String.format("%.2f", amount));
      System.out.println(
          "New account balance: $ " + String.format("%.2f", account.getAmountStored()));
    } catch (IllegalArgumentException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /** Utility to show investments */
  private static void showInvestments(BankInvestmentAccount account) {
    System.out.println("\n===== ACTIVE INVESTMENTS =====");

    if (account.getInvestments().isEmpty()) {
      System.out.println("No active investments.");
      return;
    }

    for (String name : account.getInvestments().keySet()) {
      BankInvestmentAccount.Investment inv = account.getInvestments().get(name);
      System.out.println(
          String.format(
              "%s: Initial Amount $ %.2f, Current Value $ %.2f (Rate: %.2f%%)",
              name, inv.getPrincipal(), inv.getCurrentValue(), inv.getAnnualRate() * 100));
    }
  }

  /** Simulates the passage of time for all accounts */
  private static void simulateTimePassage() {
    System.out.println("\n===== SIMULATE TIME PASSAGE =====");

    // Check if there are any accounts
    if (accounts.isEmpty()) {
      System.out.println("No accounts found in the system. Nothing to simulate.");
      return;
    }

    // Ask for time period
    System.out.println("What time period would you like to simulate?");
    System.out.println("1. One Month");
    System.out.println("2. One Year (12 months)");
    System.out.print("Enter your choice: ");

    int choice;
    int months = 0;

    try {
      choice = Integer.parseInt(scanner.nextLine());

      switch (choice) {
        case 1:
          months = 1;
          System.out.println("Simulating one month passing...");
          break;
        case 2:
          months = 12;
          System.out.println("Simulating one year (12 months) passing...");
          break;
        default:
          System.out.println("Invalid option. Operation cancelled.");
          return;
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input. Operation cancelled.");
      return;
    }

    // Get monthly fee for simple accounts
    System.out.print("Enter monthly maintenance fee for simple accounts ($ per month): ");
    double monthlyFee;

    try {
      monthlyFee = Double.parseDouble(scanner.nextLine());
      if (monthlyFee < 0) {
        System.out.println("Fee cannot be negative. Using $0 instead.");
        monthlyFee = 0;
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid amount. Using $0 instead.");
      monthlyFee = 0;
    }

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
        investmentAccount.simulateInvestmentTimePassage(months);

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
              System.out.println(
                  "Warning: Account #"
                      + simpleAccount.getAccountNumber()
                      + " has insufficient funds for monthly fee.");
              break; // Stop charging this account
            }
          } catch (Exception e) {
            System.out.println(
                "Error applying fee to Account #"
                    + simpleAccount.getAccountNumber()
                    + ": "
                    + e.getMessage());
          }
        }

        simpleAccountsUpdated++;
      }
    }

    // Show results
    System.out.println("\nTime simulation complete!");
    System.out.println("Period simulated: " + months + (months == 1 ? " month" : " months"));
    System.out.println(
        simpleAccountsUpdated
            + " simple accounts were charged $ "
            + String.format("%.2f", monthlyFee)
            + " per month (total: $ "
            + String.format("%.2f", monthlyFee * months)
            + ")");
    System.out.println(investmentAccountsUpdated + " investment accounts had interest applied.");

    // Ask if user wants to see updated balances
    System.out.print("\nWould you like to see updated account balances? (Y/N): ");
    String response = scanner.nextLine().trim().toUpperCase();

    if (response.equals("Y")) {
      System.out.println("\n===== UPDATED ACCOUNT BALANCES =====");
      for (BankAccount account : accounts) {
        System.out.println(
            "Account #"
                + account.getAccountNumber()
                + " - "
                + account.getClient().getName()
                + " ("
                + account.getClass().getSimpleName()
                + ")"
                + " - Balance: $ "
                + String.format("%.2f", account.getAmountStored()));
      }
    }
  }
}
