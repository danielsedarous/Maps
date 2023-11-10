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
public MapsAreaKeyWordHandler(){

}
  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      String area = request.queryParams("Area");
      Moshi moshi = new Moshi.Builder().build();
      Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
      JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
      JsonReader reader = JsonReader.of(new Buffer().writeUtf8(Files.readString(Path.of(
          "C:\\cs32\\maps-dsedarou-felia\\Maps\\Frontend\\src\\geodata\\fullDownload.json"))));
      GeoJsonCollection geoFeature = JsonParsing.fromJsonGeneral(reader, GeoJsonCollection.class);


      if (area.isEmpty()) {
        return JsonParsing.toJsonGeneral(geoFeature);
      }
      Map<String, Object> responseMap = new HashMap<>();

      responseMap.put("type", "success");
      geoFeature.features = filterFeatureByArea(geoFeature, area);
      responseMap.put("data", JsonParsing.toJsonGeneral(geoFeature));
//      responseMap.put()
      return adapter.toJson(responseMap);
    } catch(Exception e) {
      return e;
    }

  }



  public static List<Feature> filterFeatureByArea(GeoJsonCollection geoJsonCollection, String area){
    List<Feature> filteredFeatures = new ArrayList<>(geoJsonCollection.features);
    //System.out.println(filteredFeatures);
    Iterator<Feature> iterator = filteredFeatures.iterator();
    while (iterator.hasNext()) {
      Feature feature = iterator.next();
      Properties properties = feature.properties;
      //System.out.println(properties);
      Map<String, String> description = properties.area_description_data;
      //System.out.println(description);
      if (description==null){
        iterator.remove();
        continue;
      }

      String allData = new String();

      for (String line: description.values())  {
        allData += line;
      }
      if (!allData.contains(area))
      {iterator.remove();
        continue;
      }
//      feature.properties.name = "Highlight";
//      feature.properties.holc_grade = "H";
    }
    //System.out.println(filteredFeatures);
    return filteredFeatures;
  }

}
