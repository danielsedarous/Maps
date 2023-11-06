import "./main.css";
import { Dispatch, ReactElement, SetStateAction, useState } from "react";

/**
 * Here, we set up and display the command history though React components
 * stored in an array called history. These components are created in 
 * REPLInput.
 */

interface REPLHistoryProps {
  history: ReactElement[];
}

/**
 * This function displays each React component kept in the history array 
 * in the REPL history box.
 * @param props 
 * @returns 
 */

export function REPLHistory(props: REPLHistoryProps) {
  return (
    <div id="replHistoryId" className="repl-history" aria-live="polite" aria-atomic="true" aria-a aria-label="Past commands">
      {props.history.map((command, index) => (
        <p>{command}</p>
      ))}
    </div>
  );
}
