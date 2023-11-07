package edu.brown.cs.student.main.csv.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The CSVParser class handles all the logic regarding parsing the files. It stands as a generic
 * class to allow those accessing the class to parse csv rows into any specified data object.
 */
public class Parser<T> {

  private Reader readerObj;

  private CreatorFromRow<T> converter;


  /**
   * The constructor for the parser takes in any Reader object (allowing the flexibility to parse
   * any file), a class implementing the CreatorFromRow interface to specify what each row should be
   * converted to, and a boolean indicating whether there are headers for the parser to consider
   * while processing the csv.
   *
   * @param readerObj Any object that extends the Reader class can be parsed
   * @param convertType A class that contains a create() method specifying what data object each row
   *     should be converted to
   */
  public Parser(Reader readerObj, CreatorFromRow<T> convertType) {
    this.readerObj = readerObj;
    this.converter = convertType;
  }

  /**
   * The parse() method handles all the data parsing and processing into rows. First, it creates a
   * list that will store each row after it has been parsed. Using a BufferedReader that wraps
   * around the given Reader object, each line will be read and passed through the regular
   * expression so that the strings can be properly processed for searching. Then, each line is
   * passed to the convertType class, allowing the row to be converted to a specified data object.
   * Each parsed row is added to a list that will allow access to all rows within one structure.
   * parse() also catches IOException and FactoryFailureException to allow those accessing parse to
   * understand what might have went wrong with the given inputs. Defensive programming was
   * used to wrap the result in an unmodifiable list.
   *
   * @return List(T), a generic for any class implementing CreaterFromRow to specify what a list of
   *     strings (row) should be converted into.
   */
  public List<T> parse() throws IOException, FactoryFailureException {
    List<T> parsedRows = new ArrayList<>();


      BufferedReader bReader = new BufferedReader(this.readerObj);
      String line = bReader.readLine();


      while (line != null) {
        List<String> parsedLine =
            List.of(line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))"));
        T object = this.converter.create(parsedLine);
        parsedRows.add(object);


        line = bReader.readLine();
      }

    return Collections.unmodifiableList(parsedRows);
  }



}
