package seedu.financeit.datatrackers.recurringtracker.commands;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;
import seedu.financeit.common.ParamHandler;
import seedu.financeit.common.exceptions.InsufficientParamsException;
import seedu.financeit.common.exceptions.ItemNotFoundException;
import seedu.financeit.common.exceptions.ParseFailParamException;
import seedu.financeit.datatrackers.recurringtracker.RecurringEntry;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.ParamChecker;

import static seedu.financeit.utils.ParamChecker.PARAM_AMOUNT;
import static seedu.financeit.utils.ParamChecker.PARAM_DAY;
import static seedu.financeit.utils.ParamChecker.PARAM_DESCRIPTION;
import static seedu.financeit.utils.ParamChecker.PARAM_EXP;
import static seedu.financeit.utils.ParamChecker.PARAM_INC;

/**
 * Command class to create an entry instance, and populates it with parameter values.
 * The populated entry will be stored within the class, and can be retrieved by calling a
 * corresponding getter method.
 */
public class CreateEntryCommand extends ParamHandler {
    RecurringEntry recurringEntry;

    public CreateEntryCommand() {
        this.setRequiredParams(
            PARAM_DAY,
            PARAM_DESCRIPTION,
            PARAM_AMOUNT,
            PARAM_INC + " or " + PARAM_EXP
        );
    }

    public void handlePacket(CommandPacket packet) throws InsufficientParamsException {
        try {
            this.recurringEntry = new RecurringEntry();
            this.handleParams(packet);
        } catch (ItemNotFoundException exception) {
            // Fall-through
        }
    }

    @Override
    public void handleSingleParam(CommandPacket packet, String paramType) throws ParseFailParamException {
        switch (paramType) {
        case ParamChecker.PARAM_DAY:
            this.recurringEntry.setDay(ParamChecker.getInstance().checkAndReturnInt(paramType));
            break;
        case ParamChecker.PARAM_AMOUNT:
            this.recurringEntry.setAmount(ParamChecker.getInstance().checkAndReturnDouble(paramType));
            break;
        case ParamChecker.PARAM_INC:
            this.recurringEntry.setEntryType(Constants.EntryType.INC);
            break;
        case ParamChecker.PARAM_EXP:
            this.recurringEntry.setEntryType(Constants.EntryType.EXP);
            break;
        case ParamChecker.PARAM_DESCRIPTION:
            this.recurringEntry.setDescription(packet.getParam(paramType));
            break;
        case ParamChecker.PARAM_AUTO:
            this.recurringEntry.setAuto(true);
            break;
        case ParamChecker.PARAM_NOTES:
            this.recurringEntry.setNotes(packet.getParam(paramType));
            break;
        default:
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                ParamChecker.getInstance().getUnrecognizedParamMessage(paramType));
        }
    }

    public RecurringEntry getCurrEntry() {
        return this.recurringEntry;
    }
}
