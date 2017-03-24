package net.toolan.doorplugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathan on 23/03/2017.
 */
public enum DoorSubCommand {
    Create,
    Demo,
    Open,
    Close,
    Location,
    Size,
    Style,
    Direction,
    Delay,
    Info,
    List,
    Delete,
    SetTrigger,
    Usage

    ;

    public static java.util.List<String> SubCommandNameList() {
        java.util.List<String> lst = new ArrayList<>();
        for (DoorSubCommand item : DoorSubCommand.values()) {
            lst.add(item.toString());
        }
        return lst;
    }

    public static DoorSubCommand GetSubCommand(String[] args) {
        if (args.length == 0) return DoorSubCommand.Usage;
        if (MatchesCommand(args, "demo", 1)) return DoorSubCommand.Demo;
        if (MatchesCommand(args, "list", 1)) return DoorSubCommand.List;
        if (MatchesCommand(args, "open", 2)) return DoorSubCommand.Open;
        if (MatchesCommand(args, "close", 2)) return DoorSubCommand.Close;
        if (MatchesCommand(args, "info", 2)) return DoorSubCommand.Info;
        if (MatchesCommand(args, "create", 2)) return DoorSubCommand.Create;
        if (MatchesCommand(args, "trigger", 1)) return DoorSubCommand.SetTrigger;

        return DoorSubCommand.Usage;
    }

    public static CommandArguments ParseArguments(final String[] args, DoorSubCommand subCommand) {

        if (subCommand == DoorSubCommand.List) {
            return new CommandArguments();
        }

        else if (subCommand == DoorSubCommand.Info
                || subCommand == DoorSubCommand.Open
                || subCommand == DoorSubCommand.Close
                || subCommand == DoorSubCommand.SetTrigger) {
            final String aName = ArgumentAt(args, 1, "" );
            return new CommandArguments() {{
                doorName = aName;
            }};
        }

        else if (subCommand == DoorSubCommand.Create) {
            final String aName = ArgumentAt(args, 1, "" );
            final DoorSize ds = new DoorSize(
                    ArgumentIntAt(args, 2, 5),
                    ArgumentIntAt(args, 3, 5),
                    ArgumentIntAt(args, 4, 5)
            );
            return new CommandArguments() {{
                doorName = aName;
                doorSize = ds;
            }};
        }

        return new CommandArguments();
    }

    private static boolean MatchesCommand(String[] args, String name, int minimumNumberOfArguments) {
        if (minimumNumberOfArguments > args.length) return false;
        if (args[0].equalsIgnoreCase(name)) return true;
        return false;
    }
    static int ArgumentIntAt (String[] args, int position, int defaultValue) {
        if (position >= args.length) return defaultValue;
        try {
            return Integer.parseInt(args[position]);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
    static String ArgumentAt (String[] args, int position, String defaultValue) {
        if (position >= args.length) return defaultValue;
        return args[position];
    }


}
