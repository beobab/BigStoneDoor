package net.toolan.doorplugin;

/**
 * Created by jonathan on 16/03/2017.
 */

import net.toolan.doorplugin.Database.Database;
import net.toolan.doorplugin.Database.Main;
import net.toolan.doorplugin.Database.SQLite;
import org.bukkit.Bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class doorplugin extends JavaPlugin {

    public static final boolean DEBUG = true;

    private DoorBell _doorbell;
    public DoorBell getDoorBell() {
        if (_doorbell == null) _doorbell = new DoorBell();
        return this._doorbell;
    }

    public Database db;
    public Database getSqlDatabase() { return this.db; }

    AllDoors _allDoors;


    @Override
    public void onEnable() {
        // called when the plugin is reloaded. Assume wiped from memory until this point.
        getLogger().info("onEnable has been invoked!");

        this.db = new SQLite(new Main(this));
        this.db.load();

        _allDoors = getSqlDatabase().loadDoors();
        getLogger().info("Loaded " + Integer.toString(_allDoors.Count()) + " door(s).");
        Bukkit.getPluginManager().registerEvents(getDoorBell(), this);

        getCommand("door").setTabCompleter(
            new DoorTabCompleter() {{
                DoorList = _allDoors;
            }}
        );
    }

    @Override
    public void onDisable() {
        // When plugin is disabled - persist somewhere.
        getSqlDatabase().saveDoors(_allDoors);

        getLogger().info("Saved " + Integer.toString(_allDoors.Count()) + " door(s).");
        getLogger().info("onDisable has been invoked!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("door")) {

            Player player = null;
            if (sender instanceof Player)
                player = (Player) sender;

            if (DEBUG) sender.sendMessage(Integer.toString(args.length) + " arguments");

            DoorSubCommand subCmd = DoorSubCommand.GetSubCommand(args);

            if (DEBUG) {
                if (subCmd == null) {
                    sender.sendMessage("No subCommand!");
                    sender.sendMessage("Usage: /door help");
                } else {
                    sender.sendMessage(subCmd.toString());
                }
            }


            CommandArguments parsedArgs = DoorSubCommand.ParseArguments(args, subCmd);
            if (parsedArgs == null) {
                sender.sendMessage("No arguments!");
                return false;
            }

            if (DEBUG) {
                sender.sendMessage(parsedArgs.toString());
            }

            if (subCmd == DoorSubCommand.Demo) {
                if (player != null)
                    _allDoors.DemoDoor(player);
            }

            if (subCmd == DoorSubCommand.List) {
                sender.sendMessage(_allDoors.ListDoors());
            }


            else if (subCmd == DoorSubCommand.Info) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d != null)
                    sender.sendMessage(d.Info());
                else
                    sender.sendMessage("No such door!");
            }


            else if (subCmd == DoorSubCommand.Create) {
                if (_allDoors.CreateDoor(sender, parsedArgs))
                    sender.sendMessage("Door " + parsedArgs.doorName + " created.");
                else
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
            }

            else if (subCmd == DoorSubCommand.Open) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
                else {
                    d.Open();
                    sender.sendMessage("Door " + parsedArgs.doorName + " opened.");
                }
            }

            else if (subCmd == DoorSubCommand.Close) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
                else {
                    d.Close();
                    sender.sendMessage("Door " + parsedArgs.doorName + " closed.");
                }
            }

            else if (subCmd == DoorSubCommand.SetTrigger) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to find a door named " + parsedArgs.doorName + ".");
                else {
                    getDoorBell().NextPlayerClickSetsDoorbell(player, d);
                    sender.sendMessage("Activate a button, lever or plate to set the trigger for door: " + parsedArgs.doorName);
                }
            }

            return true;
        }

        return false;
    }
}




