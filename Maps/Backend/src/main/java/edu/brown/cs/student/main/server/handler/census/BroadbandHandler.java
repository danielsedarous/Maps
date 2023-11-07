package edu.brown.cs.student.main.server.handler.census;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.data.census.BroadbandData;
import edu.brown.cs.student.main.data.census.CensusDataSource;
import edu.brown.cs.student.main.data.census.CensusLocation;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.brown.cs.student.main.exceptions.DatasourceException;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * The BroadbandHandler interacts with user queries. It converts queries into necessary information,
 * passing it to the ACSSource, which more closely handles direct API interaction. In order to
 * properly handle and send user inputs, it implements Route and uses its handle method.
 */

public class BroadbandHandler implements Route {

  //a class that implements CensusDataSource that will interact (or fake interact) with the API
  private final CensusDataSource source;

  //Hashmap used to pair state names to their codes in the census API
  private final Map<String, String> stateCodes;

  /**
   * The constructor takes in a source that will call on its own implementation of
   * returning the necessary census data. It also instantiates the map that will
   * map all state names to their codes. This is performed here to prevent constantly
   * fetching the data for the state codes.
   * @param source that will obtain data results
   * @throws DatasourceException
   * @throws IOException
   */
  public BroadbandHandler(CensusDataSource source) throws DatasourceException, IOException {
    this.source = source;
    this.stateCodes = this.obtainStateCodes();
  }

  /**
   * This method is used to obtain the state codes necessary for requesting broadband data
   * in a specific state. It first fetches the results from an API that lists each state and their
   * code, then deserializes it so that each state name and code can be deciphered and paired
   * into the map.This method uses case insensitivity when searching through state names.
   * @return
   * @throws IOException
   * @throws DatasourceException
   */

  public Map<String, String> obtainStateCodes() throws IOException, DatasourceException {
      URL requestUrl = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientConnection = connect(requestUrl);
      Moshi moshi = new Moshi.Builder().build();
      Type listListStringObject = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListStringObject);
      List<List<String>> stateCodesList = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      Map<String, String> stateCodesMap = new HashMap<>();
      for (List<String> state : stateCodesList) {
        stateCodesMap.put(state.get(0).toLowerCase(), state.get(1));
      }
      return stateCodesMap;
  }

  /**
   *Similar to obtaining the state code, this method uses deserialization to convert
   * a county name into a code that the census API will recognize. It also fetches
   * the API containing code data, but this time in a specific state. The result is deserialized,
   * leaving a list of list of strings as a result. Then, the inputted county name is matched to
   * itself in the list, allowing the code to be returned. This method uses case insensitivity
   * when searching through county names.
   * @param stateCode
   * @param countyName
   * @return
   * @throws IOException
   * @throws DatasourceException
   */

  public String obtainCountyCode(String stateCode, String countyName) throws IOException, DatasourceException {
    URL requestUrl = new URL("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
    HttpURLConnection clientConnection = connect(requestUrl);
    Moshi moshi = new Moshi.Builder().build();
    Type listListStringObject = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListStringObject);
    List<List<String>> countyCodesList = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    String countyCode = null;
    for (List<String> county : countyCodesList) {
      if (county.get(0).toLowerCase().contains(countyName.toLowerCase())){
        countyCode = county.get(2);
      }
    }
    return countyCode;
  }

  /**
   * As a class implementing Route, handle is used to obtain the broadband-specific
   * query parameters (the state and county names to receive data for), passing the result
   * to a different state class. Handle first documents the current date and time, which will be
   * used in part with the hashmap of handler results. a Moshi instance builds and adapts to
   * take on a Map<String,Object> format, since this is what the response will use. For a
   * successful query, the handler will convert the state names to codes, passing those
   * to a CensusLocation record, which is then passed to the state (API interaction) class.
   * For an unsuccessful one, (such as when the state, or code, or both were not included
   * in query parameters) an informative message will be included in the response map.
   * @param request information to be obtained
   * @param response
   * @return serialized map of API data results
   */

  @Override
  public Object handle(Request request, Response response){
    String targetState = request.queryParams("state");
    String targetCounty = request.queryParams("county");

    Date retrieved = Calendar.getInstance().getTime();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    String strDate = dateFormat.format(retrieved);

    Map<String, Object> responseMap = new HashMap<>();
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    responseMap.put("retrieved", strDate);
    try{
      String stateCode = this.stateCodes.get(targetState.toLowerCase());
      String countyCode = this.obtainCountyCode(stateCode, targetCounty.toLowerCase());

      CensusLocation queryArea = new CensusLocation(stateCode,countyCode);
      BroadbandData countyData = this.source.getBroadbandData(queryArea);
      responseMap.put("type", "success");
      responseMap.put("data", countyData.data());


      return adapter.toJson(responseMap);

      } catch(Exception e){
        responseMap.put("type", "error");
        responseMap.put("error_type", "error_datasource");

        if(targetCounty == null || targetCounty.isEmpty() || targetState == null || targetState.isEmpty()) {
          responseMap.put("details", "Please input a state AND county name.");
        } else{
          responseMap.put("details", e.getMessage());
        }

      return adapter.toJson(responseMap);
      }
  }

  /**
   *
   *  helper method for creating URL connection
   *  @param requestURL url to use in API request
   *  @return HTTPURLConnection for finding and connecting to API
   *  @throws DatasourceException
   *  @throws IOException
   *
   * @param requestURL API to be connected to
   * @return conenction for obtaining data
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
