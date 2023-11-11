package edu.brown.cs.student.main;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.GeoJsonCollection;
import edu.brown.cs.student.main.JsonParsing;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapsBoundingHandler implements Route {
    public MapsBoundingHandler(){

    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {

            double lowerLat = Double.parseDouble(request.queryParams("lowerLatitude"));
            double upperLat = Double.parseDouble(request.queryParams("upperLatitude"));
            double lowerLong = Double.parseDouble(request.queryParams("lowerLongitude"));
            double upperLong = Double.parseDouble(request.queryParams("upperLongitude"));

            Moshi moshi = new Moshi.Builder().build();
            Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(Files.readString(Path.of(
                    "/Users/francescaelia/Documents/CS32/maps-dsedarou-felia/Maps/Backend/src/main/java/edu/brown/cs/student/main/geodata/fullDownload.json"))));
            GeoJsonCollection geoFeature = JsonParsing.fromJsonGeneral(reader, GeoJsonCollection.class);
            geoFeature.features = filterFeatureByCoordinates(geoFeature, lowerLat, upperLat, lowerLong, upperLong);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("type", "success");
            responseMap.put("data", JsonParsing.toJsonGeneral(geoFeature));
            return adapter.toJson(responseMap);
        } catch(Exception e) {
            return e;
        }

    }

    public static List<GeoJsonCollection.Feature> filterFeatureByCoordinates(GeoJsonCollection geoJsonCollection, double lowerLat, double upperLat, double lowerLong, double upperLong){
        List<GeoJsonCollection.Feature> filteredFeatures = new ArrayList<>(geoJsonCollection.features);
        Iterator<GeoJsonCollection.Feature> iterator = filteredFeatures.iterator();
        while (iterator.hasNext()) {
            GeoJsonCollection.Feature feature = iterator.next();
            GeoJsonCollection.Geometry geometry = feature.geometry;
            if (geometry != null){
                List<List<List<List<Double>>>> coordinatesList = geometry.coordinates;
                List<List<Double>> coordinates = coordinatesList.get(0).get(0);

                for (List<Double> coordinatePair : coordinates) {
                    if (!(coordinatePair.get(0) >= lowerLong && coordinatePair.get(0) <= upperLong
                            && coordinatePair.get(1) >= lowerLat && coordinatePair.get(1) <= upperLat)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            else{
                iterator.remove();
            }
        }
        return filteredFeatures;
    }
}