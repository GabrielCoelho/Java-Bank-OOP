package br.com.devcoelho;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a bank with multiple clients and accounts */
public class Bank {

  private String name;
  private String bankCode;
  private List<Person> clients = new ArrayList<>();
  private Map<Person, List<BankAccount>> clientAccounts = new HashMap<>();

  public Bank(String name, String bankCode) {
    this.name = name;
    this.bankCode = bankCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBankCode() {
    return bankCode;
  }

  public void setBankCode(String bankCode) {
    this.bankCode = bankCode;
  }

  /**
   * Gets a list of all clients
   *
   * @return list of all clients
   */
  public List<Person> getClients() {
    return new ArrayList<>(clients); // Return a copy to prevent direct modification
  }

  /**
   * Adds a new client to the bank
   *
   * @param client the client to add
   * @return true if added successfully, false if already exists
   */
  public boolean addClient(Person client) {
    if (client == null || client.getCpf() == null) {
      throw new IllegalArgumentException("Client must have a valid CPF");
    }

    // Check if client with same CPF exists
    for (Person existingClient : clients) {
      if (existingClient.getCpf().equals(client.getCpf())) {
        return false; // Client already exists
      }
    }

    clients.add(client);
    clientAccounts.put(client, new ArrayList<>());
    return true;
  }

  /**
   * Creates a new account for a client
   *
   * @param client the client who owns the account
   * @param accountType the type of account to create
   * @return the created bank account
   */
  public BankAccount createAccount(Person client, AccountType accountType) {
    if (!clients.contains(client)) {
      throw new IllegalArgumentException("Client not registered with this bank");
    }

    BankAccount account;

    switch (accountType) {
      case SIMPLE:
        account = new BankSimpleAccount(client);
        break;
      case INVESTMENT:
        account = new BankInvestmentAccount(client);
        break;
      default:
        throw new IllegalArgumentException("Unsupported account type");
    }

    // Add account to client's account list
    clientAccounts.get(client).add(account);

    return account;
  }

  /**
   * Gets all accounts for a client
   *
   * @param client the client
   * @return list of all accounts owned by the client
   */
  public List<BankAccount> getClientAccounts(Person client) {
    List<BankAccount> accounts = clientAccounts.get(client);
    if (accounts == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(accounts); // Return a copy to prevent direct modification
  }

  /**
   * Finds a client by CPF
   *
   * @param cpf the CPF to search for
   * @return the client if found, null otherwise
   */
  public Person findClientByCpf(String cpf) {
    for (Person client : clients) {
      if (client.getCpf().equals(cpf)) {
        return client;
      }
    }
    return null;
  }

  /** Account types supported by the bank */
  public enum AccountType {
    SIMPLE,
    INVESTMENT
  }
}
