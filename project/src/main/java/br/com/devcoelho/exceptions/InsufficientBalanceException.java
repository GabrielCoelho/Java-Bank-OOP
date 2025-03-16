package br.com.devcoelho.exceptions;

/** Exception thrown when attempting to withdraw more than available balance */
public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException(String message) {
    super(message);
  }
}
