package seedu.financeit;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.financetools.FinanceTools;
import seedu.financeit.goaltracker.GoalTracker;
import seedu.financeit.manualtracker.ManualTracker;
import seedu.financeit.parser.InputParser;
import seedu.financeit.recurringtracker.RecurringTracker;
import seedu.financeit.ui.MenuPrinter;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.ParamChecker;
import seedu.financeit.utils.storage.SaveStateHandlerManualTracker;
import seedu.financeit.utils.storage.SaveStateHandlerRecurringTracker;
import seedu.financeit.utils.storage.SaveStateHandlerGoalTracker;

import java.util.logging.Level;

public class Financeit {

    public static void main(String[] args) {

        String input = "";
        CommandPacket packet = null;
        Level mode = Level.OFF;
        ParamChecker.logger.setLevel(mode);
        SaveStateHandlerManualTracker mt = new SaveStateHandlerManualTracker("./data/save.txt", "./data");
        SaveStateHandlerGoalTracker gt = new SaveStateHandlerGoalTracker("./data/save1.txt", "./data");
        SaveStateHandlerRecurringTracker at = new SaveStateHandlerRecurringTracker("./data/save2.txt", "./data");

        try {
            gt.load();
            mt.load();
            at.load();
        } catch (Exception m) {
            MenuPrinter.prompt = "An exception has occurred: " + m;
        }

        while (true) {
            MenuPrinter.printMainMenu();
            input = UiManager.handleInput();
            packet = new InputParser().parseInput(input);
            UiManager.refreshPage();
            switch (packet.getCommandString()) {
            case "manual":
                ManualTracker.main();
                break;
            case "recur":
                RecurringTracker.main();
                break;
            case "acc": //AccSummary.main();
                break;
            case "goal": //GoalTracker.main();
                GoalTracker.main();
                break;
            case "financial": //FinancialCalculator.main();
                FinanceTools.main();
                break;
            case "logger":
                mode = (mode == Level.OFF) ? Level.ALL : Level.OFF;
                MenuPrinter.prompt = (mode == Level.OFF)
                    ? "Logger is off."
                    : "Logger is on.";
                ParamChecker.logger.setLevel(mode);
                break;
            case "exit":
                try {
                    mt.save();
                    gt.save();
                    at.save();
                } catch (Exception m) {
                    System.out.println("An exception has occurred: " + m);
                }
                return;
            default:
                MenuPrinter.prompt = "Invalid Command";
                break;
            }
        }
    }
}
