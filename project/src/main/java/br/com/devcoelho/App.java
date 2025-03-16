package br.com.devcoelho;

/** Demonstration of improved bank system functionality */
public class App {

  public static void main(String[] args) {
    // Create a bank
    Bank bank = new Bank("DevCoelho Bank", "123");

    // Create clients with addresses
    Person client1 = createClientWithAddress("João Silva", "123.456.789-00", "01311-000");
    Person client2 = createClientWithAddress("Maria Oliveira", "987.654.321-00", "04538-133");

    // Add clients to bank
    bank.addClient(client1);
    bank.addClient(client2);

    // Create accounts for clients
    BankAccount simpleAccount = bank.createAccount(client1, Bank.AccountType.SIMPLE);
    BankInvestmentAccount investmentAccount =
        (BankInvestmentAccount) bank.createAccount(client1, Bank.AccountType.INVESTMENT);
    BankAccount account2 = bank.createAccount(client2, Bank.AccountType.SIMPLE);

    // Perform banking operations
    simpleAccount.depositAmount(1000.0);
    account2.depositAmount(500.0);

    System.out.println("After deposits:");
    simpleAccount.printExtract();
    account2.printExtract();

    simpleAccount.transferAmount(300.0, account2);

    System.out.println("\nAfter transfer:");
    simpleAccount.printExtract();
    account2.printExtract();

    // Test investment account features
    investmentAccount.depositAmount(5000.0);
    investmentAccount.createInvestment("Treasury Bonds", 2000.0, 0.07);
    investmentAccount.createInvestment("Stock Fund", 1500.0, 0.12);

    System.out.println("\nInvestment account status:");
    investmentAccount.printExtract();

    // Simulate interest application
    investmentAccount.applyMonthlyInterest();

    // Liquidate an investment
    double liquidatedAmount = investmentAccount.liquidateInvestment("Treasury Bonds");
    System.out.println("\nLiquidated Treasury Bonds for $" + liquidatedAmount);

    System.out.println("\nFinal investment account status:");
    investmentAccount.printExtract();

    // Print transaction history
    simpleAccount.printTransactionHistory();
    investmentAccount.printTransactionHistory();

    // Show all client accounts
    System.out.println("\nAccounts for " + client1.getName() + ":");
    for (BankAccount account : bank.getClientAccounts(client1)) {
      System.out.println(
          "- Account #"
              + account.getAccountNumber()
              + " ("
              + account.getClass().getSimpleName()
              + ")"
              + " Balance: $"
              + account.getAmountStored());
    }
  }

  /** Helper method to create a client with address */
  private static Person createClientWithAddress(String name, String cpf, String cep) {
    Person person = new Person();
    person.setName(name);
    person.setCpf(cpf);

    Address address = new Address();
    // Use ViaCEP to populate address fields
    address.validateAndFillAddressByCep(cep);
    address.setHouseNumber("123");

    AddressType addressType = new AddressType();
    addressType.setAddressType("RESIDENTIAL");
    addressType.setAddressLocationType("HOME");
    address.setAddressLocationT(addressType);

    person.getAddress().add(address);

    return person;
  }
}
