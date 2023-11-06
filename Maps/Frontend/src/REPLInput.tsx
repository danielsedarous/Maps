import "./main.css";
import { Dispatch, ReactElement, SetStateAction, useState } from "react";
import { ControlledInput } from "./ControlledInput";


export interface REPLInputProps {
  history: ReactElement[];
  setHistory: Dispatch<SetStateAction<ReactElement[]>>;
}


export function REPLInput(props: REPLInputProps) {
  const [commandString, setCommandString] = useState<string>("");

  async function handleSubmit(commandString: string) {
    var result : string[][];
    var splitString = splitIntoWords(commandString);
    if (splitString.length == 2) {
      var broadbandResult = await broadband(splitString);
      result = [["Broadband percentage for " + broadbandResult[1][0] + ": " + broadbandResult[1][1]]]
    } else {
      result = [["Please input a valid state and county in the following format: <state> <county>"]];
    }
    var resultTable = CSVToTable(result);
    props.setHistory([resultTable]);
    setCommandString("");
  }

  return (
    <div className="repl-input">
      <fieldset>
        <legend>Enter a command:</legend>
        <ControlledInput
          value={commandString}
          setValue={setCommandString}
          ariaLabel={"Command input"}
          onKeyDown={handleKeyDown}
        />
      </fieldset>
    </div>
  );

  /**
   *Split into words is a helper function that is used to turn the input that the user provides in the
   *command box into usable data that the application can deal with. A regex is used that is
   *looking for two conditions to “filter” the input text on. If the user would like to provide terms
   *containing more than one word, angle brackets are used to contain the entire term. Aside from
   *text within these angle brackets however, spaces are used to separate the terms, which are
   *added to an array of strings. It is this array that is used to check for individual commands and
   *values passed in by the user performed by other functions throughout the application.
   */

  // Regex pattern found on StackOverflow.
  function splitIntoWords(input: string): string[] {
    const regex = /<([^>]+)>|[^\s]+/g;
    const results: string[] = [];
    let match;
    while ((match = regex.exec(input)) !== null) {
      results.push(match[1] || match[0]);
    }
    return results;
  }

  function CSVToTable(data: string[][]) {
    return (
      <table aria-label="Result Table">
        <tbody>
          {data.map((row) => (
            <tr>
              {row.map((cell) => (
                <td aria-label={cell}> {cell} </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  /**
   * This function is responsible for checking for keyboard events executed by the user.
   * When the enter key is pressed, the handleSubmit function is called (as if the button was pressed).
   * In addition, arrow keys can be used to scroll thourgh the REPLHistory as shortcuts
   * for using the mouse or a trackpad.
   * @param event
   */
  function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    const historyWindow = document.getElementById("replHistoryId");
    if (event.key === "Enter") {
      handleSubmit(commandString);
    }
    if (event.key === "ArrowUp") {
      if (historyWindow != null) {
        historyWindow.scrollBy(0, -100);
      }
    }
    if (event.key === "ArrowDown") {
      if (historyWindow != null) {
        historyWindow.scrollBy(0, 100);
      }
    }
  }

  async function broadband(args: string[]) : Promise<string[][]> {
    return fetch(
      "http://localhost:1234/broadband?state=" + args[0] + "&county=" + args[1]
    )
      .then((r) => r.json())
      .then((response) => {
          var answer;
          if (response.type == "success") {
            answer = response.data;
          } else {
            answer =
              [["Broadband error - check server API connection or ensure provided state and county are valid"]];
          }
          return answer;
        } 
      );
  }
}
