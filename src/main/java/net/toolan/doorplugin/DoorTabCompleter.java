package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathan on 22/03/2017.
 */
public class DoorTabCompleter implements TabCompleter {

    public AllDoors DoorList;

    DoorTabCompleter() {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        Bukkit.broadcastMessage(Integer.toString(args.length) + " arguments: ");
        for (String a : args) Bukkit.broadcastMessage(a);

        if (args.length == 1) {
            String partial = args[0];
            List<String> lst = new ArrayList<>();
            for (doorplugin.SubCommand cmd : doorplugin.SubCommand.values()) {
                String item = cmd.toString();
                if (item.startsWith(partial))
                    lst.add(item);
            }
            return lst;
        }

        if (args.length == 2) {
            String partial = args[1];
            List<String> lst = new ArrayList<>();
            for (String item : DoorList.ListDoorNames()) {
                if (item.startsWith(partial))
                    lst.add(item);
            }
            return lst;
        }

        return null;
    }
}
