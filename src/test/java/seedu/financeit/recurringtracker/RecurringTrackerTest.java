package seedu.financeit.recurringtracker;

import org.junit.jupiter.api.Test;
import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecurringTrackerTest {

    @Test
    public void handleNewEntry_validInput_validEntryCreated() {
        CommandPacket packet = new CommandPacket("add", new HashMap());
        packet.addParamToMap("-e", "");
        packet.addParamToMap("/desc", "Test23123//>?>_+_~#$#@");
        packet.addParamToMap("/amt", "3490.34");
        packet.addParamToMap("/day", "15");
        packet.addParamToMap("/notes", "OIYH(*^(*ot9w3848(*&(*~~||///");
        RecurringEntry entry = RecurringTracker.handleNewEntry(packet);
        assertEquals(15, entry.day);
        assertEquals("Test23123//>?>_+_~#$#@", entry.description);
        assertEquals(Constants.EntryType.EXP, entry.entryType);
        assertEquals(3490.34, entry.amount);
        assertFalse(entry.auto);
        assertEquals("OIYH(*^(*ot9w3848(*&(*~~||///", entry.notes);
    }
}
