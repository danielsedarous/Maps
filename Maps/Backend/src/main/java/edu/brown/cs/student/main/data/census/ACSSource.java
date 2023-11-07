package edu.brown.cs.student.main.data.census;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.exceptions.DatasourceException;
import okio.Buffer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * The ACSSource class represents the logic regarding direct interaction with the Census
 * API. It will receive inputs from the Broadband handler that will be used to generate
 * and collect results.
 */

public class ACSSource implements CensusDataSource{

  /**
   * This method is required to be implemented by the CensusDataSource interface. Since
   * this class is not meant to be a mock, and actually interacts with the API directly,
   * it simply calls on the other getBroadbandData method to pass along the necessary
   * Census location record.
   * @param loc record representing the current state and county
   * @return Data regarding broadband
   * @throws DatasourceException thrown for incorrect data
   * @throws IOException thrown for data issues
   */

  @Override
  public BroadbandData getBroadbandData(CensusLocation loc) throws DatasourceException, IOException {
    return this.getBroadbandData(loc.state(),loc.county());
  }

  /**
   * Given a state code and county code (which are taken from the record fields), this method
   * invokes the census API. First, the url is requested and sent to the client connection, then
   * the result is deserialized into a list of list of string and given as the argument for the
   * BroadbandData (meaning that the value is stored in a data record).
   * @param stateCode the code that the API must use to find the state and narrow results
   * @param countyCode the code that the API must use to find the county and narrow results
   * @return a BroadbandData record instance containing the returned API data
   * @throws DatasourceException
   * @throws IOException
   */
  private BroadbandData getBroadbandData(String stateCode, String countyCode) throws DatasourceException, IOException {
      URL requestUrl =
          new URL("https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + countyCode + "&in=state:" + stateCode);
      HttpURLConnection clientConnection = connect(requestUrl);
      Moshi moshi = new Moshi.Builder().build();
      Type listListStringObject = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListStringObject);
      List<List<String>> input = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      BroadbandData locInfo = new BroadbandData(input);
      clientConnection.disconnect();
      return locInfo;
  }

  /**
   * helper method for creating URL connection
   * @param requestURL url to use in API request
   * @return HTTPURLConnection for finding and connecting to API
   * @throws DatasourceException
   * @throws IOException
   */

  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(! (urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }
}
