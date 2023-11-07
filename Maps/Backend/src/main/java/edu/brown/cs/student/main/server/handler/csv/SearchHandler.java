package edu.brown.cs.student.main.server.handler.csv;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.csv.search.Searcher;
import edu.brown.cs.student.main.data.csv.proxy.CsvData;
import spark.Request;
import spark.Response;
import spark.Route;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SearchHandler implements Route {

    private final CsvData sharedData;

    /**
     * The SearchHandler contains the functionality for searching through csv data contents,
     * which are obtained from the shared source (shareData). It first takes in the same
     * instance of CsvData that ViewHandler and LoadHandler use, which ensures that
     * the same data is accessed across each handler.
     * @param sharedData encompassing a csv file to be used by other handler classes
     */


    public SearchHandler(CsvData sharedData){
        this.sharedData = sharedData;
    }

    /**
     * As a class implementing Route, handle is used to search the parsed contents using the given
     * query parameters (target, column, and header indicator), as a guide for what data to return.
     * a Moshi instance builds and adapts to take on a Map<String,Object> format, since this is what
     * the search map result will use. For a successful query, a Searcher will be instantiated,
     * passing the query parameters as arguments. The given content to search through is sourced
     * from the sharedData's accessor method for getting the content of the csv. The results
     * of the search will be added to the search result map.
     * For an unsuccessful one, (such no file was previously loaded) an informative message will be
     * included in the search result map. The search result map will then be returned to the user.
     * map.
     * @param request information to be obtained
     * @param response
     * @return serialized map indicating success of data loading
     */

    @Override
    public Object handle(Request request, Response response) {
        String target = request.queryParams("target");
        String column = request.queryParams("column");
        String hasHeader = request.queryParams("header");



        Moshi moshi = new Moshi.Builder().build();
        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> searchMap = new HashMap<>();

        try {
            Searcher searcher;
            if(column == null || column.isEmpty()){
                searcher =
                    new Searcher(target, this.sharedData.getProxyData(), Boolean.valueOf(hasHeader));
            } else{
                searcher =
                    new Searcher(target, this.sharedData.getProxyData(), column, Boolean.valueOf(hasHeader));

            }
            searchMap.put("type", "success");

            if(searcher.search().isEmpty()){
                searchMap.put("type", "error");
                searchMap.put("details", "No results were found. Please check that you are searching "
                    + "in one of the following columns: " + this.sharedData.getProxyData().get(0)
                    + " or in an index ranging from 0 to " +
                    (this.sharedData.getProxyData().get(0).size()- 1)
                    + ". Otherwise, search for a new value using the following order: <Search target> +"
                    + "<Column to search in (name or index)> + <True/False: file has headers>");
            } else{
                searchMap.put("data", searcher.search());
            }

            return adapter.toJson(searchMap);

        } catch(Exception e){
            String errorMessage = e.getMessage();
            if(!sharedData.isLoaded()){
                errorMessage = "You must first load a file to search it. Try loading first.";
            }

            searchMap.put("type","error");
            searchMap.put("error_type", "error_bad_json");
            searchMap.put("details", errorMessage);
            return adapter.toJson(searchMap);
        }
    }
}
