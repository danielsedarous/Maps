import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

// import broadband_data from "./geodata/Broadband.json"
// import { parsedHighlight } from "./REPLInput";
function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}


function boundingBoxCall(): Promise<GeoJSON.Feature<GeoJSON.Geometry, GeoJSON.GeoJsonProperties>[]> {
  return fetch(
    "http://localhost:1234/mapsBoundingBox?lowerLatitude=-90&upperLatitude=90&lowerLongitude=-180&upperLongitude=180"
  )
    .then((r) => r.json())
    .then((response) => {
      var answer;
      console.log("response type" + response.type);
      if (response.type == "success") {
        return response.data;
    }});
}

// function accessBroadbandData() : Promise<string[][]>{
//   return fetch("https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:*&in=state:*")
//   .then((r) => r.json())
// }
// async function getUnderservedCounties(){
//   let underservedCounties : string[] = []
//   const broadbandData = await accessBroadbandData();
//   broadbandData.forEach(county =>{
//     if (parseInt(county[1]) <= 85){
//       underservedCounties.push(county[0])
//     }
//   })
// }

export const featureData: GeoJSON.FeatureCollection = {
  type: "FeatureCollection",
  features: await boundingBoxCall(),
};

 export function overlayData() {
  console.log("feat: " + featureData.features)
  console.log(isFeatureCollection(featureData) ? featureData : undefined)
  return isFeatureCollection(featureData) ? featureData : undefined;
}

//  export const highlightData: GeoJSON.FeatureCollection = {
//       type: "FeatureCollection",
//       features: parsedHighlight,
//     };

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
