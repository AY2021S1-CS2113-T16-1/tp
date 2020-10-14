package seedu.financeit.recurringtracker;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;
import seedu.financeit.common.Item;
import seedu.financeit.common.ItemList;
import seedu.financeit.common.exceptions.InsufficientParamsException;
import seedu.financeit.common.exceptions.ItemNotFoundException;
import seedu.financeit.common.exceptions.ParseFailParamException;
import seedu.financeit.recurringtracker.comparators.SortByDay;
import seedu.financeit.ui.TablePrinter;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.ParamChecker;

public class RecurringEntryList extends ItemList {

    @Override
    public void addItem(Item item) {
        super.addItemAndSort(item, new SortByDay());
    }


    /**
     * Handles params given to the list e.g. index of entry to modify/delete
     *
     * @param packet Command packet
     * @throws InsufficientParamsException When not all required params are provided
     */
    @Override
    public void handleParams(CommandPacket packet) throws InsufficientParamsException {
        try {
            super.handleParams(packet);
        } catch (ItemNotFoundException exception) {
            // Fall-through
        }
    }

    /**
     * Handles a single param from the packet.
     *
     * @param packet    input CommandPacket obtained from parsing user input.
     * @param paramType paramType of param that is currently being validated and processed.
     * @throws ParseFailParamException When param parsing fails
     */
    @Override
    public void handleSingleParam(CommandPacket packet, String paramType)
            throws ParseFailParamException {
        switch (paramType) {
        case ParamChecker.PARAM_INDEX:
            int index = paramChecker.checkAndReturnIndex(paramType, super.items);
            super.indexToModify = index;
            break;
        default:
            UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    paramChecker.getUnrecognizedParamMessage(paramType));
        }
    }

    @Override
    public void printList() {
        TablePrinter.setTitle(String.format("List of Recurring entries"));
        TablePrinter.addRow("No.;Day;Description;Expenditure amount;Income amount;"
                + "Duration;Payment type;Notes                    ");
        if (super.items.isEmpty()) {
            TablePrinter.addRow("No entries created;               ");
        } else {
            for (int i = 0; i < super.items.size(); i++) {
                RecurringEntry entry = (RecurringEntry) super.items.get(i);
                TablePrinter.addRow(String.format("%s;%s", i + 1, entry));
            }
        }
        TablePrinter.printList();
    }

}

