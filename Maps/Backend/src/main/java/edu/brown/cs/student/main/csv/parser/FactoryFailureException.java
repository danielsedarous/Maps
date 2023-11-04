package edu.brown.cs.student.main.csv.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an error provided to catch any error that may occur when you create an object from a row.
 * Feel free to expand or supplement or use it for other purposes.
 */
public class FactoryFailureException extends Exception {
  final List<String> row;

  /**
   * Exception thrown when an error occurs with the CreatorFromRow object
   *
   * @param message indicating error
   * @param row of error
   */
  public FactoryFailureException(String message, List<String> row) {
    super(message);
    this.row = new ArrayList<>(row);
  }
}
