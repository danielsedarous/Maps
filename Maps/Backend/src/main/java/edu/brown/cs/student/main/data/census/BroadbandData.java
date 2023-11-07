package edu.brown.cs.student.main.data.census;

import java.util.List;

/**
 * Record representing the data returned by API for broadband information. Stored in a record
 * for flexibility and testing purposes, allowing mock data to be passed in as necessary.
 * @param data from API result
 */

public record BroadbandData(List<List<String>> data) {

}
