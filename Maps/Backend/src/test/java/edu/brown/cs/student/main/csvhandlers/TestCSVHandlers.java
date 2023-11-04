package edu.brown.cs.student.main.csvhandlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.data.csv.proxy.CsvData;
import edu.brown.cs.student.main.server.handler.csv.LoadHandler;
import edu.brown.cs.student.main.server.handler.csv.SearchHandler;
import edu.brown.cs.student.main.server.handler.csv.ViewHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class handles all testing related to the load, search, and view handlers.
 */
public class TestCSVHandlers {

  /**
   * This sets up an arbitrary port that will be passed into Spark for testing.
   */

  @BeforeAll
  public static void setupOnce(){
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  //shared state data containing parsed information, shared between handlers
  private final CsvData data = new CsvData();

  //moshi used for building and adapting when handling JSON files
  private Moshi moshi;

  //object to handle serialization and deserialization
  private Type mapStringObject;

  //adapter for converting to and from JSON object
  private JsonAdapter<Map<String,Object>> adapter;

  //result of handler response
  private Map<String,Object> output;

  /**
  Used to ensure that data for loadhandlr, searchhandler, and viewhandler tests is unaffected
  by previous tests (unless desired). The shared state data is cleared, and new instances are
   created for each handler, passing the cleared data.
   */
  @BeforeEach
  public void refreshData(){
    this.data.clearData();

    Spark.get("/load", new LoadHandler(this.data));
    Spark.get("/view", new ViewHandler(this.data));
    Spark.get("/search", new SearchHandler(this.data));

    Spark.init();
    Spark.awaitInitialization();
  }


  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/load");
    Spark.unmap("/view");
    Spark.unmap("/search");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Connection method that handles connecting to URL and returning result
   * @param apiCall specific endpoint name of handler to use
   * @param filePath, mostly used for load, ensures files are properly loaded for handler
   * @return connection
   * @throws IOException
   */

  static private HttpURLConnection tryRequest(String apiCall, String filePath) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall+"?filePath="+filePath);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Another setup method that instantiates the necessary objects (moshi, the adapter, and
   * deserialized result) to be consistently used throughout test suite.
   * @param clientConnection for obtaining response from API
   * @throws IOException
   */

  private void setUp(HttpURLConnection clientConnection) throws IOException {
    this.moshi = new Moshi.Builder().build();
    this.mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    this.adapter = moshi.adapter(mapStringObject);
    this.output =  adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
  }

  /**
   * These tests check that the loadhandler works with valid file paths. It checks that the "type"
   * from the load response map is success, and that loading a new file clears the old one,
   * even if the input wasn't an actual file.
   * @throws IOException
   */

  @Test
  public void testLoadHandlerCorrect() throws IOException {
    //tests on RI_Data csv file with correct inputs
    HttpURLConnection clientConnection = tryRequest("load",
        "/Users/kamrynwalker/cs32/server-KammieD-francesca-elia/src/main/java/edu/brown/cs/student/main/data/csv/income_by_race_edited.csv");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", this.output.get("type"));
    assertEquals(
        "/Users/kamrynwalker/cs32/server-KammieD-francesca-elia/src/main/java/edu/brown/cs/student/main/data/csv/income_by_race_edited.csv",
        this.output.get("file path"));

    //new load is incorrect (even though previously loaded), should produce error
    clientConnection = tryRequest("load",null);

    this.setUp(clientConnection);
    List<List<String>> firstLoadedFile = this.data.getProxyData();

    assertEquals("OK",clientConnection.getResponseMessage());
    assertEquals("error", this.output.get("type"));


    //replaces current loaded file, should successfully load and update data variable
    clientConnection = tryRequest("load",
        "/Users/kamrynwalker/cs32/server-KammieD-francesca-elia/src/main/java/edu/brown/cs/student/main/data/csv/RI_data.csv");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);
    List<List<String>> secondLoadedFile = this.data.getProxyData();

