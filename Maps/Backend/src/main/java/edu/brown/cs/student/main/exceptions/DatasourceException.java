package edu.brown.cs.student.main.exceptions;

/**
 * This class contains a useful, custom datasource exception that can be used
 * to indicate issues with data processing.
 */
public class DatasourceException extends Exception{

    //variable representing the initiating error (if there is one)
    private final Throwable cause;

    /**
     * This constructor allows for a custom message to be made, and any possible cause errors
     * to be ignored.
     * @param message indicating issue
     */
    public DatasourceException(String message) {
        super(message);
        this.cause = null;
    }

    /**
     * This constructor stores a message and other initiating error.
     * @param message indicating issue
     * @param cause relevant error
     */
    public DatasourceException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
}
