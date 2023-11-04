package edu.brown.cs.student.main.data.census;

import edu.brown.cs.student.main.exceptions.DatasourceException;

import java.io.IOException;

/**
 * The CensusDataSource interface creates flexibility in testing the BroadbandHandler
 * with the real API or a mock version. The BroadbandHandler uses this method to either
 * pass along data to a mock API or real one.
 */

public interface CensusDataSource {

  /**
   * This method will be used by implementing classes to obtain a location record, identify
   * its codes, and then pass along the information to a source that will return
   * data, either real or mocked.
   *
   * @param loc location record for finding broadband data
   * @return broadband data
   * @throws DatasourceException
   * @throws IOException
   */
  BroadbandData getBroadbandData(CensusLocation loc) throws DatasourceException, IOException;

}
