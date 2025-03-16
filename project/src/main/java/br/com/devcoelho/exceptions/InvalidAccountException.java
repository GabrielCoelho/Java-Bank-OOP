package br.com.devcoelho.exceptions;

/** Exception thrown when attempting to operate on an invalid account */
public class InvalidAccountException extends RuntimeException {
  public InvalidAccountException(String message) {
    super(message);
  }
}
