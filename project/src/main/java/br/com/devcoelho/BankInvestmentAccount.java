package br.com.devcoelho;

import br.com.devcoelho.exceptions.InsufficientBalanceException;
import java.util.HashMap;
import java.util.Map;

/** Investment account with additional features for financial investments */
public class BankInvestmentAccount extends BankAccount {

  private double interestRate = 0.05; // 5% annual interest rate by default
  private Map<String, Investment> investments = new HashMap<>();

  public BankInvestmentAccount(Person client) {
    super(client);
  }

  /**
   * Gets the current annual interest rate
   *
   * @return the interest rate as a decimal (e.g., 0.05 for 5%)
   */
  public double getInterestRate() {
    return interestRate;
  }

  /**
   * Sets the annual interest rate
   *
   * @param interestRate the new interest rate as a decimal
   */
  public void setInterestRate(double interestRate) {
    if (interestRate < 0) {
      throw new IllegalArgumentException("Interest rate cannot be negative");
    }
    this.interestRate = interestRate;
  }

  /**
   * Creates a new investment with the specified amount
   *
   * @param name the name of the investment
   * @param amount the amount to invest
   * @param annualRate the annual interest rate for this specific investment
   */
  public void createInvestment(String name, double amount, double annualRate) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Investment amount must be positive");
    }

    if (amount > getAmountStored()) {
      throw new InsufficientBalanceException("Insufficient balance for investment");
    }

    // Subtract from main balance
    setAmountStored(getAmountStored() - amount);

    // Create and store the investment
    Investment investment = new Investment(name, amount, annualRate);
    investments.put(name, investment);

    // Record transaction
    recordTransaction(TransactionType.WITHDRAWAL, -amount, null);
  }

  /**
   * Liquidates (ends) an investment and returns the money to the main balance
   *
   * @param name the name of the investment to liquidate
   * @return the total amount returned to the account (principal + interest)
   */
  public double liquidateInvestment(String name) {
    Investment investment = investments.get(name);
    if (investment == null) {
      throw new IllegalArgumentException("Investment not found: " + name);
    }

    double totalAmount = investment.getCurrentValue();
    setAmountStored(getAmountStored() + totalAmount);

    // Record transaction for the return of investment
    recordTransaction(TransactionType.DEPOSIT, totalAmount, null);

    // Record transaction for the interest portion
    double interestPortion = totalAmount - investment.getPrincipal();
    if (interestPortion > 0) {
      recordTransaction(TransactionType.INTEREST, interestPortion, null);
    }

    // Remove the investment
    investments.remove(name);

    return totalAmount;
  }

  /**
   * Gets all current investments
   *
   * @return a map of investment name to Investment object
   */
  public Map<String, Investment> getInvestments() {
    return new HashMap<>(investments); // Return a copy to prevent direct modification
  }

  /**
   * Applies interest to the main balance (for non-investment funds) This would typically be called
   * by a scheduled job
   */
  public void applyMonthlyInterest() {
    double interestAmount = (getAmountStored() * interestRate) / 12; // Monthly interest
    setAmountStored(getAmountStored() + interestAmount);
    recordTransaction(TransactionType.INTEREST, interestAmount, null);
  }

  @Override
  public void printExtract() {
    System.out.println("\n=== Investment Account Extract ===");
    super.printCommonInfo();

    System.out.println(String.format("\nBase Annual Interest Rate: %.2f%%", interestRate * 100));

    if (!investments.isEmpty()) {
      System.out.println("\n--- Current Investments ---");
      for (Map.Entry<String, Investment> entry : investments.entrySet()) {
        Investment inv = entry.getValue();
        System.out.println(
            String.format(
                "%s: Initial $%.2f, Current $%.2f (Rate: %.2f%%)",
                entry.getKey(),
                inv.getPrincipal(),
                inv.getCurrentValue(),
                inv.getAnnualRate() * 100));
      }
    }
  }

  /** Inner class representing a single investment */
  public class Investment {
    private final String name;
    private final double principal;
    private final double annualRate;
    private final long startTimeMillis;

    public Investment(String name, double principal, double annualRate) {
      this.name = name;
      this.principal = principal;
      this.annualRate = annualRate;
      this.startTimeMillis = System.currentTimeMillis();
    }

    public String getName() {
      return name;
    }

    public double getPrincipal() {
      return principal;
    }

    public double getAnnualRate() {
      return annualRate;
    }

    /**
     * Calculates the current value of the investment based on time elapsed
     *
     * @return the current value
     */
    public double getCurrentValue() {
      double yearsElapsed =
          (System.currentTimeMillis() - startTimeMillis) / (1000.0 * 60 * 60 * 24 * 365);

      // Compound interest formula: P * (1 + r)^t
      return principal * Math.pow(1 + annualRate, yearsElapsed);
    }
  }
}
