package edu.brown.cs.student.main.csv.parser;

import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 */
public interface CreatorFromRow<T> {

  /**
   * Method that returns a generic object T. Based on the class that implements the interface, the
   * create method will convert a row into a specified object.
   *
   * @param row list containing strings. converted into a specified object based on the class
   *     implementing the interface.
   * @return the converted list of strings into another data object.
   * @throws FactoryFailureException indicated an error with converting a row into another object.
   */
  T create(List<String> row) throws FactoryFailureException;
}
