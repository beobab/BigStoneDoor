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
}
