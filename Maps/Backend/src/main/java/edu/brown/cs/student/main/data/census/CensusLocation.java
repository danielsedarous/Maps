package edu.brown.cs.student.main.data.census;

/**
 * Record used to represent a location to be queried. Allows for flexibility, especially
 * when testing and using mock results, as this allows for streamlined access to a specific
 * location.
 * @param state code to query
 * @param county code to query
 */
public record CensusLocation(String state, String county) {


}
