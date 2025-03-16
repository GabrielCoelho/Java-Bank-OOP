package br.com.devcoelho;

import br.com.devcoelho.exceptions.InsufficientBalanceException;
import br.com.devcoelho.exceptions.InvalidAccountException;
import br.com.devcoelho.interfaces.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Abstract base class for all bank account types */
public abstract class BankAccount implements BankInterface {

  private static final String DEFAULT_AGENCY = "Mogi Guacu";
  private static final AtomicInteger ACCOUNT_SEQUENTIAL = new AtomicInteger(1);

  public static String getDefaultAgency() {
    return DEFAULT_AGENCY;
  }

  protected String agency;
  protected int accountNumber;
  protected double amountStored;
  protected Person client;
  protected List<Transaction> transactionHistory;
  protected Date openingDate;

  public BankAccount(Person personToCreateAccount) {
    if (personToCreateAccount == null) {
      throw new IllegalArgumentException("Cannot create account without a valid person");
    }

    this.agency = BankAccount.DEFAULT_AGENCY;
    this.accountNumber = ACCOUNT_SEQUENTIAL.getAndIncrement();
    this.client = personToCreateAccount;
    this.amountStored = 0.0;
    this.transactionHistory = new ArrayList<>();
    this.openingDate = new Date();
  }

  public String getAgency() {
    return agency;
  }

  public void setAgency(String agency) {
    this.agency = agency;
  }

  public int getAccountNumber() {
    return accountNumber;
  }

  public double getAmountStored() {
    return amountStored;
  }

  protected void setAmountStored(double amountStored) {
    this.amountStored = amountStored;
  }

  public Person getClient() {
    return client;
  }

  public List<Transaction> getTransactionHistory() {
    return new ArrayList<>(transactionHistory); // Return a copy to prevent direct modification
  }

  public Date getOpeningDate() {
    return openingDate;
  }

  @Override
  public boolean isValid() {
    return this.agency != null
        && this.agency.trim().length() > 0
        && this.client != null
        && this.accountNumber > 0;
  }

  @Override
  public void withdrawAmount(double value) {
    if (value <= 0) {
      throw new IllegalArgumentException("Withdrawal amount must be positive");
    }

    if (value > getAmountStored()) {
      throw new InsufficientBalanceException("Insufficient balance for withdrawal");
    }

    setAmountStored(getAmountStored() - value);
    recordTransaction(TransactionType.WITHDRAWAL, -value, null);
  }

  @Override
  public void depositAmount(double value) {
    if (value <= 0) {
      throw new IllegalArgumentException("Deposit amount must be positive");
    }

    setAmountStored(getAmountStored() + value);
    recordTransaction(TransactionType.DEPOSIT, value, null);
  }

  @Override
  public void transferAmount(double value, BankInterface destinyAccount) {
    if (destinyAccount == null || !destinyAccount.isValid()) {
      throw new InvalidAccountException("Destination account is invalid");
    }

    if (value <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive");
    }

    if (value > getAmountStored()) {
      throw new InsufficientBalanceException("Insufficient balance for transfer");
    }

    this.withdrawAmount(value);
    destinyAccount.depositAmount(value);

    // Update the transaction type to TRANSFER (overrides the WITHDRAWAL record)
    Transaction lastTransaction = this.transactionHistory.get(this.transactionHistory.size() - 1);
    lastTransaction.setType(TransactionType.TRANSFER);
    lastTransaction.setDestinationAccount(destinyAccount);
  }

  /** Records a transaction in the account history */
  public void recordTransaction(
      TransactionType type, double amount, BankInterface destinationAccount) {
    Transaction transaction = new Transaction(type, amount, new Date(), this, destinationAccount);
    this.transactionHistory.add(transaction);
  }

  /** Prints the common account information */
  protected void printCommonInfo() {
    System.out.println(String.format("Account holder: %s", this.getClient().getName()));
    System.out.println(String.format("Holder's CPF: %s", this.getClient().getCpf()));
    System.out.println(String.format("Account Number: %d", this.getAccountNumber()));
    System.out.println(String.format("Agency: %s", this.getAgency()));
    System.out.println(String.format("Stored: $%.2f", this.getAmountStored()));
    System.out.println(String.format("Opening Date: %s", this.getOpeningDate()));
  }

  /** Prints transaction history for this account */
  public void printTransactionHistory() {
    System.out.println("\n=== Transaction History ===");
    if (transactionHistory.isEmpty()) {
      System.out.println("No transactions found.");
      return;
    }

    for (Transaction transaction : transactionHistory) {
      System.out.println(transaction);
    }
  }
}
