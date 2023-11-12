import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

/**
 * checks if a provided json is a feature collection
 * @param json 
 * @returns 
 */
function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

/**
 * helpfuler function that loads in the feature collection data through a call to the backend bounding box handler
 * with the full scope of latitude/longitude to load in entirety of geoJSON file
 * @returns 
 */
function boundingBoxCall(): Promise<GeoJSON.FeatureCollection> {
  return fetch(
    "http://localhost:1234/mapsBoundingBox?lowerLatitude=-90&upperLatitude=90&lowerLongitude=-180&upperLongitude=180"
  )
    .then((r) => r.json())
    .then((response) => {
      let parseAnswer: GeoJSON.FeatureCollection;
      var answer;
      answer = response.data
      parseAnswer = JSON.parse(answer)
      return parseAnswer;
    });
}

/**
 * constant that holdes the bounding box call feature data (resolves promise from boundingBoxCall())
 */
export const featureData: GeoJSON.FeatureCollection = {
  type: "FeatureCollection",
  features: (await boundingBoxCall()).features,
};


/**
 * checks that featureData constant is a feature collection using heleper method
 * @returns 
 */
 export function overlayData() {
  return isFeatureCollection(featureData) ? featureData : undefined;
}

/**
 * highlight layer for results of searching for area keywords on map
 */
export const highlightLayer: FillLayer = {
  id: "highlight_data",
  type: "fill",
  paint: {
    "fill-color": "#000000",
    "fill-opacity": 0.2,

  },
};

/**
 * matches holc_grade property to value to display redlining data on map
 */
const propertyName = "holc_grade";
export const geoLayer: FillLayer = {
  id: "geo_data",
  type: "fill",
  paint: {
    "fill-color": [
      "match",
      ["get", propertyName],
      "A",
      "#5bcc04",
      "B",
      "#04b8cc",
      "C",
      "#e9ed0e",
      "D",
      "#d11d1d",
      "#ccc",
    ],
    "fill-opacity": 0.2,
  },
};
