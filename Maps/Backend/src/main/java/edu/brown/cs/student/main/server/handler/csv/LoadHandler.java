package edu.brown.cs.student.main.server.handler.csv;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.parser.Parser;
import edu.brown.cs.student.main.csv.parser.RowToList;
import edu.brown.cs.student.main.data.csv.proxy.CsvData;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadHandler implements Route {
  private final CsvData sharedData;

  /**
   * The LoadHandler parses a given data file, passing its contents into a shared
   * source to be searched or viewed by the other handlers. It first takes in the same
   * instance of CsvData that ViewHandler and SearchHandler use, which ensures that
   * the same data is accessed across each handler
   * @param sharedData encompassing a csv file to be used by other handler classes
   */

  public LoadHandler(CsvData sharedData){
    this.sharedData = sharedData;
  }

  /**
   * As a class implementing Route, handle is used to load the parsed contents of the given
   * query parameter (a file path), into the sharedData's csv container. a Moshi instance builds and
   * adapts to take on a Map<String,Object> format, since this is what the loaded map response will
   * use. For a successful query, a Parser will be instantiated, and the handler will
   * pass the parsed data into the sharedData's container. For an unsuccessful one,
   * (such as when an invalid file path is given) an informative message will be
   * included in the load response map. The load response map will then be returned to the user.
   * map.
   * @param request information to be obtained
   * @param response
   * @return serialized map indicating success of data loading
   */

  @Override
  public Object handle(Request request, Response response){
    String filePath = request.queryParams("filePath");

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, String>> adapter = moshi.adapter(mapStringObject);
    Map<String, String> loadMap = new HashMap<>();

    try {

      Parser<List<String>> parser = new Parser<>(new FileReader(filePath), new RowToList());
      this.sharedData.loadProxyData(parser.parse());
      loadMap.put("type", "success");
      loadMap.put("data", filePath);
      return adapter.toJson(loadMap);

    } catch(Exception e){
      loadMap.put("type", "error");
      loadMap.put("error_type", "error_datasource");
      loadMap.put("error_message", "Please check that a valid file path has been given. " + filePath +
            " is incorrect.");


      return adapter.toJson(loadMap);
    }
  }
}