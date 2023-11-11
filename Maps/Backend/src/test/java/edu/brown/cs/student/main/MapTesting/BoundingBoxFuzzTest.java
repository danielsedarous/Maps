package edu.brown.cs.student.main.MapTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.MapsBoundingHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * This class fuzz tests our bounding box.
 */
public class BoundingBoxFuzzTest {

  private final Type mappingObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * We want this to run before all our tests, so we can establish a port.
   */
  @BeforeAll
  public static void setup_before_once() {
    // arbitrary available port.
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * We want to run this setup before each test, so we can set up a search for boundingbox.
   * */

  @BeforeEach
  public void setup() {
//    Spark.get("mapsKeyWord", new MapsAreaKeyWordHandler());
    Spark.get("/mapsBoundingBox", new MapsBoundingHandler());
    Spark.init();
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mappingObject);
  }

  /**
   * After the test is completed we want to disconnect Spark from the endpoints
   */
  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/mapsBoundingBox");
//    Spark.unmap("geoJSON");
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
   * This is our fuzz tests which randomly generates coordinates between the provided bounds
   * The test then runs 99 times and will check to see if those random coords are in our boundingbox
   * to ensure that boundingbox functionality is proper for the most random of cases. We check this
   * by seeing if the result of the search for the random coords are successful.
   * @throws IOException- If the inputs fail for some reason.
   */
  @Test
  public void testRandomBounds() throws IOException {
    final double latMinCap = -90;
    final double latMaxCap = 90;
    final double lonMinCap = -180;
    final double lonMaxCap = 180;
    final int maxIterations = 100;

    Random random = new Random();

    for (int intTest = 0; intTest < maxIterations; intTest++) {
      double randomLatValue1 = latMinCap + (latMaxCap - latMinCap) * random.nextDouble();
      double randomLatValue2 = latMinCap + (latMaxCap - latMinCap) * random.nextDouble();

      double randomLonValue1 = lonMinCap + (lonMaxCap - lonMinCap) * random.nextDouble();
      double randomLonValue2 = lonMinCap + (lonMaxCap - lonMinCap) * random.nextDouble();

      double latMin = Math.min(randomLatValue1, randomLatValue2);
      double latMax = Math.max(randomLatValue1, randomLatValue2);
      double lonMin = Math.min(randomLonValue1, randomLonValue2);
      double lonMax = Math.max(randomLonValue1, randomLonValue2);
      HttpURLConnection loadConnection = tryRequest("mapsBoundingBox?lowerLatitude=" + latMin + "&upperLatitude="
          + latMax + "&lowerLongitude=" + lonMin + "&upperLongitude=" + lonMax);
      Map<String, Object> body =
          adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
      assertEquals(200, loadConnection.getResponseCode());
      assertEquals("success", body.get("type"));
    }
  }
}
