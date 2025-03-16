package br.com.devcoelho;

import br.com.devcoelho.interfaces.BankInterface;
import java.util.Date;

/** Represents a transaction in the bank system */
public class Transaction {
  private TransactionType type;
  private double amount;
  private Date date;
  private BankInterface sourceAccount;
  private BankInterface destinationAccount;

  public Transaction(
      TransactionType type,
      double amount,
      Date date,
      BankInterface sourceAccount,
      BankInterface destinationAccount) {
    this.type = type;
    this.amount = amount;
    this.date = date;
    this.sourceAccount = sourceAccount;
    this.destinationAccount = destinationAccount;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = type;
  }

  public double getAmount() {
    return amount;
  }

  public Date getDate() {
    return date;
  }

  public BankInterface getSourceAccount() {
    return sourceAccount;
  }

  public BankInterface getDestinationAccount() {
    return destinationAccount;
  }

  public void setDestinationAccount(BankInterface destinationAccount) {
    this.destinationAccount = destinationAccount;
  }

  @Override
  public String toString() {
    String accountInfo = "";

    if (type == TransactionType.TRANSFER && destinationAccount != null) {
      accountInfo =
          String.format(" to Account #%d", ((BankAccount) destinationAccount).getAccountNumber());
    }

    return String.format(
        "[%s] %s: $%.2f%s", date, type.getDescription(), Math.abs(amount), accountInfo);
  }
}
