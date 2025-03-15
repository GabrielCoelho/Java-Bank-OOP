package br.com.devcoelho;

import br.com.devcoelho.interfaces.*;

/** BankAccount */
public abstract class BankAccount implements BankInterface {

  private static final String DEFAULT_AGENCY = "Mogi Guacu";
  private static int PRIMARY_KEY_SEQUENTIAL = 1;

  protected String agency;
  protected int accountNumber;
  protected double amountStored;
  protected Person client;

  public BankAccount(Person personToCreateAccount) {
    this.agency = BankAccount.DEFAULT_AGENCY;
    this.accountNumber = PRIMARY_KEY_SEQUENTIAL++;
    this.client = personToCreateAccount;
  }

  public static String getDefaultAgency() {
    return DEFAULT_AGENCY;
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

  public void setAccountNumber(int accountNumber) {
    this.accountNumber = accountNumber;
  }

  public double getAmountStored() {
    return amountStored;
  }

  public void setAmountStored(double amountStored) {
    this.amountStored = amountStored;
  }

  public Person getClient() {
    return client;
  }
}
