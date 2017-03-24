package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathan on 22/03/2017.
 * DoorTabCompleter will fill in sub-commands and gates. Most commands take that form.
 */
public class DoorTabCompleter implements TabCompleter {

    AllDoors DoorList;

    DoorTabCompleter() {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        Bukkit.broadcastMessage(Integer.toString(args.length) + " arguments: ");
        for (String a : args) Bukkit.broadcastMessage(a);

        if (args.length == 1) {
            return SomeOf(DoorSubCommand.SubCommandNameList(), args[0]);
        }

        if (args.length == 2) {
            return SomeOf(DoorList.ListDoorNames(), args[1]);
        }

        return null;
    }

    private List<String> SomeOf(List<String> all, String partial) {
        String lowerPartial = (partial == null ? "" : partial.toLowerCase());
        List<String> lst = new ArrayList<>();
        for (String item : all) {
            if (item.toLowerCase().startsWith(lowerPartial))
                lst.add(item);
        }
        return lst;
    }
}
