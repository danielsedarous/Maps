package edu.brown.cs.student.main;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.GeoJsonCollection.Feature;
import edu.brown.cs.student.main.GeoJsonCollection.Properties;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

public class MapsAreaKeyWordHandler implements Route {
  private Map<String,Object> searchHistory;
public MapsAreaKeyWordHandler(){
  this.searchHistory = new HashMap<>();
}
  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String area = request.queryParams("Area");
      Moshi moshi = new Moshi.Builder().build();
      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
      JsonReader reader = JsonReader.of(new Buffer().writeUtf8(Files.readString(Path.of(
          "/Users/francescaelia/Documents/CS32/maps-dsedarou-felia/Maps/Backend/src/main/java/edu/brown/cs/student/main/geodata/fullDownload.json"))));
      GeoJsonCollection geoFeature = JsonParsing.fromJsonGeneral(reader, GeoJsonCollection.class);


      if (area.isEmpty()) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", "error_bad_request");
        responseMap.put("error_description", "Please input an area key word");
        return adapter.toJson(responseMap);
      }

      if (!(geoFeature.features.toString().contains(area))){
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", "error_bad_request");
        responseMap.put("error_description", "There were no areas that matched this area description");
        return adapter.toJson(responseMap);
      }
      Map<String, Object> responseMap = new HashMap<>();

      responseMap.put("type", "success");
      geoFeature.features = filterFeatureByArea(geoFeature, area);
      this.searchHistory.put(area, geoFeature.features);
      responseMap.put("data", JsonParsing.toJsonGeneral(geoFeature));
      return adapter.toJson(responseMap);
    } catch(Exception e) {
      return e;
    }
  }


  public static List<Feature> filterFeatureByArea(GeoJsonCollection geoJsonCollection, String area){
    List<Feature> filteredFeatures = new ArrayList<>(geoJsonCollection.features);
    Iterator<Feature> iterator = filteredFeatures.iterator();
    while (iterator.hasNext()) {
      Feature feature = iterator.next();
      Properties properties = feature.properties;
      Map<String, String> description = properties.area_description_data;
      if (description==null){
        iterator.remove();
        continue;
      }

      String allData = new String();

      for (String line: description.values())  {
        allData += line;
      }
      if (!allData.contains(area)) {
        iterator.remove();
        continue;
      }
    }
    return filteredFeatures;
  }

}
