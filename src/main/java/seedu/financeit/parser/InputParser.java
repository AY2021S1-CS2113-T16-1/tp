package seedu.financeit.parser;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;
import seedu.financeit.common.exceptions.EmptyCommandStringException;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.RegexMatcher;

import java.util.HashMap;
import java.util.regex.Matcher;

public class InputParser {
    protected Matcher matcher;

    public InputParser() {
    }

    /**
     * This will parse the command into 4 small parts
     * Example: expense 5000 for 08
     * Output: expense;5000;for;08
     * @param userCommand
     * @return
     */
    public String[] parseGoalCommand (String userCommand) {
        String[] newCommand = userCommand.split(" ", 4);
        return newCommand;
    }

    /**
     * This will parse the edit command into 5 small parts
     * Example: edit expense 2000 for 08
     * Output: edit;expense;2000;for;08
     * @param userCommand
     * @return
     */
    public String[] parseEditCommand (String userCommand) {
        String[] newCommand = userCommand.split(" ", 5);
        return newCommand;
    }

    //@@author Artemis-Hunt
    /**
     * Example input: deadline /by tomorrow /note skip page 70.
     * commandString: "deadline"
     * CommandPacket created:
     * {
     *  commandType: ADD_DEADLINE
     *  params: HashMap< String, String >
     *  {
     *   "by": "tomorrow"
     *   "note": "skip page 70"
     *  }
     * }
     */
    public CommandPacket parseInput(String input) {
        String commandString = "";
        HashMap<String, String> params = new HashMap<>();
        String[] buffer;
        String separator = "";
        boolean paramsExist = false;

        //Split into [command, rest of input]
        //Check for existence of command title

        try {
            input = " " + input + " ";
            matcher = RegexMatcher.paramMatcher(input);
            separator = getSeparator(input);
        } catch (java.lang.IllegalStateException exception) {
            //No params provided
            commandString = input.toLowerCase();
            return new CommandPacket(commandString, params);
        }

        try {
            //Split into [<command>, <params string>]
            buffer = input.split(separator, 2);
            if (buffer[0].equals(" ")) {
                throw new EmptyCommandStringException();
            }
            commandString = buffer[0].toLowerCase();
            String paramSubstring = separator + buffer[1];
            //Build params HashMap
            params = new ParamsParser(paramSubstring).parseParams();
        } catch (EmptyCommandStringException e) {
            UiManager.printWithStatusIcon(Constants.PrintType.SYS_MSG, e.getMessage());
        }
        return new CommandPacket(commandString, params);
    }

    public String getSeparator(String input) {
        //Matcher matches <space><separator><paramType><space>, so (matched index + 1) gives the separator
        int separatorIndex = matcher.start() + 1;
        return String.valueOf(input.charAt(separatorIndex));
    }
}