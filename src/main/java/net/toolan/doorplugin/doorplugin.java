package net.toolan.doorplugin;

/**
 * Created by jonathan on 16/03/2017.
 */

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.*;

public final class doorplugin extends JavaPlugin implements Listener {

    public static final boolean DEBUG = true;

    AllDoors _allDoors;

    Map<String, OperateDoor> _playerOperations = new HashMap<>();
    Map<String, List<OperateDoor>> _itemOperations = new HashMap<>();

    private static String PlayerKey(Player p) {
        return (p == null ? "" : p.getName());
    }

    private static String BlockKey(Block b) {
        // X|Y|Z|world|material
        Location l = (b == null ? null : b.getLocation());
        World w = (l == null ? null : l.getWorld());
        String wn = (w == null ? "" : w.getName());
        String m = (b == null ? "" : b.getType().toString());

        if (l == null) return "0|0|0|" + wn + "|" + m;

        return Integer.toString(l.getBlockX()) + "|" +
                Integer.toString(l.getBlockY()) + "|" +
                Integer.toString(l.getBlockZ()) + "|" +
                wn + "|" + m;
    }

    //maybe move this to BigDoor?
    private static String DoorKey (BigDoor d) {
        return (d == null ? "" : ((d.Name == null ? "" : d.Name) + "|" + (d.Owner == null ? "" : d.Owner.getName())));
    }

    // The doorbell.
    @EventHandler
    public void onButtonClick(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if (b == null) return;

        Action a = e.getAction();
        if (a == null) return;
        // Must switch a lever/button or step on a pressure plate.
        if (a != Action.RIGHT_CLICK_BLOCK && a != Action.PHYSICAL) return;

        Material clicker = b.getType();
        if (clicker == null) return;

        boolean isCorrectMaterial = (
// Single click items act like toggles.
               clicker == Material.STONE_BUTTON
            || clicker == Material.WOOD_BUTTON
            || clicker == Material.STONE_PLATE
            || clicker == Material.WOOD_PLATE
// --  Need work to work out how pressure plates will work.
//            || clicker == Material.IRON_PLATE
//            || clicker == Material.GOLD_PLATE

// The lever is a real toggle. Should probably get it's state and open/close specifically.
            || clicker == Material.LEVER
        );
        // Short circuit out asap if it's not interesting.
        if (!isCorrectMaterial) return;

        // See if any setup things are running for this player.
        final String playerKey = PlayerKey(e.getPlayer());
        OperateDoor operateDoor = _playerOperations.get(playerKey);
        if (operateDoor != null)
            operateDoor.operation(e);

        // This might hang on to references. It might be better to hang on to names of blocks somehow.
        final String blockKey = BlockKey(b);
        List<OperateDoor> operations = _itemOperations.get(blockKey);
        if (operations != null)
            for (OperateDoor op : operations)
                op.operation(e);
    }

    interface OperateDoor {
        void operation(PlayerInteractEvent e);
    }

    @Override
    public void onEnable() {
        // called when the plugin is reloaded. Assume wiped from memory until this point.
        getLogger().info("onEnable has been invoked!");
        _allDoors = new AllDoors();
        getLogger().info("Loaded " + Integer.toString(_allDoors.Count()) + " door(s).");
        //Bukkit.getPluginManager().registerEvents(listeners, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        DoorTabCompleter tc = new DoorTabCompleter();
        tc.DoorList = _allDoors;
        getCommand("door").setTabCompleter(tc);
    }

    @Override
    public void onDisable() {
        // When plugin is disabled - persist somewhere.
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

            DoorSubCommand subCmd = GetSubCommand(args);

            if (DEBUG) {
                if (subCmd == null) {
                    sender.sendMessage("No subCommand!");
                    sender.sendMessage("Usage: /door help");
                } else {
                    sender.sendMessage(subCmd.toString());
                }
            }


            CommandArguments parsedArgs = ParseArguments(args, subCmd);

            if (DEBUG) {
                if (parsedArgs == null) {
                    sender.sendMessage("No arguments!");
                } else {
                    sender.sendMessage(parsedArgs.toString());
                }
            }

            if (subCmd == DoorSubCommand.Demo) {
                if (player != null)
                    _allDoors.DemoDoor(player);
            }

            if (subCmd == DoorSubCommand.List) {
                sender.sendMessage(_allDoors.ListDoors());
            }


            else if (subCmd == subCmd.Info) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d != null)
                    sender.sendMessage(d.Info());
                else
                    sender.sendMessage("No such door!");
            }


            else if (subCmd == subCmd.Create) {
                if (_allDoors.CreateDoor(sender, parsedArgs))
                    sender.sendMessage("Door " + parsedArgs.doorName + " created.");
                else
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
            }

            else if (subCmd == subCmd.Open) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
                else {
                    d.Open();
                    sender.sendMessage("Door " + parsedArgs.doorName + " opened.");
                }
            }

            else if (subCmd == subCmd.Close) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to create Door named " + parsedArgs.doorName + ".");
                else {
                    d.Close();
                    sender.sendMessage("Door " + parsedArgs.doorName + " closed.");
                }
            }

            else if (subCmd == subCmd.SetTrigger) {
                BigDoor d = _allDoors.GetBigDoor(parsedArgs.doorName);
                if (d == null)
                    sender.sendMessage("Unable to find a door named " + parsedArgs.doorName + ".");
                else {
                    final String playerKey = PlayerKey(player);

                    OperateDoor setTriggerOnDoor = (e) -> {
                        Block b = e.getClickedBlock();
                        d.Trigger = b;
                        List<OperateDoor> operateDoors = _itemOperations.get(b);
                        if (operateDoors == null) operateDoors = new ArrayList<>();

                        operateDoors.add(x -> {
                            d.Toggle();
                            if (d.isOpen)
                                sender.sendMessage("You feel a gust of air.");
                            else
                                sender.sendMessage("You feel a thud of the ground in your feet");
                        });
                        _itemOperations.put(BlockKey(b), operateDoors);
                        Bukkit.broadcastMessage("This will now open and close door: " + d.Name);

                        // remove this op.
                        _playerOperations.remove(playerKey);
                    };
                    _playerOperations.put(playerKey, setTriggerOnDoor);
                    sender.sendMessage("Activate a button, lever or plate to set the trigger for door: " + parsedArgs.doorName);
                }
            }

            return true;
        }

        return false;
    }




    public DoorSubCommand GetSubCommand(String[] args) {
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

    private boolean MatchesCommand(String[] args, String name, int minimumNumberOfArguments) {
        if (minimumNumberOfArguments > args.length) return false;
        if (args[0].equalsIgnoreCase(name)) return true;
        return false;
    }
    private CommandArguments ParseArguments(final String[] args, DoorSubCommand subCommand) {

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



        return null;
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




