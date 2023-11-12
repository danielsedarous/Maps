import Map, { Layer, MapLayerMouseEvent, Source } from "react-map-gl";
import { geoLayer, highlightLayer, overlayData, featureData } from "./overlays";
import React, { Dispatch, SetStateAction, useState, useEffect } from "react";
import { Access_Token } from "./private/api.js";

/**
 * defines props for values that need to be accessed and updated across classes
 */
export interface MapBoxProps {
  highlightResult: GeoJSON.Feature[];
  setHighlightResult: Dispatch<
    SetStateAction<
      GeoJSON.Feature<GeoJSON.Geometry, GeoJSON.GeoJsonProperties>[]
    >
  >;
}

/**
 * defines mapbox functions and values like setting highlighting for redlining and area searches
 * @param props
 * @returns
 */
function MapBox(props: MapBoxProps) {
  const initialZoom = 10;

  function onMapClick(e: MapLayerMouseEvent) {
    console.log(e.lngLat.lat);
    console.log(e.lngLat.lng);
  }

  const [viewState, setViewState] = useState({});

  const highlightData: GeoJSON.FeatureCollection = {
    type: "FeatureCollection",
    features: props.highlightResult,
  };

  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );

  useEffect(() => {
    setOverlay(overlayData());
  }, [props.highlightResult]);

  return (
    <Map
      mapboxAccessToken={Access_Token}
      {...viewState}
      onMove={(ev) => setViewState(ev.viewState)}
      style={{ width: window.innerWidth, height: window.innerHeight }}
      mapStyle={"mapbox://styles/mapbox/streets-v12"}
      onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
    >
      <Source id="geo_data" type="geojson" data={featureData}>
        <Layer {...geoLayer} />
      </Source>
      <Source id="highlight" type="geojson" data={highlightData}>
        <Layer {...highlightLayer} />
      </Source>
    </Map>
  );
}

export default MapBox;
