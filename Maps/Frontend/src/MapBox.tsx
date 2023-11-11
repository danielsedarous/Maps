import Map, { Layer, MapLayerMouseEvent, Source } from "react-map-gl";
import { geoLayer, highlightLayer, overlayData, featureData} from "./overlays";
import React, { Dispatch, SetStateAction, useState, useEffect } from "react";
import { Access_Token } from "./private/api.js";
// import { highlightData } from "./overlays";
// import { parsedHighlight } from "./REPLInput";

// export interface MapBoxProps {
//   highlightAreaResult: string[][];
//   setHighlightAreaResult: Dispatch<SetStateAction<string[][]>>;
// }

export interface MapBoxProps {
  highlightResult: GeoJSON.Feature[];
  setHighlightResult: Dispatch<
    SetStateAction<
      GeoJSON.Feature<GeoJSON.Geometry, GeoJSON.GeoJsonProperties>[]
    >
  >;
}
interface LatLong {
  lat: number;
  long: number;
}

function MapBox(props: MapBoxProps) {
  //  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(undefined);
  // const ProvidenceLatLong: LatLong = { lat: 41.824, long: -71.4128 };
  const initialZoom = 10;

  function onMapClick(e: MapLayerMouseEvent) {
    console.log(e.lngLat.lat);
    console.log(e.lngLat.lng);
  }

  const [viewState, setViewState] = useState({
    // longitude: ProvidenceLatLong.long,
    // latitude: ProvidenceLatLong.lat,
    // zoom: initialZoom,
  });

  const highlightData: GeoJSON.FeatureCollection = {
    type: "FeatureCollection",
    features: props.highlightResult,
  };

    const [overlay, setOverlay] = useState<
      GeoJSON.FeatureCollection | undefined
    >(undefined);


  useEffect(() => {
    // fetch bounding box data here
    // const boundingBoxData = getBoundingBoxData;
    setOverlay(overlayData());
    console.log("overlay" + overlay)
    console.log("featureData: " + featureData.features);
    console.log("featureType: " + featureData.type);

  }, [props.highlightResult]);

  // const highlightData: GeoJSON.FeatureCollection = {
  //   type: "FeatureCollection",
  //   features: props.highlightAreaResult,
  // };

  // function getBoundingBoxData(): GeoJSON.FeatureCollection | undefined {
  //   return overlayData();
  // }

  return (
    <Map
      mapboxAccessToken={Access_Token}
      {...viewState}
      onMove={(ev) => setViewState(ev.viewState)}
      style={{ width: window.innerWidth, height: window.innerHeight }}
      mapStyle={"mapbox://styles/mapbox/streets-v12"}
      onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
    >
      <Source id="geo_data" type="geojson" data={overlay}>
        <Layer {...geoLayer} />
      </Source>
      <Source id="highlight" type="geojson" data={highlightData}>
        <Layer {...highlightLayer} />
      </Source>
    </Map>
  );
}

export default MapBox;
