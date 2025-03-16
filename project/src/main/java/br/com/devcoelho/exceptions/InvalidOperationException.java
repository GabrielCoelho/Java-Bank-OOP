package br.com.devcoelho.exceptions;

/** Exception thrown when attempting invalid operations on an account */
public class InvalidOperationException extends RuntimeException {
  public InvalidOperationException(String message) {
    super(message);
  }
}
