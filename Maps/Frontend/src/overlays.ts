import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

import rl_data from "./geodata/fullDownload.json";
import broadband_data from "./geodata/Broadband.json"
// import { parsedHighlight } from "./REPLInput";
function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

function accessBroadbandData() : Promise<string[][]>{
  return fetch("https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:*&in=state:*")
  .then((r) => r.json())
}
// async function getUnderservedCounties(){
//   let underservedCounties : string[] = []
//   const broadbandData = await accessBroadbandData();
//   broadbandData.forEach(county =>{
//     if (parseInt(county[1]) <= 85){
//       underservedCounties.push(county[0])
//     }
//   })
// }


export function overlayData(): GeoJSON.FeatureCollection | undefined {
  return isFeatureCollection(rl_data) ? rl_data : undefined;
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
