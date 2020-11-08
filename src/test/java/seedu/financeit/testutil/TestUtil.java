package seedu.financeit.testutil;

import seedu.financeit.common.CommandPacket;

import java.util.HashMap;

public class TestUtil {
    public static CommandPacket createCommandPacket(String commandString, String[][] paramInput) {
        HashMap<String, String> paramMap = new HashMap<>();
        if (paramInput != null) {
            for (String[] paramSet : paramInput) {
                paramMap.put(paramSet[0], paramSet[1]);
            }
        }
        return new CommandPacket(commandString, paramMap);
    }

    /**
     * Creates a command packet. Assumes that paramTypes and paramArguments are of the same length.
     * @param commandString
     * @param paramTypes
     * @param paramArguments
     * @return CommandPacket created
     */
    public static CommandPacket createCommandPacket(String commandString, String[] paramTypes, String[] paramArguments) {
        HashMap<String, String> paramMap = new HashMap<>();
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                paramMap.put(paramTypes[i], paramArguments[i]);
            }
        }
        return new CommandPacket(commandString, paramMap);
    }
}
