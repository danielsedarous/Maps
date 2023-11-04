package edu.brown.cs.student.main.broadbandhandlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.data.census.ACSSource;
import edu.brown.cs.student.main.data.census.BroadbandData;
import edu.brown.cs.student.main.data.census.CensusDataSource;
import edu.brown.cs.student.main.data.census.CensusLocation;
import edu.brown.cs.student.main.exceptions.DatasourceException;
import edu.brown.cs.student.main.data.census.mocks.MockedACSSource;
import edu.brown.cs.student.main.server.handler.census.BroadbandHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the BroadbandHandler methods and general functionality
 */
public class TestBroadbandHandler {

    private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    private JsonAdapter<Map<String, Object>> adapter;
    private final Moshi moshi = new Moshi.Builder().build();


    /**
     * This will set the port and eliminate logger spam - needs to be done before any tests are preformed
     */
    @BeforeAll
    public static void setupOnce() {
        // Pick an arbitrary free port
        Spark.port(0);
        // Eliminate logger spam in console for test suite
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root
    }


    /**
     * This needs to be run in any test that uses mocked data - sets up an instance of MockedACSSource that stores
     * "fake" data to be tested on without having to call the census api.
     * @throws DatasourceException this exception is thrown if BroadbandHandler cannot be properly instantiated due
     * to the data source being passed in
     * @throws IOException this exception is thrown if a file is badly formed
     */
    public void setup() throws DatasourceException, IOException {
        CensusDataSource mockedSource = new MockedACSSource(new BroadbandData(List.of(
            List.of("Kings County, California", "83.5", "06", "031"),
            List.of("Providence County, Rhode Island", "85.4", "44", "007"))));
        Spark.get("/broadband", new BroadbandHandler(mockedSource));
        Spark.awaitInitialization(); // don't continue until the server is listening

        this.adapter = this.moshi.adapter(this.mapStringObject);
    }

    /**
     * This method takes care of everything that should happen after each test, including unmapping the url path and
     * stopping spark
     */
    @AfterEach
    public void tearDown() {
        // Gracefully stop Spark listening on both endpoints
        Spark.unmap("/broadband");
        Spark.awaitStop(); // don't proceed until the server is stopped
    }


    /**
     * Helper to start a connection to a specific API endpoint/params
     *
     * @param apiCall the call string, including endpoint
     *                (Note: this would be better if it had more structure!)
     * @return the connection for the given URL, just after connecting
     * @throws IOException if the connection fails for some reason
     */
    private HttpURLConnection tryRequest(String apiCall) throws IOException {
        // Configure the connection (but don't actually send a request yet)
        URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
        // The request body contains a Json object
        clientConnection.setRequestProperty("Content-Type", "application/json");
        // We're expecting a Json object in the response body
        clientConnection.setRequestProperty("Accept", "application/json");
        clientConnection.connect();
        return clientConnection;
    }

    /**
     * This tests that our BroadbandHandler is successfully connecting when we use mock data as our source
     * @throws IOException
     * @throws DatasourceException
     */
    @Test
    public void testBroadbandRequestSuccessMockData() throws IOException, DatasourceException {
        this.setup();
        CensusLocation location = new CensusLocation("California", "Kings%20County");

        // Set up the request, make the request
        HttpURLConnection loadConnection = tryRequest("broadband?state=" + location.state() + "&county=" + location.county());
        Map<String, Object> body = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));

        assertEquals(200, loadConnection.getResponseCode());
        assertEquals("success", body.get("type"));

        loadConnection.disconnect();
    }

    /**
     * This tests that our BroadbandHandler correctly displays an error when the HttpURLConnection is formatted
     * incorrectly
     * @throws IOException
     * @throws DatasourceException
     */
    @Test
    public void testBroadbandRequestFailureMockData() throws IOException, DatasourceException {
        this.setup();
        CensusLocation location = new CensusLocation("California", "Kings%20County");

        // Set up the request, make the request
        HttpURLConnection loadConnection = tryRequest("broadband?state=-" + "&county=" + location.county());
        Map<String, Object> body = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));

        assertEquals(200, loadConnection.getResponseCode());
        assertEquals("error", body.get("type"));

        loadConnection.disconnect();
    }

    /**
     * This tests that the Broadband request fails using the real API as a data source if one of the parameters is
     * missing from the URL
     * @throws IOException
     * @throws DatasourceException
     */
    @Test
    public void testBroadbandRequestFailRealData() throws IOException, DatasourceException {
        CensusLocation location = new CensusLocation("Ohio", "Clark%20County");
        this.adapter = this.moshi.adapter(this.mapStringObject);


        // Set up the request, make the request
        ACSSource realSource = new ACSSource();

        Spark.get("/broadband", new BroadbandHandler(realSource));
        Spark.awaitInitialization();

        HttpURLConnection loadConnection = tryRequest("broadband?state=" + location.state() + "&county=");
        Map<String, Object> body = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        assertEquals("error_datasource", body.get("error_type"));

        loadConnection = tryRequest("broadband?");
        body = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        assertEquals("Please input a state AND county name.", body.get("details"));

        loadConnection.disconnect();
    }

    /**
     * This tests that the Broadband request is successful when it is formatted correctly using the real API as a
     * data source
     * @throws IOException
     * @throws DatasourceException
     */
    @Test
    public void testBroadbandRequestSuccessRealData() throws IOException, DatasourceException {
        CensusLocation location = new CensusLocation("Ohio", "Clark%20County");
        this.adapter = this.moshi.adapter(this.mapStringObject);

        // Set up the request, make the request
        ACSSource realSource = new ACSSource();

        Spark.get("/broadband", new BroadbandHandler(realSource));
        Spark.awaitInitialization();

        HttpURLConnection loadConnection = tryRequest("broadband?state=" + location.state() + "&county=" + location.county());
        Map<String, Object> body = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
        assertEquals("success", body.get("type"));
        loadConnection.disconnect();
    }

    /**
     * This tests our obtainStateCodes and obtainCountyCodes helper methods from the BroadbandHandler class are
     * functioning correctly
     * @throws DatasourceException
     * @throws IOException
     */
    @Test
    public void testStateAndCountyCodes() throws DatasourceException, IOException {
        ACSSource realSource = new ACSSource();
        BroadbandHandler broadbandHandler = new BroadbandHandler(realSource);
        Spark.get("/broadband", broadbandHandler);
        Spark.awaitInitialization();
        Map<String, String> stateCodes = broadbandHandler.obtainStateCodes();
        String countyCode = broadbandHandler.obtainCountyCode(stateCodes.get("rhode island"), "providence county");
        assertEquals("007", countyCode);
    }

    /**
     * This tests our obtainStateCodes and obtainCountyCodes helper methods return a null county code if the county
     * is real, but is not in the specified state.
     * @throws DatasourceException
     * @throws IOException
     */
    @Test
    public void testStateAndCountyCodesCountyNotInState() throws DatasourceException, IOException {
        ACSSource realSource = new ACSSource();
        BroadbandHandler broadbandHandler = new BroadbandHandler(realSource);
        Spark.get("/broadband", broadbandHandler);
        Spark.awaitInitialization();
        Map<String, String> stateCodes = broadbandHandler.obtainStateCodes();
        String countyCode = broadbandHandler.obtainCountyCode(stateCodes.get("rhode island"), "kings county");
        assertEquals(null, countyCode);
    }

}
