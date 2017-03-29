package net.toolan.doorplugin;

/**
 * Created by jonathan on 16/03/2017.
 * Main entry-point to the plugin. Accepts the standard door command.
 */

import net.toolan.doorplugin.Database.Database;
import net.toolan.doorplugin.Database.Main;
import net.toolan.doorplugin.Database.SQLite;
import org.bukkit.Bukkit;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class doorplugin extends JavaPlugin {

    private DoorBell _doorbell;
    public DoorBell getDoorBell() {
        if (_doorbell == null) _doorbell = new DoorBell();
        return this._doorbell;
    }

    public Database db;
    public Database getSqlDatabase() { return this.db; }

    private AllDoors _allDoors;


    @Override
    public void onEnable() {

        List<World> worlds = Bukkit.getServer().getWorlds();
        for (World w : worlds) {
            getLogger().info("recognised world: " + w.getName());
        }

        // This should create the config directories.
        saveDefaultConfig();

        // called when the plugin is reloaded. Assume wiped from memory until this point.
        getLogger().info("onEnable has been invoked!");

        this.db = new SQLite(new Main(this));
        this.db.load();

        _allDoors = getSqlDatabase().loadDoors(getDoorBell());

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

            DoorSubCommand subCmd = DoorSubCommand.GetSubCommand(args);

            CommandArguments parsedArgs = DoorSubCommand.ParseArguments(args, subCmd);
            if (parsedArgs == null) {
                return false;
            }

            if (subCmd == DoorSubCommand.Create) {
                CreateDoorCommand(player, subCmd, parsedArgs);
            }

            else if (subCmd == DoorSubCommand.Demo) {
                if (player != null)
                    _allDoors.DemoDoor(player);
            }

            else if (subCmd == DoorSubCommand.List) {
                sender.sendMessage(_allDoors.ListDoors());
            }

            else if (subCmd == DoorSubCommand.CancelTrigger) {
                DoorBell bell = getDoorBell();
                if (!bell.hasPlayerKey(player))
                    sender.sendMessage("You have nothing to cancel.");
                else {
                    getDoorBell().CancelPlayerClick(player);
                    sender.sendMessage("Activation cancelled.");
                }
            }


            else if (subCmd == DoorSubCommand.Info) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d != null)
                    sender.sendMessage(d.Info());
                else
                    sender.sendMessage("No such door!");
            }

            else if (subCmd == DoorSubCommand.Delete) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d != null) {
                    if (d.isOpen) d.Close();

                    getSqlDatabase().deleteDoor(parsedArgs.doorName);
                    _allDoors.Doors.remove(d);
                    getDoorBell().unsetDoorBell(d);
                    sender.sendMessage("Door " + parsedArgs.doorName + " deleted.");
                } else {
                    sender.sendMessage("Unable to delete door named " + parsedArgs.doorName + ".");
                }
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

            else if (subCmd == DoorSubCommand.Trigger) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to find a door named " + parsedArgs.doorName + ".");
                else {
                    getDoorBell().NextPlayerClickSetsDoorbell(player, d);
                    sender.sendMessage("Activate a button, lever or plate to set the trigger for door: " + parsedArgs.doorName);
                    sender.sendMessage("-- To cancel, use: /door cancelTrigger");
                }
            }

            else if (subCmd == DoorSubCommand.DeleteTrigger) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to find a door named " + parsedArgs.doorName + ".");
                else {
                    getDoorBell().NextPlayerClickUnSetsDoorbell(player, d);
                    sender.sendMessage("Activate a button, lever or plate to set the trigger for door: " + parsedArgs.doorName);
                }
            }

            // Didn't match any commands we recognised.
            else { return false; }

            // Successfully processed at least one of them.
            return true;
        }

        return false;
    }

    void CreateDoorCommand(Player player, DoorSubCommand subCmd, CommandArguments parsedArgs) {
        if (_allDoors.CreateDoor(player, parsedArgs)) {
            player.sendMessage("Door " + parsedArgs.doorName + " created.");

            BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);

            if (d.isAirDoor()) {
                player.sendMessage("No blocks where you want a door. I'm filling it with leaves so you can find it.");
                d.Close();
                d.FillWithLeaves();
            }

            getDoorBell().NextPlayerClickSetsDoorbell(player, d);
            player.sendMessage("Activate a button, lever or plate to set the trigger for your new door: " + parsedArgs.doorName);
            player.sendMessage("-- To cancel, use: /door cancelTrigger");

        } else {
            player.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
        }
    }

    void Help(String[] args) {
        String explain = "/door is a command to allow you to create a huge stone doorway which can be opened by a button or lever without the use of redstone.";


    }

}




