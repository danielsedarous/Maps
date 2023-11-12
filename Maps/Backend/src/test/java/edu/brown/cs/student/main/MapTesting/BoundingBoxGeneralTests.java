package edu.brown.cs.student.main.MapTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.maps.handlers.MapsBoundingHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * This is the test class for when a user searches for a specific latitude
 * or longitude bound. We are mostly error testing in this class as we fuzz
 * tested which handles several working cases.
 */
public class BoundingBoxGeneralTests {
  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * We want this to run before all our tests, so we can establish a port.
   */
  @BeforeAll
  public static void setup_before_everything() {
    // arbitrary available port.
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We need to be able to mutate it (adding recipes etc.) but never
   * need to replace the reference itself. We clear this state out after every test runs.
   */

  @BeforeEach
  public void setup() {
    Spark.get("/mapsBoundingBox", new MapsBoundingHandler());
    Spark.init();
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  /**
   * After the test is completed we want to disconnect Spark from the endpoints
   */
  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/mapsBoundingBox");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Use this to build the URL connection without sending the request in
   * @param apiCall- The queries we would like to input for our search
   * @return the connection after searching
   * @throws IOException - Error with any connectivity
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    // clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * This tests the correct error response is returned if there
   * are no lat and long queries
   * @throws IOException
   */
  @Test
  public void testNoLatLongParameters() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsBoundingBox?efjkwefnew");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("Search query must include: 'lowerLatitude', 'upperLatitude', 'lowerLongitude', 'upperLongitude'",
        body.get("error_description"));
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("incorrect query format", body.get("error_type"));
  }

  /**
   * This method tests if no bounds are entered then the whole dataset gets returned
   *
   * @throws IOException
   */
  @Test
  public void testEmptyLatLongBounds() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsBoundingBox?lowerLatitude=&upperLatitude=&lowerLongitude=&upperLongitude=");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("please make sure you inputted values for all your bounds", body.get("error_description"));
  }

  /**
   * This method tests if there is no int for one or more of the
   * lat long queries
   * @throws IOException
   */
  @Test
  public void oneOrMoreEmptyLatLong() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=110&upperLongitude=");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("please make sure you inputted values for all your bounds", body.get("error_description"));

//    assertTrue(body.get("error").toString().contains(" One of the community's newest localities. Large lots; well landscaped. Layout of community adds charm and appeal. Community stores. Schools."));
  }

  /**
   * This method tests a random set of ints for lat and long works
   * @throws IOException
   */
  @Test
  public void randomWorkingTest() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=110&upperLongitude=34");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("success", body.get("type"));
  }

  /**
   * This method tests that a invalid (non-double) value inputted for the upper latitude returns a descriptive error
   * @throws IOException
   */
  @Test
  public void testInvalidLatMax() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=ufwifwe&lowerLongitude=110&upperLongitude=155");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("min and max latitude and longitude must be valid double values", body.get("error_description"));
  }

  /**
   * This method tests that a invalid (non-double) value inputted for the upper longitude returns a descriptive error
   * @throws IOException
   */
  @Test
  public void testInvalidLongMax() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=110&upperLongitude=ferioief");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("min and max latitude and longitude must be valid double values", body.get("error_description"));
  }
  /**
   * This method tests that a invalid (non-double) value inputted for the lower latitude returns a descriptive error
   * @throws IOException
   */
  @Test
  public void testInvalidLatMin() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=UWEIF&upperLatitude=55&lowerLongitude=110&upperLongitude=178");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("min and max latitude and longitude must be valid double values", body.get("error_description"));
  }

  /**
   * This method tests that a invalid (non-double) value inputted for the lower longitude returns a descriptive error
   * @throws IOException
   */
  @Test
  public void testInvalidLongMin() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=fdsfdssfd&upperLongitude=150");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("min and max latitude and longitude must be valid double values", body.get("error_description"));
  }

  /**
   * This method tests that an inputted upper latitude value greater than 90 returns a descriptive error message
   * @throws IOException
   */
  @Test
  public void testOutOfRangeLatMax() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=333&lowerLongitude=110&upperLongitude=155");

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("latitude must be between -90 and 90 and longitude must be between -180 and 180", body.get("error_description"));
  }

  /**
   * This method tests that an inputted upper longitude value greater than 180 returns a descriptive error message
   * @throws IOException
   */
  @Test
  public void testOutOfRangeLongMax() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=110&upperLongitude=1212");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("latitude must be between -90 and 90 and longitude must be between -180 and 180", body.get("error_description"));
  }

  /**
   * This method tests that an inputted lower latitude value less than -90 returns a descriptive error message
   * @throws IOException
   */
  @Test
  public void testOutOfRangeLatMin() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=3432&upperLatitude=55&lowerLongitude=110&upperLongitude=178");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("latitude must be between -90 and 90 and longitude must be between -180 and 180", body.get("error_description"));
  }

  /**
   * This method tests that an inputted lower longitude value less than -180 returns a descriptive error message
   * @throws IOException
   */
  @Test
  public void testOutOfRangeLongMin() throws IOException {
    HttpURLConnection loadConnection = tryRequest(
        "mapsBoundingBox?lowerLatitude=45&upperLatitude=55&lowerLongitude=-22222&upperLongitude=150");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("latitude must be between -90 and 90 and longitude must be between -180 and 180", body.get("error_description"));
  }
}