    assertEquals("success", this.output.get("type"));
    assertEquals(
        "/Users/kamrynwalker/cs32/server-KammieD-francesca-elia/src/main/java/edu/brown/cs/student/main/data/csv/RI_data.csv",
        this.output.get("file path"));
    assertNotEquals(firstLoadedFile,secondLoadedFile); // test that two different datasets were loaded
  }

  /**
   * This test checks for incorrect inputs, ensuring that "type" "error" or details of the error
   * are given.
   * @throws IOException
   */

  @Test
  public void testLoadHandlerFail() throws IOException {
    HttpURLConnection clientConnection = tryRequest("load","fake_path");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("error", this.output.get("type"));
    assertEquals("error_datasource", this.output.get("error_type"));
  }

  /**
   * This tests that an entire parsed csv file is returned given that the file
   * was first loaded. It checks for a success message, and that the data from the
   * shared state matches the output of the search result map.
   * @throws IOException
   */

  @Test
  public void testViewCorrect() throws IOException {
    this.testLoadHandlerCorrect();

    HttpURLConnection clientConnection = tryRequest("view","");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", this.output.get("type"));
    assertEquals(this.data.getProxyData(), this.output.get("data"));
  }

  /**
   * This test checks that the type of result is an error, given that no file was previously
   * loaded before attempting to view.
   * @throws IOException
   */

  @Test
  public void testViewFailed() throws IOException {
    HttpURLConnection clientConnection = tryRequest("view","");

    this.setUp(clientConnection);

    assertEquals("error", this.output.get("type"));;
  }

  /**
   * This tests that the searchhandler correctly returns a row given a search value and
   * column to search in. It checks for the correct return rows and a sucess response from the search
   * result map.
   * @throws IOException
   */


  @Test
  public void testSearchCorrectWithHeaderIndex() throws IOException {
    this.testLoadHandlerCorrect();

    HttpURLConnection clientConnection = tryRequest("search?target=Providence&column=0&header=true","");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", this.output.get("type"));
    List<List<String>> expectedOutput = List.of(
            List.of("East Providence", "\"65,016.00\"","\"93,935.00\"","\"38,714.00\""),
            List.of("North Providence", "\"68,821.00\"","\"82,117.00\"","\"35,843.00\""),
            List.of("Providence","\"55,787.00\"","\"65,461.00\"","\"31,757.00\""));
    assertEquals(expectedOutput, this.output.get("data"));
  }


  /**
   * This tests for searching in a file for a file that is not present within it. While it will
   * successfully search through the data (and return a type indicating that), but will
   * inform the user that no results were found.
   * @throws IOException
   */

  @Test
  public void testSearchNotPresent() throws IOException {
    this.testLoadHandlerCorrect();

    HttpURLConnection clientConnection = tryRequest("search?target=incorrect&header=true","");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", this.output.get("type"));
    String result = (String) this.output.get("data");
    assertTrue(result.contains("No results were found."));
  }

  /**
   * This tests the result of searching in an unloaded file. The output type will be an error.
   * @throws IOException
   */


  @Test
  public void testSearchFailedNotLoaded() throws IOException {
    HttpURLConnection clientConnection = tryRequest("search?target=Providence&header=true","");

    this.setUp(clientConnection);

    assertEquals("error", this.output.get("type"));
    assertEquals("error_bad_json", this.output.get("error_type"));
  }

  /**
   * This test searches for badly formed query parameters. The expected output will be an
   * error and error type of bad json.
   * @throws IOException
   */

  @Test
  public void testSearchFailedBadInput() throws IOException {
    this.testLoadHandlerCorrect();

    HttpURLConnection clientConnection = tryRequest("search","");

    this.setUp(clientConnection);

    assertEquals("error", this.output.get("type"));
    assertEquals("error_bad_json", this.output.get("error_type"));
  }

  /**
   * This tests for searches that are out of the bounds of the file. The expected output will be an
   *    * error and error type of bad json.
   * @throws IOException
   */

  @Test
  public void testSearchIndexOutOfBounds() throws IOException {
    this.testLoadHandlerCorrect();
    HttpURLConnection clientConnection = tryRequest("search?target=providence&column=50&header=true","");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", output.get("type"));
    String result = (String) output.get("data");
    assertTrue(result.contains("No results were found."));
  }

  /**
   * This tests that searchhandler can search in all columns. There will be a success response.
   * @throws IOException
   */

  @Test
  public void testSearchAllCol() throws IOException {
    this.testLoadHandlerCorrect();
    HttpURLConnection clientConnection = tryRequest("search?target=providence&header=true","");
    assertEquals("OK",clientConnection.getResponseMessage());

    this.setUp(clientConnection);

    assertEquals("success", this.output.get("type"));
  }
}
