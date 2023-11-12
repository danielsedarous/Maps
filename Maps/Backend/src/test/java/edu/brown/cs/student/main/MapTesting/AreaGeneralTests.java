package edu.brown.cs.student.main.MapTesting;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.MapsAreaKeyWordHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * This is the testing class for Search. We test the query parameters
 * as well as different cities.
 */
public class AreaGeneralTests {


  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;
  @BeforeAll
  public static void setup_before_everything() {
    // arbitrary available port.
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We need to be able to mutate it (adding recipes etc.) but never need to replace
   * the reference itself. We clear this state out after every test runs.
   */

  @BeforeEach
  public void setup() {
    Spark.get("mapsKeyWord", new MapsAreaKeyWordHandler());
    Spark.init();
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("mapsKeyWord");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

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
   * This method tests if the area query parameter is not inputted
   * @throws IOException
   */
  @Test
  public void testNoAreaParameter() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?feufiwef");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("failure", body.get("type"));
    assertEquals("The search query must be formatted as 'mapsKeyWord?Area=[area description]'", body.get("error_description"));
  }

  @Test
  public void testSearchHistory() throws IOException{
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?Area=Boston");
       Map<String,Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
  Map<String,Object> searchHistory = new HashMap<>();
  searchHistory.put("Boston",body.get("data") );
  assertTrue(searchHistory.get("Boston").toString().contains("Boston"));
    HttpURLConnection loadConnection2 = tryRequest("mapsKeyWord?Area=Providence");
    Map<String,Object> body2 = adapter.fromJson(new Buffer().readFrom(loadConnection2.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    searchHistory.put("Providence",body2.get("data") );
    assertTrue(searchHistory.get("Providence").toString().contains("Providence"));
  }

  /**
   * This method tests if the inputted area is not in the dataset
   * @throws IOException
   */
  @Test
  public void testNoAreaInDataset() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?Area=786218937982178");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("There were no areas that matched this area description", body.get("error_description"));
  }

  /**
   * This method tests if no key word for area is entered then
   * the whole dataset is returned
   * @throws IOException
   */
  @Test
  public void testNoKeyword() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?Area=");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("error_bad_request", body.get("type"));
    assertEquals("Please input an area key word", body.get("error_description"));
  }

  /**
   * This method tests a successful search response
   * @throws IOException
   */
  @Test
  public void testMapsSearchHandler() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?Area=Boston");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    String geometryFeature = "features\":[{\"geometry\":{\"coordinates\":[[[[-73.458822,41.074694],[-73.457099,41.074559],[-73.453741,41.074918],[-73.450741,41.07503],[-73.448423,41.074918],[-73.447116,41.074111],[-73.445928,41.073776],[-73.445333,41.073887],[-73.445779,41.073036],[-73.445601,41.072051],[-73.444769,41.071379],[-73.443937,41.070528],[-73.444888,41.069117],[-73.445957,41.068109],[-73.446552,41.067011],[-73.44673,41.065802],[-73.446076,41.064869],[-73.445886,41.063883],[-73.44629,41.062951],[-73.447288,41.062557],[-73.448263,41.061876],[-73.449214,41.061159],[-73.450616,41.060442],[-73.450972,41.059725],[-73.450616,41.059456],[-73.450307,41.059492],[-73.449285,41.060102],[-73.447811,41.060962],[-73.446765,41.06175],[-73.445577,41.061983],[-73.445458,41.061643],[-73.445862,41.060621],[-73.445862,41.059905],[-73.445411,41.059295],[-73.445316,41.058901],[-73.446124,41.058292],[-73.446813,41.058184],[-73.447241,41.057521],[-73.448324,41.057204],[-73.449346,41.05724],[-73.450368,41.057079],[-73.450582,41.057455],[-73.451153,41.057939],[-73.451794,41.0581],[-73.452175,41.057832],[-73.45246,41.057473],[-73.452555,41.056935],[-73.453363,41.056452],[-73.453197,41.056147],[-73.452674,41.056165],[-73.452198,41.056308],[-73.452222,41.055914],[-73.453434,41.05534],[-73.454528,41.055394],[-73.455193,41.054964],[-73.456191,41.053889],[-73.457261,41.053709],[-73.457522,41.05344],[-73.457475,41.052974],[-73.458592,41.052849],[-73.4594,41.052652],[-73.4594,41.052885],[-73.460232,41.052921],[-73.46123,41.052598],[-73.46142,41.052741],[-73.460969,41.053172],[-73.460375,41.053906],[-73.460636,41.05405],[-73.460779,41.054337],[-73.46066,41.054588],[-73.460256,41.054588],[-73.459709,41.054803],[-73.459828,41.055071],[-73.459471,41.055555],[-73.458735,41.055914],[-73.457522,41.056613],[-73.45719,41.057043],[-73.456952,41.058082],[-73.457736,41.057903],[-73.45814,41.057455],[-73.45883,41.057276],[-73.458806,41.057581],[-73.458212,41.058692],[-73.45776,41.058674],[-73.457023,41.058656],[-73.456049,41.058979],[-73.455288,41.059731],[-73.454314,41.060592],[-73.454052,41.061452],[-73.453316,41.062061],[-73.452745,41.062473],[-73.452484,41.063548],[-73.452793,41.064301],[-73.453102,41.063925],[-73.453791,41.062473],[-73.453957,41.062097],[-73.455027,41.061255],[-73.456477,41.060412],[-73.45738,41.060126],[-73.457546,41.060197],[-73.458188,41.059893],[-73.458711,41.059355],[-73.459091,41.059337],[-73.459448,41.059283],[-73.460066,41.059104],[-73.460731,41.058764],[-73.461135,41.058638],[-73.461302,41.058871],[-73.461492,41.058835],[-73.462157,41.058853],[-73.462133,41.059265],[-73.461943,41.059713],[-73.461753,41.060484],[-73.461896,41.060824],[-73.462038,41.060663],[-73.462846,41.060323],[-73.464035,41.059964],[-73.464296,41.059606],[-73.46413,41.059158],[-73.464106,41.058584],[-73.464463,41.058298],[-73.465152,41.058047],[-73.465913,41.057957],[-73.466412,41.057616],[-73.466673,41.05767],[-73.466911,41.057903],[-73.466958,41.058405],[-73.46634,41.05862],[-73.465675,41.058925],[-73.46577,41.059355],[-73.466507,41.05957],[-73.466935,41.059695],[-73.466935,41.060126],[-73.46703,41.060538],[-73.467529,41.060108],[-73.46779,41.059642],[-73.468384,41.059283],[-73.468693,41.059176],[-73.46886,41.057903],[-73.469335,41.057616],[-73.469739,41.057545],[-73.470666,41.057079],[-73.471284,41.057204],[-73.472068,41.057079],[-73.472068,41.056792],[-73.471593,41.056685],[-73.4705,41.056541],[-73.470238,41.056093],[-73.470215,41.055645],[-73.470714,41.055394],[-73.471355,41.055502],[-73.472282,41.055986],[-73.473281,41.056219],[-73.474445,41.055824],[-73.474968,41.055215],[-73.475182,41.055537],[-73.475182,41.055986],[-73.475491,41.056918],[-73.475919,41.055824],[-73.476014,41.055358],[-73.476014,41.054856],[-73.4758,41.054408],[-73.475325,41.054283],[-73.474992,41.054175],[-73.474968,41.053835],[-73.474469,41.053656],[-73.474374,41.053978],[-73.474231,41.054337],[-73.473946,41.05448],[-73.473019,41.054641],[-73.471522,41.054767],[-73.470595,41.054605],[-73.469169,41.054552],[-73.4676,41.054552],[-73.466911,41.054731],[-73.466673,41.054265],[-73.466578,41.053763],[-73.466222,41.05344],[-73.466578,41.052706],[-73.467196,41.05267],[-73.468123,41.052401],[-73.468575,41.051953],[-73.469454,41.051451],[-73.470191,41.051379],[-73.471308,41.05129],[-73.471664,41.051576],[-73.47145,41.052132],[-73.471474,41.052437],[-73.472187,41.052007],[-73.472995,41.05163],[-73.473708,41.051702],[-73.474231,41.052562],[-73.474897,41.0531],[-73.475467,41.053225],[-73.475895,41.052473],[-73.476299,41.052598],[-73.476347,41.052258],[-73.475657,41.05129],[-73.475443,41.051182],[-73.475348,41.051648],[-73.475111,41.051881],[-73.474564,41.051451],[-73.47397,41.05111],[-73.473946,41.05077],[-73.47397,41.05016],[-73.473661,41.049551],[-73.473708,41.049246],[-73.474065,41.048959],[-73.474469,41.049067],[-73.474826,41.049461],[-73.475491,41.049945],[-73.476513,41.049945],[-73.477416,41.04973],[-73.477274,41.0493],[-73.476893,41.049121],[-73.476656,41.048888],[-73.476394,41.048386],[-73.476204,41.047759],[-73.476204,41.046647],[-73.476085,41.045697],[-73.475966,41.045267],[-73.47561,41.044944],[-73.475277,41.045392],[-73.474944,41.04602],[-73.474469,41.046558],[-73.473708,41.047185],[-73.473352,41.047526],[-73.473162,41.048189],[-73.472496,41.048601],[-73.472187,41.049228],[-73.471641,41.049336],[-73.471522,41.04921],[-73.470809,41.048942],[-73.470238,41.04887],[-73.469739,41.048368],[-73.469501,41.047974],[-73.469311,41.047454],[-73.469478,41.047149],[-73.469739,41.047113],[-73.470191,41.047328],[-73.470809,41.047364],[-73.471855,41.046952],[-73.472568,41.046755],[-73.4729,41.046862],[-73.473613,41.046701],[-73.473827,41.046289],[-73.474017,41.04559],[-73.474326,41.044908],[-73.474208,41.044675],[-73.474826,41.044514],[-73.475111,41.04446],[-73.475467,41.043743],[-73.47542,41.043259],[-73.4758,41.042031],[-73.475836,41.041413],[-73.476157,41.040633],[-73.476406,41.039988],[-73.47662,41.03953],[-73.476977,41.038858],[-73.477262,41.03789],[-73.477404,41.036976],[-73.477832,41.036035],[-73.478474,41.035362],[-73.478937,41.035255],[-73.479864,41.035927],[-73.480185,41.036814],[-73.480114,41.037675],[-73.480078,41.038159],[-73.48015,41.038993],[-73.480542,41.039584],[-73.481576,41.040472],[-73.482289,41.041655],[-73.48261,41.042193],[-73.482503,41.042865],[-73.482503,41.043698],[-73.483715,41.044263],[-73.485034,41.044371],[-73.485533,41.044693],[-73.485497,41.045124],[-73.485248,41.045231],[-73.484677,41.045473],[-73.485105,41.045661],[-73.485569,41.046038],[-73.48564,41.046656],[-73.485141,41.047328],[-73.484606,41.047275],[-73.484107,41.046575],[-73.483893,41.046065],[-73.483251,41.045769],[-73.482966,41.04593],[-73.483037,41.046145],[-73.483358,41.046817],[-73.483287,41.047167],[-73.483144,41.047624],[-73.483144,41.048189],[-73.483358,41.048511],[-73.483323,41.048807],[-73.48318,41.049049],[-73.482716,41.049184],[-73.482503,41.049479],[-73.482182,41.04999],[-73.481326,41.051254],[-73.480898,41.051818],[-73.480613,41.052571],[-73.480435,41.053297],[-73.48047,41.053969],[-73.480613,41.054561],[-73.479864,41.055394],[-73.479151,41.055959],[-73.478617,41.056416],[-73.478367,41.056846],[-73.47826,41.057679],[-73.478331,41.058244],[-73.477868,41.05897],[-73.477725,41.059507],[-73.477725,41.059964],[-73.477903,41.060314],[-73.477975,41.060878],[-73.477654,41.061255],[-73.477226,41.061523],[-73.476691,41.062115],[-73.476299,41.062572],[-73.475978,41.063271],[-73.475836,41.063889],[-73.475301,41.064749],[-73.474837,41.065206],[-73.474517,41.065905],[-73.475622,41.065287],[-73.476834,41.06405],[-73.477476,41.062868],[-73.477725,41.062357],[-73.478224,41.061685],[-73.478937,41.061631],[-73.47933,41.061873],[-73.47965,41.061792],[-73.479401,41.061281],[-73.479258,41.060556],[-73.479009,41.060206],[-73.479116,41.059722],[-73.479472,41.059695],[-73.47965,41.059131],[-73.480007,41.058593],[-73.479829,41.058109],[-73.479437,41.05776],[-73.47908,41.057303],[-73.479508,41.057088],[-73.479936,41.0569],[-73.480613,41.056577],[-73.481076,41.056066],[-73.481255,41.055744],[-73.481397,41.055233],[-73.48154,41.055018],[-73.481718,41.054964],[-73.482681,41.054641],[-73.483358,41.054803],[-73.483679,41.054964],[-73.484285,41.054776],[-73.48425,41.054238],[-73.484071,41.053835],[-73.484071,41.053512],[-73.484499,41.053001],[-73.484891,41.052652],[-73.484606,41.052302],[-73.484178,41.052087],[-73.483358,41.052356],[-73.483037,41.052598],[-73.482467,41.052544],[-73.481896,41.052275],[-73.481932,41.051603],[-73.482182,41.051281],[-73.483323,41.050958],[-73.483822,41.050367],[-73.484143,41.049909],[-73.484178,41.049506],[-73.48507,41.049506],[-73.485747,41.049533],[-73.486567,41.049157],[-73.486888,41.049076],[-73.487815,41.048968],[-73.48785,41.048619],[-73.487209,41.048323],[-73.486567,41.047839],[-73.486389,41.047355],[-73.48621,41.046817],[-73.48646,41.046011],[-73.48646,41.044989],[-73.486745,41.044559],[-73.486995,41.044398],[-73.487458,41.044209],[-73.487922,41.044129],[-73.488278,41.043914],[-73.488563,41.044209],[-73.489562,41.044156],[-73.490524,41.043887],[-73.491487,41.043645],[-73.49195,41.043887],[-73.4922,41.044209],[-73.492057,41.044989],[-73.492343,41.045581],[-73.492842,41.046038],[-73.493305,41.046522],[-73.493626,41.046952],[-73.494232,41.047113],[-73.494339,41.04749],[-73.494339,41.047759],[-73.494303,41.048458],[-73.49441,41.049157],[-73.494624,41.049479],[-73.495052,41.049829],[-73.495444,41.050017],[-73.496407,41.050125],[-73.496656,41.050501],[-73.496656,41.050797],[-73.496015,41.050985],[-73.495551,41.051119],[-73.49523,41.051388],[-73.494909,41.051738],[-73.494553,41.051953],[-73.494303,41.051845],[-73.494089,41.051576],[-73.494196,41.051119],[-73.494517,41.050501],[-73.494054,41.050716],[-73.493697,41.051254],[-73.492735,41.051765],[-73.492663,41.052168],[-73.492877,41.052464],[-73.494054,41.052464],[-73.494303,41.052706],[-73.494196,41.053216],[-73.494161,41.053727],[-73.49441,41.054023],[-73.494874,41.054184],[-73.495195,41.054372],[-73.494838,41.054722],[-73.494874,41.055071],[-73.495337,41.055206],[-73.495587,41.054453],[-73.495694,41.054157],[-73.496086,41.054077],[-73.496229,41.054238],[-73.496157,41.055018],[-73.496336,41.055529],[-73.496799,41.05534],[-73.49712,41.055797],[-73.49712,41.05612],[-73.497797,41.056174],[-73.498332,41.056389],[-73.498082,41.056631],[-73.497619,41.057168],[-73.498938,41.056765],[-73.499794,41.056765],[-73.500257,41.056873],[-73.50097,41.056765],[-73.50179,41.056873],[-73.502503,41.057142],[-73.502646,41.057518],[-73.503395,41.057921],[-73.505177,41.058405],[-73.50558,41.058545],[-73.506496,41.058862],[-73.507067,41.059722],[-73.507269,41.060954],[-73.507121,41.060902],[-73.502397,41.059229],[-73.49999,41.058848],[-73.497762,41.05858],[-73.490914,41.059597],[-73.48628,41.060699],[-73.482679,41.062124],[-73.478543,41.067742],[-73.474372,41.072607],[-73.47396,41.071585],[-73.473009,41.072011],[-73.47194,41.072302],[-73.471375,41.072526],[-73.470959,41.071787],[-73.470662,41.0706],[-73.470692,41.069099],[-73.470038,41.067733],[-73.468018,41.067688],[-73.467929,41.068517],[-73.468315,41.069211],[-73.468374,41.069906],[-73.467988,41.069995],[-73.466265,41.070062],[-73.465077,41.070197],[-73.464275,41.070443],[-73.463502,41.070734],[-73.462759,41.071317],[-73.462433,41.071944],[-73.462908,41.072593],[-73.463086,41.073243],[-73.462759,41.074004],[-73.463383,41.075214],[-73.463264,41.075841],[-73.460396,41.075119],[-73.458822,41.074694]]]],";
    String data = body.get("data").toString();
    assertTrue(data.contains(geometryFeature));
  }

  /**
   * This method tests a successful search response on a different city
   * @throws IOException
   */
  @Test
  public void testMapsSearchHandlerDifferentCity() throws IOException {
    HttpURLConnection loadConnection = tryRequest("mapsKeyWord?Area=Providence");
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals("success", body.get("type"));
    String areaDescription = "All of this area is sparsely settled, but more particularly the central portion, where there are only a very few scattered houses.";
    String data = body.get("data").toString();
    assertTrue(data.contains(areaDescription));
  }

}
