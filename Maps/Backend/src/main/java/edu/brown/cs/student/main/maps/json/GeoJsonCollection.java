package edu.brown.cs.student.main.maps.json;

import java.util.List;
import java.util.Map;

/**
 * Represents a GeoJSON collection
 */
public class GeoJsonCollection {
public String type;
public List <Feature> features;

  /**
   * Represents a GeoJSON features
   */
  public static class Feature{
  public String type;
  public Geometry geometry;
  public Properties properties;
}

  /**
   * Represents properties associated with a GeoJSON Feature
   */
  public static class Properties{
  public String name;
  public String holc_grade;
  public Map<String,String> area_description_data;
}

  /**
   * Represents geometry/coordinate information in GeoJSON
   */
  public static class Geometry{
  public String type;
  public List<List<List<List<Double>>>> coordinates;
}
}
