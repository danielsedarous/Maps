package edu.brown.cs.student.main.data.census.mocks;

import edu.brown.cs.student.main.data.census.BroadbandData;
import edu.brown.cs.student.main.data.census.CensusDataSource;
import edu.brown.cs.student.main.data.census.CensusLocation;

/**
 * This class is responsible for serving as a data source that does not require calling the census
 * API - it implements the CensusDataSource interface, such that it can be passed into the
 * BroadbandHandler class in place of an instance of the ACSSource class
 */
public class MockedACSSource implements CensusDataSource {

        private final BroadbandData constantData;

        public MockedACSSource(BroadbandData constantData) {
            this.constantData = constantData;
        }

  /**
   * This method overrides from the CensusDataSource interface and returns the constant data
   * that is stored within this class.
   * @param loc location record for finding broadband data
   * @return
   */
  @Override
    public BroadbandData getBroadbandData(CensusLocation loc) {
        return this.constantData;
    }
}
