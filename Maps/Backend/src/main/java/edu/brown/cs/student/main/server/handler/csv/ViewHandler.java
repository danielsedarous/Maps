package edu.brown.cs.student.main.server.handler.csv;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.data.csv.proxy.CsvData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;


public class ViewHandler implements Route {
    /**
     * The ViewHandler contains the functionality for viewing an entire csv data's contents,
     * which are obtained from the shared source (shareData). It first takes in the same
     * instance of CsvData that SearchHandler and LoadHandler use, which ensures that
     * the same data is accessed across each handler.
     * @param sharedData encompassing a csv file to be used by other handler classes
     */

    private final CsvData sharedData;

    public ViewHandler(CsvData sharedData) {
        this.sharedData = sharedData;
    }

    /**
     * As a class implementing Route, handle is used to view the entire parsed file.
     * a Moshi instance builds and adapts to take on a Map<String,Object> format, since this is what
     * the view map result will use. For a successful query, the value from accessing
     * sharedData's current loaded file will be added to the view result map.
     * For an unsuccessful one, (such no file was previously loaded) an informative message will be
     * included in the view result map. The view result map will then be returned to the user.
     * map.
     * @param request information to be obtained
     * @param response
     * @return serialized map indicating success of data loading
     */

    @Override
    public Object handle(Request request, Response response) {
        Moshi moshi = new Moshi.Builder().build();

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> viewMap = new HashMap<>();

        try {
            viewMap.put("type", "success");
            viewMap.put("data", this.sharedData.getProxyData());
            return adapter.toJson(viewMap);

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (!sharedData.isLoaded()) {
                errorMessage = "You must first load a file to view it. Try loading first.";
            }
            viewMap.put("type", "error");
            viewMap.put("error_type", "error_bad_json");
            viewMap.put("details", errorMessage);
            return adapter.toJson(viewMap);
        }
    }
}


