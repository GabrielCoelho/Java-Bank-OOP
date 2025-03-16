package br.com.devcoelho.persistence;

import br.com.devcoelho.*;
import br.com.devcoelho.exceptions.InsufficientBalanceException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Handles saving and loading banking data to and from text files */
public class FilePersistence {
  private static final String CLIENTS_FILE = "clients.txt";
  private static final String ACCOUNTS_FILE = "accounts.txt";
  private static final String INVESTMENTS_FILE = "investments.txt";
  private static final String TRANSACTIONS_FILE = "transactions.txt";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * Saves all clients to a text file
   *
   * @param clients list of clients to save
   */
  public static void saveClients(List<Person> clients) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(CLIENTS_FILE))) {
      for (Person client : clients) {
        // Save client information
        writer.println("CLIENT|" + client.getName() + "|" + client.getCpf());

        // Save addresses
        for (Address address : client.getAddress()) {
          String state = (address.getState() != null) ? address.getState().getAbbreviation() : "";
          String addressType = "";
          String locationType = "";

          if (address.getAddressLocationT() != null) {
            addressType = address.getAddressLocationT().getAddressType();
            locationType = address.getAddressLocationT().getAddressLocationType();
          }

          writer.println(
              "ADDRESS|"
                  + client.getCpf()
                  + "|"
                  + address.getAddress()
                  + "|"
                  + address.getHouseNumber()
                  + "|"
                  + address.getHouseComplement()
                  + "|"
                  + address.getNeighborhood()
                  + "|"
                  + address.getCityName()
                  + "|"
                  + state
                  + "|"
                  + address.getCepNumber()
                  + "|"
                  + addressType
                  + "|"
                  + locationType);
        }
      }
      System.out.println("Clients saved successfully to " + CLIENTS_FILE);
    } catch (IOException e) {
      System.err.println("Error saving clients: " + e.getMessage());
    }
  }

  /**
   * Loads all clients from a text file
   *
   * @return list of loaded clients
   */
  public static List<Person> loadClients() {
    Map<String, Person> clientMap = new HashMap<>();

    File file = new File(CLIENTS_FILE);
    if (!file.exists()) {
      System.out.println("No clients file found. Starting with empty client list.");
      return new ArrayList<>();
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\|");

        if (parts[0].equals("CLIENT")) {
          // CLIENT|name|cpf
          String name = parts[1];
          String cpf = parts[2];

          Person person = new Person();
          person.setName(name);
          person.setCpf(cpf);

          clientMap.put(cpf, person);
        } else if (parts[0].equals("ADDRESS")) {
          // ADDRESS|cpf|street|number|complement|neighborhood|city|state|cep|addressType|locationType
          String cpf = parts[1];
          Person person = clientMap.get(cpf);

          if (person != null) {
            Address address = new Address();
            address.setAddress(parts[2]);
            address.setHouseNumber(parts[3]);
            address.setHouseComplement(parts[4]);
            address.setNeighborhood(parts[5]);
            address.setCityName(parts[6]);

            if (parts[7] != null && !parts[7].isEmpty()) {
              address.setState(BrazilianState.fromAbbreviation(parts[7]));
            }

            address.setCepNumber(parts[8]);

            AddressType addressType = new AddressType();
            addressType.setAddressType(parts[9]);
            addressType.setAddressLocationType(parts[10]);
            address.setAddressLocationT(addressType);

            person.getAddress().add(address);
          }
        }
      }

      System.out.println("Loaded " + clientMap.size() + " clients from " + CLIENTS_FILE);
    } catch (IOException e) {
      System.err.println("Error loading clients: " + e.getMessage());
    }

    return new ArrayList<>(clientMap.values());
  }

  /**
   * Saves all accounts to text files
   *
   * @param accounts list of accounts to save
   */
  public static void saveAccounts(List<BankAccount> accounts) {
    try (PrintWriter accountWriter = new PrintWriter(new FileWriter(ACCOUNTS_FILE));
        PrintWriter investmentWriter = new PrintWriter(new FileWriter(INVESTMENTS_FILE));
        PrintWriter transactionWriter = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {

      for (BankAccount account : accounts) {
        // Save basic account information
        String accountType = (account instanceof BankInvestmentAccount) ? "INVESTMENT" : "SIMPLE";
        String interestRate =
            (account instanceof BankInvestmentAccount)
                ? String.valueOf(((BankInvestmentAccount) account).getInterestRate())
                : "0.0";

        accountWriter.println(
            account.getAccountNumber()
                + "|"
                + account.getAgency()
                + "|"
                + account.getAmountStored()
                + "|"
                + account.getClient().getCpf()
                + "|"
                + accountType
                + "|"
                + interestRate
                + "|"
                + DATE_FORMAT.format(account.getOpeningDate()));

        // Save investments if this is an investment account
        if (account instanceof BankInvestmentAccount) {
          BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;

          for (Map.Entry<String, BankInvestmentAccount.Investment> entry :
              investmentAccount.getInvestments().entrySet()) {

            BankInvestmentAccount.Investment investment = entry.getValue();
            investmentWriter.println(
                account.getAccountNumber()
                    + "|"
                    + investment.getName()
                    + "|"
                    + investment.getPrincipal()
                    + "|"
                    + investment.getAnnualRate());
          }
        }

        // Save transactions for all accounts
        for (Transaction transaction : account.getTransactionHistory()) {
          String destAccountNumber = "";

          if (transaction.getDestinationAccount() != null
              && transaction.getDestinationAccount() instanceof BankAccount) {
            destAccountNumber =
                String.valueOf(
                    ((BankAccount) transaction.getDestinationAccount()).getAccountNumber());
          }

          transactionWriter.println(
              account.getAccountNumber()
                  + "|"
                  + transaction.getType()
                  + "|"
                  + transaction.getAmount()
                  + "|"
                  + DATE_FORMAT.format(transaction.getDate())
                  + "|"
                  + destAccountNumber);
        }
      }

      System.out.println("Accounts saved successfully to text files");
    } catch (IOException e) {
      System.err.println("Error saving accounts: " + e.getMessage());
    }
  }

  /**
   * Loads all accounts from text files
   *
   * @param clients list of clients for account association
   * @param bank the bank instance for account creation
   * @return list of loaded accounts
   */
  public static List<BankAccount> loadAccounts(List<Person> clients, Bank bank) {
    Map<Integer, BankAccount> accountMap = new HashMap<>();
    Map<String, Person> clientByCpfMap = new HashMap<>();

    // Create map of clients by CPF for quick lookup
    for (Person client : clients) {
      clientByCpfMap.put(client.getCpf(), client);
      // Add client to bank if not already added
      if (!bank.getClients().contains(client)) {
        bank.addClient(client);
      }
    }

    // Load base account information
    File accountFile = new File(ACCOUNTS_FILE);
    if (!accountFile.exists()) {
      System.out.println("No accounts file found. Starting with empty account list.");
      return new ArrayList<>();
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(accountFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\|");

        if (parts.length >= 7) {
          int accountNumber = Integer.parseInt(parts[0]);
          String agency = parts[1];
          double balance = Double.parseDouble(parts[2]);
          String clientCpf = parts[3];
          String accountType = parts[4];
          double interestRate = Double.parseDouble(parts[5]);
          Date openingDate = DATE_FORMAT.parse(parts[6]);

          // Find client
          Person client = clientByCpfMap.get(clientCpf);
          if (client == null) {
            System.out.println(
                "Warning: Client with CPF "
                    + clientCpf
                    + " not found for account "
                    + accountNumber);
            continue;
          }

          // Create appropriate account type
          BankAccount account;
          if (accountType.equals("INVESTMENT")) {
            account = new BankInvestmentAccount(client);
            ((BankInvestmentAccount) account).setInterestRate(interestRate);
          } else {
            account = new BankSimpleAccount(client);
          }

          // Set account properties using reflection
          try {
            // Set account number
            java.lang.reflect.Field accountNumberField =
                BankAccount.class.getDeclaredField("accountNumber");
            accountNumberField.setAccessible(true);
            accountNumberField.set(account, accountNumber);

            // Set agency
            java.lang.reflect.Field agencyField = BankAccount.class.getDeclaredField("agency");
            agencyField.setAccessible(true);
            agencyField.set(account, agency);

            // Set balance
            java.lang.reflect.Field balanceField =
                BankAccount.class.getDeclaredField("amountStored");
            balanceField.setAccessible(true);
            balanceField.set(account, balance);

            // Set opening date
            java.lang.reflect.Field openingDateField =
                BankAccount.class.getDeclaredField("openingDate");
            openingDateField.setAccessible(true);
            openingDateField.set(account, openingDate);

            // Set empty transaction history list
            java.lang.reflect.Field transactionHistoryField =
                BankAccount.class.getDeclaredField("transactionHistory");
            transactionHistoryField.setAccessible(true);
            transactionHistoryField.set(account, new ArrayList<Transaction>());
          } catch (Exception e) {
            System.err.println("Error setting account fields: " + e.getMessage());
            continue;
          }

          accountMap.put(accountNumber, account);
        }
      }

      System.out.println("Loaded " + accountMap.size() + " accounts from " + ACCOUNTS_FILE);

      // Load investments for investment accounts
      loadInvestments(accountMap);

      // Load transactions for all accounts
      loadTransactions(accountMap);

    } catch (IOException | ParseException e) {
      System.err.println("Error loading accounts: " + e.getMessage());
    }

    return new ArrayList<>(accountMap.values());
  }

  /**
   * Loads investments for investment accounts
   *
   * @param accountMap map of accounts by account number
   */
  private static void loadInvestments(Map<Integer, BankAccount> accountMap) {
    File investmentFile = new File(INVESTMENTS_FILE);
    if (!investmentFile.exists()) {
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(investmentFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\|");

        if (parts.length >= 4) {
          int accountNumber = Integer.parseInt(parts[0]);
          String name = parts[1];
          double principal = Double.parseDouble(parts[2]);
          double annualRate = Double.parseDouble(parts[3]);

          BankAccount account = accountMap.get(accountNumber);
          if (account instanceof BankInvestmentAccount) {
            BankInvestmentAccount investmentAccount = (BankInvestmentAccount) account;

            try {
              investmentAccount.createInvestment(name, principal, annualRate);
            } catch (InsufficientBalanceException e) {
              // Fix balance issue by adding enough funds and then removing them
              double currentBalance = investmentAccount.getAmountStored();

              // We need to use reflection to temporarily add funds
              try {
                java.lang.reflect.Field balanceField =
                    BankAccount.class.getDeclaredField("amountStored");
                balanceField.setAccessible(true);

                // Add enough for the investment
                balanceField.setDouble(investmentAccount, currentBalance + principal);

                // Create the investment now that we have sufficient funds
                investmentAccount.createInvestment(name, principal, annualRate);

                // Balance will have been automatically adjusted by createInvestment
              } catch (Exception ex) {
                System.err.println("Error restoring investment: " + ex.getMessage());
              }
            }
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Error loading investments: " + e.getMessage());
    }
  }

  /**
   * Loads transactions for all accounts
   *
   * @param accountMap map of accounts by account number
   */
  private static void loadTransactions(Map<Integer, BankAccount> accountMap) {
    File transactionFile = new File(TRANSACTIONS_FILE);
    if (!transactionFile.exists()) {
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(transactionFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\|");

        if (parts.length >= 4) {
          int accountNumber = Integer.parseInt(parts[0]);
          TransactionType type = TransactionType.valueOf(parts[1]);
          double amount = Double.parseDouble(parts[2]);
          Date date = DATE_FORMAT.parse(parts[3]);

          // Get destination account if available
          BankAccount destAccount = null;
          if (parts.length >= 5 && !parts[4].isEmpty()) {
            int destAccountNumber = Integer.parseInt(parts[4]);
            destAccount = accountMap.get(destAccountNumber);
          }

          BankAccount account = accountMap.get(accountNumber);
          if (account != null) {
            // Create and add transaction
            Transaction transaction = new Transaction(type, amount, date, account, destAccount);

            // Add transaction to account's history
            try {
              java.lang.reflect.Field transactionHistoryField =
                  BankAccount.class.getDeclaredField("transactionHistory");
              transactionHistoryField.setAccessible(true);

              @SuppressWarnings("unchecked")
              List<Transaction> transactions =
                  (List<Transaction>) transactionHistoryField.get(account);

              transactions.add(transaction);
            } catch (Exception e) {
              System.err.println("Error adding transaction: " + e.getMessage());
            }
          }
        }
      }
    } catch (IOException | ParseException e) {
      System.err.println("Error loading transactions: " + e.getMessage());
    }
  }
}
