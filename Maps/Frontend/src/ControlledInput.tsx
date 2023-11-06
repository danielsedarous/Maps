import './main.css';
import { Dispatch, SetStateAction } from 'react';

/**
 * Here, we set up the command box and set a label for it.
 */

interface ControlledInputProps {
    value: string, 
    setValue: Dispatch<SetStateAction<string>>,
    ariaLabel: string,
    onKeyDown?:(event: React.KeyboardEvent <HTMLInputElement>) => void
  }

  export function ControlledInput({value, setValue, ariaLabel, onKeyDown}: ControlledInputProps) {
    return (
      <input
        type="text"
        className="repl-command-box"
        value={value}
        placeholder="Enter command here!"
        aria-label={ariaLabel}
        onChange={(ev) => setValue(ev.target.value)}
        onKeyDown={onKeyDown}
      ></input>
    );
  }