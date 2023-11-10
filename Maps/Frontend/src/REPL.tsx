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
  const [highlightResult, setHighlightResult] = useState<string[][]>([[]]);

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
        highlightAreaResult={highlightResult}
        setHighlightAreaResult={setHighlightResult}
      />    
    </div>
  );
}
