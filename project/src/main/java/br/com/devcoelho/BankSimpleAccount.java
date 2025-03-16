package br.com.devcoelho;

/** BankSimpleAccount */
public class BankSimpleAccount extends BankAccount {

  public BankSimpleAccount(Person client) {
    super(client);
  }

  @Override
  public void printExtract() {
    System.out.println("=== Account Extract ===");
    super.printCommonInfo();
  }
}
