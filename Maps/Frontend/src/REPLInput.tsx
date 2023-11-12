import "./main.css";
import {
  Dispatch,
  ReactElement,
  SetStateAction,
  useState,
  useEffect,
} from "react";
import { ControlledInput } from "./ControlledInput";

// /**
//  * props that need to be set and accessed across classes
//  */
export interface REPLInputProps {
  history: ReactElement[];
  setHistory: Dispatch<SetStateAction<ReactElement[]>>;
  Result: string[][];
  setResult: Dispatch<SetStateAction<string[][]>>;
  highlightResult: GeoJSON.Feature[];
  setHighlightResult: Dispatch<
    SetStateAction<
      GeoJSON.Feature<GeoJSON.Geometry, GeoJSON.GeoJsonProperties>[]
    >
  >;
}

/**
 * function that deals with the logic of the user's input to differentiate between
 * broadband and highlight searches, error handle to ensure they are inputting the
 * correct number of arguments, and ultimately return the data or a descriptive
 * error message
 * @param props
 * @returns
 */
export function REPLInput(props: REPLInputProps) {
  const [commandString, setCommandString] = useState<string>("");
  var splitString: string[];

  async function handleSubmit(commandString: string) {
    splitString = splitIntoWords(commandString);
    if (splitString[0] == "broadband") {
      var broadbandResult = await broadband(splitString);
      if (splitString.length == 3) {
        props.setResult(broadbandResult);
      } else {
        props.setResult([
          [
            "Please enter a valid broadband command: broadband <state> <county>",
          ],
        ]);
      }
    } else if (splitString[0] == "highlight") {
      const highlightLength: number = (await highlight(splitString)).features
        .length;

      if (splitString.length > 1 && highlightLength > 0) {
        props.setHighlightResult((await highlight(splitString)).features);
        await props.setResult([
          ["Search successful! Look on your map for the highlighted areas!"],
        ]);
      } else {
        await props.setResult([
          [
            "No results for your area description, please try another one and make sure your format is: highlight <area description>",
          ],
        ]);
      }
    } else {
      await props.setResult([
        [
          "Please enter a valid command (broadband <state> <county> or highlight <area description>)",
        ],
      ]);
    }
  }

    /**
     * parses result to a table and adds it to the history, and resets command string
     */
  useEffect(() => {
    let finalResult = props.Result;
    var resultTable = CSVToTable(finalResult);
    props.setHistory([resultTable]);

    setCommandString("");
  }, [props.Result]);

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

  /**
   * parses data into table format
   * @param data
   * @returns
   */
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
   * @param event
   */
  function handleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    const historyWindow = document.getElementById("replHistoryId");
    if (event.key === "Enter") {
      handleSubmit(commandString);
    }
  }

  /**
   * helper function that queries our backend server for the broadband percentage of a state and county
   * @param args
   * @returns
   */
  async function broadband(args: string[]): Promise<string[][]> {
    return fetch(
      "http://localhost:1234/broadband?state=" + args[1] + "&county=" + args[2]
    )
      .then((r) => r.json())
      .then((response) => {
        var answer;
        console.log("response type" + response.type);
        if (response.type == "success") {
          // answer = response.data;
          answer = [
            [
              "Broadband percentage for " +
                response.data[1][0] +
                ": " +
                response.data[1][1],
            ],
          ];
        } else {
          answer = [
            [
              "Broadband error - check server API connection or ensure provided state and county are valid",
            ],
          ];
        }
        return answer;
      });
  }

  /**
   * helper function that queries our backend server for a collection of features with
   * a cetain keword
   * @param args
   * @returns
   */
  async function highlight(args: string[]): Promise<GeoJSON.FeatureCollection> {
    return fetch("http://localhost:1234/mapsKeyWord?Area=" + args[1])
      .then((r) => r.json())
      .then((response) => {
        let parseAnswer: GeoJSON.FeatureCollection;
        var answer;
        answer = response.data;
        parseAnswer = JSON.parse(answer);
        console.log(parseAnswer);
        return parseAnswer;
      });
  }
}
