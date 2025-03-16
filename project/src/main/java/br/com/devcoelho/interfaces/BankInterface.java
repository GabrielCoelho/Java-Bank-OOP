package br.com.devcoelho.interfaces;

/** BankInterface */
public interface BankInterface {

  boolean isValid();

  void withdrawAmount(double value);

  void depositAmount(double value);

  void transferAmount(double value, BankInterface destinyAccount);

  void printExtract();
}
