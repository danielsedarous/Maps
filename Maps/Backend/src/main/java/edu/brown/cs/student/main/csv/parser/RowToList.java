package edu.brown.cs.student.main.csv.parser;

import java.util.List;

/**
 * This class implements CreatorFromRow in order to specify how a row (or list of strings) should be
 * converted.
 */
public class RowToList implements CreatorFromRow<List<String>> {

  /** Empty constructor. The only necessary operation from this class is the create method. */
  public RowToList() {}

  /**
   * The create() method simply takes in a list of strings and returns it. This class is used by
   * search to search through each list of strings.
   *
   * @param row list containing strings. Converted into a specified object based on the class
   *     implementing the interface.
   * @return List of Strings - row of data to be searched
   */
  @Override
  public List<String> create(List<String> row) {
    return row;
  }
}
