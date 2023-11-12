import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}


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

export const featureData: GeoJSON.FeatureCollection = {
  type: "FeatureCollection",
  features: (await boundingBoxCall()).features,
};

 export function overlayData() {
  return isFeatureCollection(featureData) ? featureData : undefined;
}

export const highlightLayer: FillLayer = {
  id: "highlight_data",
  type: "fill",
  paint: {
    "fill-color": "#000000",
    "fill-opacity": 0.2,

  },
};


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
