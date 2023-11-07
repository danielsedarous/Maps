package edu.brown.cs.student.main.csv.search;


import edu.brown.cs.student.main.csv.parser.FactoryFailureException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Searcher class searches a parsed CSV file for rows that contain a specified value.
 */
public class Searcher {

    private String value;

    private int colIndex;
    private String colName;
    private List<List<String>> csv;
    private Boolean hasHeader;

    /**
     * This constructor takes in a value to search for, a parser that handles the file processing, and
     * a column to search in, either by index or column name. It calls on the convertType() method to
     * convert the column search into an int if searching for a column index, or keeping as a string
     * if for a column name.
     *
     * @param value     to search for within csv file, returning rows that contain the value
     * @param parser    object that parses file prior to searching
     * @param colSearch specifying which columnn to search in for a vaue
     */
    public Searcher(String value, List<List<String>> data, String colSearch, Boolean hasHeader) {
        this.value = value;
        this.colName = null;
        this.colIndex = -1;
        this.convertType(colSearch);
        this.csv = data;
        this.hasHeader = hasHeader;
    }

    /**
     * This constructor is the same as the previous, except it excludes the parameter colSearch in
     * case a user does not want to specify a column to search in.
     *
     * @param value  to search for within csv file, returning rows that contain the value
     * @param parser object that parses file prior to searching
     */
    public Searcher(String value, List<List<String>> data, Boolean hasHeader)
            throws IOException, FactoryFailureException {
        this.value = value;
        this.colIndex = -1;
        this.csv = data;
        this.hasHeader = hasHeader;
    }

    /**
     * The search() method handles all the logic for searching through a parsed file and adding
     * results to a list to be returned and reported to the user. First, a list that will contain
     * lists of strings is instantiated, allowing all rows that contains the relevant value to be
     * seen. Then, using for each loops that iterate through each row and column, multiple if-else
     * statements are used to account for different inputs. In the case that the user has provided a
     * column to be searched, and indicated that the file contains a header, the method will find the
     * specific column, then search through each row until finding one that contains the matching
     * value in the given column name, adding the rows to the full list allRows. Otherwise, if an
     * index is provided, the method will find the column corresponding to the index, searching
     * through each row of the column for a value that matches the one given. If neither an index nor
     * column name is provided, the method will search through every row to find any columns that
     * contain the value given. This method works by checking that column names or column values are
     * contained within the file (rather than equal to column names or values in the file), meaning
     * that as long as a column contains the search result (or the search result is a part of the
     * column names/values) it will be considered to match the query and will be added to the list of
     * rows to be returned. This method catches ArrayIndexOutOfBoundsException, then informs the user
     * that they cannot index into columns that don't exist, nor can they try to search for a column
     * name that doesnt exist. The try-catch for IndexOutOfBoundsException is used to inform the user
     * that they should use a file that doesn't contain empty spaces.
     *
     * @return List of list of strings- list of rows that match given value
     */
    public List<List<String>> search() { //TODO: make less ugly?
        List<List<String>> allRows = new ArrayList<>();

        List<String> headerList = null;

        if (this.hasHeader) {
            headerList = this.csv.get(0);
        }

        try {
            for (List<String> currRow : this.csv) {
                for (String currCol : currRow) {

                    if (headerList != null) {
                        if (headerList.size() != currRow.size()) {
                            // prevents searching file with uneven amount of columns in rows
                            throw new IndexOutOfBoundsException();
                        }
                    }

                    if (this.colName != null && headerList != null) {
            /* all words in the csv file or inputted by the user are converted to lowercase to ensure case
            insensitivity. Since the user must input multi-word arguments using underscores, the searcher
            replaces underscores with spaces, ensuring proper matching to the csv file.
             */
                        if (currCol.toLowerCase().contains(this.colName.toLowerCase().replaceAll("_", " "))) {
                            int searchInCol = headerList.indexOf(currCol);
                            if (currCol.toLowerCase().contains(this.colName.toLowerCase().replaceAll("_", " "))) {
                                for (int i = 1; i < this.csv.size(); i++) {
                                    List<String> nextRow = this.csv.get(i);
                                    if (nextRow
                                            .get(searchInCol)
                                            .toLowerCase()
                                            .contains(this.value.toLowerCase().replaceAll("_", " "))) {
                                        if (!allRows.contains(nextRow)) {
                                            allRows.add(nextRow);
                                        }
                                    }
                                }
                            }
                        }

                    } else if (this.colIndex >= 0) {
                        if (currRow
                                .get(this.colIndex)
                                .toLowerCase()
                                .contains(this.value.toLowerCase().replaceAll("_", " "))) {
                            if (!allRows.contains(currRow)) {
                                allRows.add(currRow);
                            }
                        }

                    } else if (this.colName == null) {
                        int searchCurrRow = this.csv.indexOf(currRow);
                        int searchCurrCol = this.csv.get(searchCurrRow).indexOf(currCol);

                        if (this.csv
                                .get(searchCurrRow)
                                .get(searchCurrCol)
                                .toLowerCase()
                                .contains(this.value.toLowerCase().replaceAll("_", " "))) {
                            if (!allRows.contains(currRow)) {
                                allRows.add(currRow);
                            }
                        }
                    } else {
                        System.out
                                .println( // an internal indicator that the search is incorrect, not seen by the
                                        // user
                                        "There was an issue with searching for the given value. Please check that you have provided "
                                                + "either an int OR column name OR neither. If you provided a column name, please only do so"
                                                + " if you have also indicated that the CSV file contains headers, and that it exists in the data.");
                    }
                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(
                    "Please only provide a column index within the length of your data rows, or check that you're "
                            + "searching for a column name that exists in the given headers.");
        } catch (IndexOutOfBoundsException e) {
            System.err.println(
                    "An empty space was detected in your csv file. You cannot search for a column value"
                            + "if the row does not contain one. Please make sure that each row contains"
                            + " values for each column.");
        }

        return Collections.unmodifiableList(allRows);
    }

    /**
     * Method that converts the colSearch into either an index value or a column name to search for.
     * If the string can successfully be converted into an int, the colIndex variable will be updated
     * to this int. Otherwise, the colName variable will be updated to match the string.
     *
     * @param convertTo a string containing either words or an int to indicate whether to search by
     *                  column idnex or name
     */
    public void convertType(String convertTo) {
        try {
            this.colIndex = Integer.parseInt(convertTo);

        } catch (NumberFormatException e) {
            this.colName = convertTo;
        }
    }

}
