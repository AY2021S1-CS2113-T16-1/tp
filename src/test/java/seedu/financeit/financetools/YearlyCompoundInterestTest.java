package seedu.financeit.financetools;

import org.junit.jupiter.api.Test;
import seedu.financeit.common.CommandPacket;
import seedu.financeit.parser.InputParser;
import seedu.financeit.ui.UiManager;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class YearlyCompoundInterestTest {

    public static CommandPacket handleInput(String input) {
        UiManager.printInputPrompt();
        return InputParser.getInstance().parseInput(input.toLowerCase());
    }

    // test
    @Test
    void calculateCompoundInterest_inputAmount_expectCorrectInterestEarned() {
        CommandPacket packet = handleInput("cyearly /a 1000 /r 3 /p 2");
        Double interestEarned = Handler.handleYearlyCompoundInterest(packet);
        assertEquals(60.9, interestEarned);
    }

    @Test
    void calculateCompoundInterest_inputAmountWithDeposit_expectCorrectInterestEarned() {
        CommandPacket packet = handleInput("cyearly /a 1000 /r 3 /p 2 /d 1200");
        Double interestEarned = Handler.handleYearlyCompoundInterest(packet);
        assertEquals(169.98, interestEarned);
    }
}