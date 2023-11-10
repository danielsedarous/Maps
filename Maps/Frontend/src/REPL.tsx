import { Dispatch, ReactElement, SetStateAction, useState } from "react";
import "./main.css";
import { REPLHistory } from "./REPLHistory";
import { REPLInput } from "./REPLInput";
import MapBox from "./MapBox";

/* 
Here, we set up the higher level REPL component with the necessary variables
for the REPLInput and REPLHistory components.
*/

export default function REPL() {
  const [history, setHistory] = useState<ReactElement[]>([]);
  const [Result, setResult] = useState<string[][]>([[]]);
  const [highlightResult, setHighlightResult] = useState<GeoJSON.Feature[]>([]);

  return (
    <div
      className="repl"
      aria-label="Please input commands here in this command line box"
    >
      <REPLHistory history={history} />
      <hr></hr>
      <REPLInput
        history={history}
        setHistory={setHistory}
        Result={Result}
        setResult={setResult}
        highlightResult={highlightResult}
        setHighlightResult={setHighlightResult}
      />
      <div className="map-container">
        <MapBox
          highlightResult={highlightResult}
          setHighlightResult={setHighlightResult}
        />
      </div>
    </div>
  );
}
