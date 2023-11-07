package edu.brown.cs.student.main.csv;

import edu.brown.cs.student.main.csv.parser.FactoryFailureException;
import edu.brown.cs.student.main.csv.parser.Parser;
import edu.brown.cs.student.main.csv.parser.RowToList;
import edu.brown.cs.student.main.csv.search.Searcher;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is the Main class of the project. It handles the user-facing logic by interacting with and
 * prompting user inputs.
 */
public final class Main {
    List<String> inputs;

    /**
     * The initial method called when execution begins.
     *
     * @param args An array of command line arguments
     */
    public static void main(String[] args) {
        new Main().run();
    }

    /**
     * The constructor of the Main class, which instantiates a list of user inputs to be used for
     * collecting the arguments that are passed to the run method.
     */
    private Main() {
        this.inputs = new ArrayList<>();
    }

    /**
     * The run() method handles the logic of printing the search results. First, it calls on
     * userPrompts() to populate the inputs list. Then, using the populated list, it instantiates an
     * instance of the Parser and Scanner classes. It indexes into values of the input list and passes
     * them into their corresponding parameters in mainSearcher and mainParser. run() also handles
     * multiple types of exceptions, including FileNotFound, NullPointer, and IndexOutOfBounds by
     * using try-catch statements to communicate to the user where the issue came from and how to
     * avoid it when attempting to run the program again.
     */
    private void run() {
        this.userPrompts();
        Searcher mainSearcher = null;

        try {
            Parser<List<String>> mainParser =
                    new Parser<>(
                            new FileReader(this.inputs.get(0)),
                            new RowToList());

            if (this.inputs.size()
                    == 4) { // case where user has included optional parameter searching in specific column
                mainSearcher = new Searcher(this.inputs.get(2), mainParser.parse(), Boolean.valueOf(this.inputs.get(1)));
            } else {
                mainSearcher = new Searcher(this.inputs.get(2), mainParser.parse(), Boolean.valueOf(this.inputs.get(1)));
            }
        } catch (IOException e) {
            System.err.println("Please input a file that exists using the correct file path!");
        } catch (IndexOutOfBoundsException e) {
            System.err.println(
                    "Please make sure that you are inputting 3 or 4 arguments according "
                            + "to the given prompts: "
                            + "<file path> "
                            + "<true or false (if file contains headers)> <value to search for>"
                            + " <(optional) index or column name to search in>");
        } catch (FactoryFailureException e) {
            System.err.println("Issue found with ");
        }

        try {
            if (mainSearcher
                    .search()
                    .isEmpty()) { // case where search() returns empty list, so no rows matched search query
                System.out.println(
                        "No rows were found to match your query. Please check that you"
                                + "have searched in the correct column or that your value exists in the file.");
            } else {
                System.out.println("Rows found: ");
                for (List<String> sublist : mainSearcher.search()) {
                    System.out.println(sublist);
                }
            }

        } catch (NullPointerException e) {
            System.err.println(
                    "Please make sure that you are inputting 3 or 4 arguments according to the prompts: "
                            + "<file path>"
                            + " <yes or no (if file contains headers)> <value to search for> "
                            + "<(optional) index or column name to search in>");
        }
    }

    /**
     * userPrompts() is responsible for populating the this.inputs list based on the user's responses
     * to different prompts. It collects information regarding the file path, whether the csv contains
     * headers, the value to search for, and an optional column to search in based on an index or
     * name. In order to inform the user of which answers to provide, this method uses a scanner to
     * communicate the information a user should provide (and how to do so), then adds the answer to
     * the inputs list. For most of the prompts, there are checks in place to ensure that the user
     * does not wrongly provide information, although if the user continues to provide wrong
     * information, the run() method will return an informative message addressing the input error
     * after all prompts have been answered.
     */
    private void userPrompts() {
        Scanner mainScanner = new Scanner(System.in);

        System.out.println(
                "You can search for a value in your CSV here. Note that if any of your inputs "
                        + "are incorrect, you will be informed of "
                        + "improper inputs at the end of the prompts."
                        + "\n Enter the file path of your CSV file: ");
        String fileName = mainScanner.nextLine();
        try {
      /*
      verifies that file path is legitimate by passing to an actual FileReader object and
      checking for a FileNotFound exception
       */
            new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.err.println(
                    "Try again! Please input a correct file path, or else your input will not produce "
                            + "results after the prompts: ");
            fileName = mainScanner.nextLine();
        }
        this.inputs.add(fileName);

        System.out.println(
                "Indicate, using EITHER (and only) yes or no, whether or not your file contains headers: ");
        String containsHeaders = mainScanner.nextLine();
        if (containsHeaders
                .toLowerCase()
                .contains("yes")) { // converts from natural, human language to java language
            containsHeaders = "true";
        } else if (containsHeaders.toLowerCase().contains("no")) {
            containsHeaders = "false";
        } else {
            System.out.println(
                    "Please try again and only use yes or np, otherwise you will not receive the proper "
                            + "results.");
            containsHeaders = mainScanner.nextLine();
        }
        this.inputs.add(containsHeaders);

        System.out.println(
                "Name the value you want to search for. If your search contains multiple words, separate "
                        + "using an underscore (i.e. data_type): ");
        String searchValue = mainScanner.nextLine();
        this.inputs.add(searchValue);

        System.out.println("Do you want to search for a specific column index or name (yes or no): ");
        String optionalSearch = mainScanner.nextLine();
        String colSearch;
        if (optionalSearch.toLowerCase().contains("yes")) {
            System.out.println(
                    "Indicate the column index or name you want to search in. If your search contains "
                            + "multiple words, separate using an underscore (i.e. data_type): ");
            colSearch = mainScanner.nextLine();
            this.inputs.add(colSearch);
        }
    }
}
