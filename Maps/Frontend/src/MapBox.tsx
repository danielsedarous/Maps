import Map, { Layer, MapLayerMouseEvent, Source } from "react-map-gl";
import { geoLayer, overlayData } from "./overlays";
import React, { useEffect, useState } from "react";
import { Access_Token } from "./private/api.js";

interface LatLong {
  lat: number;
  long: number;
}

function MapBox() {
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

  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );

  useEffect(() => {
    // fetch bounding box data here
    setOverlay(overlayData());
  }, []);
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
    </Map>
  );
}

export default MapBox;
