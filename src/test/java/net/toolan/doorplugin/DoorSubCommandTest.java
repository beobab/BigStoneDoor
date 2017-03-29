package net.toolan.doorplugin;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 27/03/2017.
 */
public class DoorSubCommandTest {
    @Test
    public void subCommandNameList() throws Exception {
    }

    @Test
    public void getSubCommand() throws Exception {
        String[] args = {"cancelTrigger"};

        DoorSubCommand actual = DoorSubCommand.GetSubCommand(args);

        assertEquals("Cancel Trigger", DoorSubCommand.CancelTrigger, actual);
    }

    @Test
    public void parseArguments() throws Exception {
        String[] args = {"delete", "one"};

        CommandArguments actual = DoorSubCommand.ParseArguments(args, DoorSubCommand.Delete);

        assertEquals("delete command is trying to delete door 'one'", "one", actual.doorName);
    }

    @Test
    public void argumentIntAt() throws Exception {
        String[] args = {"create", "door1", "5", "5"};

        int actual = DoorSubCommand.ArgumentIntAt(args, 3, 1);

        assertEquals("args[3] == 5", 5, actual);
    }

    @Test
    public void argumentAt() throws Exception {
        String[] args = {"delete", "one"};

        String actual = DoorSubCommand.ArgumentAt(args, 1, "two");

        assertEquals("args[1] == 'one'", "one", actual);
    }

//    @Test
//    public void matchesCommand() throws Exception {
//        String[] args = {"delete", "one"};
//        boolean actual = DoorSubCommand.MatchesCommand(args, "delete", 2);
//
//        assertTrue("delete one matched delete command", actual);
//
//
//        String[] args2 = {"cancelTrigger"};
//        boolean actual2 = DoorSubCommand.MatchesCommand(args2, "cancelTrigger", 1);
//
//        assertTrue("cancelTrigger matched cancelTrigger command", actual2);
//    }

}