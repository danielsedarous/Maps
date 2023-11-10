import React, { useEffect, useState } from "react";
import "./App.css";
import MapBox from "./MapBox";
import REPL from "./REPL";

function App() {
  return (
    <div className="App">
      <div className="repl-container">
        <REPL />
      </div>
    </div>
  );
}

export default App;

