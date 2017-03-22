package net.toolan.doorplugin;

/**
 * Created by jonathan on 16/03/2017.
 */

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


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

            SubCommand subCmd = GetSubCommand(args);

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

            if (subCmd == SubCommand.Demo) {
                if (player != null)
                    _allDoors.DemoDoor(player);
            }

            if (subCmd == SubCommand.List) {
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


    public enum SubCommand {
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
    }

    public SubCommand GetSubCommand(String[] args) {
        if (args.length == 0) return SubCommand.Usage;
        if (MatchesCommand(args, "demo", 1)) return SubCommand.Demo;
        if (MatchesCommand(args, "list", 1)) return SubCommand.List;
        if (MatchesCommand(args, "open", 2)) return SubCommand.Open;
        if (MatchesCommand(args, "close", 2)) return SubCommand.Close;
        if (MatchesCommand(args, "info", 2)) return SubCommand.Info;
        if (MatchesCommand(args, "create", 2)) return SubCommand.Create;
        if (MatchesCommand(args, "trigger", 1)) return SubCommand.SetTrigger;

        return SubCommand.Usage;
    }

    private boolean MatchesCommand(String[] args, String name, int minimumNumberOfArguments) {
        if (minimumNumberOfArguments > args.length) return false;
        if (args[0].equalsIgnoreCase(name)) return true;
        return false;
    }
    private CommandArguments ParseArguments(final String[] args, SubCommand subCommand) {

        if (subCommand == SubCommand.List) {
            return new CommandArguments();
        }

        else if (subCommand == SubCommand.Info
              || subCommand == SubCommand.Open
              || subCommand == SubCommand.Close
              || subCommand == SubCommand.SetTrigger) {
            final String aName = ArgumentAt(args, 1, "" );
            return new CommandArguments() {{
                doorName = aName;
            }};
        }

        else if (subCommand == SubCommand.Create) {
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

class DoorSize {
    public int X;
    public int Y;
    public int Z;

    DoorSize(int x, int y, int z) {
        X = x;
        Y = y;
        // "At most one thick" criteria enforced.
        if (x > 1 && y > 1 && z > 1)
            Z = 1;
        else if (x < -1 && y < -1 && z < -1)
            Z = -1;
        else
            Z = z;
    }

    @Override
    public String toString() {
        return "X: " + Integer.toString(X) +
               ", Y: " + Integer.toString(Y) +
               ", Z: " + Integer.toString(Z);
    }
}

class BigDoorStorageClass {
    String doorName;
    String doorSize;
    String worldName;
    String ownerName;
    String doorRoot;

//    BigDoor GetDoor() {
//        return new BigDoor(
//                doorName,
//                Player owner,
//                World w,
//                Location root,
//                DoorSize d
//
//        )
//    }

    @Override
    public String toString() {
        return "Name: " + (doorName == null ? "(null)" : doorName) + "\n" +
                "Owner: " + (ownerName == null ? "(null)" : ownerName) + "\n" +
                "World: " + (worldName == null ? "(null)" : worldName) + "\n" +
                "Location: " + (doorRoot == null ? "(null)" : doorRoot.toString()) + "\n" +
                "Size: " + (doorSize == null ? "(null)" : doorSize.toString());
    }
}

class CommandArguments {
    String doorName;
    DoorSize doorSize;
    String worldName;
    String ownerName;
    Location doorRoot;

    @Override
    public String toString() {
        return "Name: " + (doorName == null ? "(null)" : doorName) + "\n" +
                "Owner: " + (ownerName == null ? "(null)" : ownerName) + "\n" +
                "World: " + (worldName == null ? "(null)" : worldName) + "\n" +
                "Location: " + (doorRoot == null ? "(null)" : doorRoot.toString()) + "\n" +
                "Size: " + (doorSize == null ? "(null)" : doorSize.toString());
    }
}

class AllDoors {
    public List<BigDoor> Doors;

    //constructor
    AllDoors() {
        Doors = new ArrayList<BigDoor>();
//        Doors.add(new BigDoor("mountainway", "LiztherWiz"));
//        Doors.add(new BigDoor("dragondoors", "Minecraft_101"));
    }

    public void DemoDoor(Player player) {
        Location startLoc = GetLocationInFrontOfPlayer(player);

        DemoInterpret(player, "0,0,0-0,5,5|STONE,2");
        DemoInterpret(player, "0,0,1-0,4,4|STONE,5");
        DemoInterpret(player, "0,0,1-0,4,4|DOOR|DEMO_VANISH");
        DemoInterpret(player, "0,0,6|LEVER|DEMO_VANISH");

    }

    @SuppressWarnings("deprecation")
    private void DemoInterpret(Player player, String setupline) {
         String[] args = setupline.split("\\|");
         LocationPair pair = InterpretPair(player, args[0]);
         if (IsDoorKeyword(args[1])) {
            player.sendMessage("door command");
         } else {
             MaterialData material = InterpretMaterial(args[1]);

             BuildThings(player, pair, material);
             player.sendMessage("built some stuff");
         }
    }

    @SuppressWarnings("deprecation")
    private void BuildThings(Player player, LocationPair pair, MaterialData material) {
        int minX = (int) Math.min(pair.A.getX(), pair.B.getX());
        int minY = (int) Math.min(pair.A.getY(), pair.B.getY());
        int minZ = (int) Math.min(pair.A.getZ(), pair.B.getZ());

        int maxX = 1 + (int) Math.max(pair.A.getX(), pair.B.getX());
        int maxY = 1 + (int) Math.max(pair.A.getY(), pair.B.getY());
        int maxZ = 1 + (int) Math.max(pair.A.getZ(), pair.B.getZ());


        player.sendMessage("A: " +  Integer.toString(minX) + "," +
                Integer.toString(minY) + "," +
                Integer.toString(minZ) +
                " B: " + Integer.toString(maxX) + "," +
                Integer.toString(maxY) + "," +
                Integer.toString(maxZ));

        World w = player.getWorld();

        Location workingBlock = pair.A.clone();

        for (int x = minX; x < maxX; x++) {
            workingBlock.setX(x);
            for (int y = minY; y < maxY; y++) {
                workingBlock.setY(y);
                for (int z = minZ; z < maxZ; z++) {
                    workingBlock.setZ(z);

                    Block b = w.getBlockAt(workingBlock);
                    b.setData(material.getData());
                    b.setType(material.getItemType());
                    b.setData(material.getData());
                }
            }
        }
    }


    private boolean IsDoorKeyword(String s) {
            return s.equalsIgnoreCase("DOOR") ||
                    s.equalsIgnoreCase("LEVER") ||
                    s.equalsIgnoreCase( "BUTTON");
    }

    @SuppressWarnings("deprecation")
    private MaterialData InterpretMaterial(String material) {
        String[] args = material.split(",");
        Material m = Material.valueOf(args[0]);
        if (args.length > 1) {
            byte b = (byte) (Integer.parseInt(args[1]));
            return new MaterialData(m, b);
        } else {
            return new MaterialData(m);
        }
    }

    private LocationPair InterpretPair(Player player, String location) {
        String[] args = location.split("-");
        Location loc1 = InterpretLocation(player, args[0]);
        Location loc2;
        if (args.length > 1)
            loc2 = InterpretLocation(player, args[1]);
        else
            loc2 = loc1.clone();
        return new LocationPair(loc1, loc2);
    }
    private Location InterpretLocation(Player player, String location) {
        String[] args = location.split(",");
        Location root = player.getLocation();

        return new Location(
                player.getWorld(),
                root.getX() + Double.parseDouble(args[0]),
                root.getY() + Double.parseDouble(args[1]),
                root.getZ() + Double.parseDouble(args[2])
        );
    }

    public List<String> ListDoorNames() {
        List<String> lst = new ArrayList<>();
        for (BigDoor b : Doors) {
            lst.add(b.Name);
        }
        return lst;
    }


    private class LocationPair {
        public final Location A;
        public final Location B;
        LocationPair(Location a, Location b) { this.A = a; this.B = b; }
    }


    public int Count() {
        return Doors.size();
    }

    public BigDoor GetBigDoor(String name) {
        for(Iterator<BigDoor> i = Doors.iterator(); i.hasNext(); ) {
            BigDoor item = i.next();
            if (item.Name.equalsIgnoreCase(name))
                return item;
        }
        return null;
    }

    public String ListDoors() {
        String s = "Name (Owner)\n" +
                "--------------------------------------------\n";
        for(Iterator<BigDoor> i = Doors.iterator(); i.hasNext(); ) {
            BigDoor item = i.next();
            s += item.Name + " (" + item.Owner + ")\n";
        }
        return s;
    }

    private static String getCardinalDirection(Player player, boolean includeUpDown) {
        float yaw = player.getLocation().getYaw();
        // make sure it's within 0-360.
        //Bukkit.broadcastMessage("Yaw: " + Float.toString(yaw));

        int rot = (int) (((double)yaw + 360.0) % 360);
        //Bukkit.broadcastMessage("Rotation: " + Integer.toString(rot));

        if (includeUpDown) {
            float pitch = player.getLocation().getPitch();
            if (pitch > 55.0) return "Down";
            if (pitch < -55.0) return "Up";
        }

        if (0 <= rot && rot < 45) {
            return "South"; // 0 or 360 = +Z

        } else if (45 <= rot && rot < 135) {
            return "West";  // 90 = -X

        } else if (135 <= rot && rot < 225) {
            return "North"; // 180 = -Z

        } else if (225 <= rot && rot < 315) {
            return "East"; // 270 = +X

        } else if (315 <= rot && rot <= 360) {
            return "South"; // 0 or 360 = +Z

        } else {
            return "No idea";
        }
    }
    private Location GetLocationInFrontOfPlayer(Player player) {
        Location loc = player.getLocation();
        loc.setY(loc.getY() + 0.1); // In likely case of clipping with ground.

        String facing = getCardinalDirection(player, true);
        if (facing.equalsIgnoreCase("East"))
            loc.setX(loc.getX() + 1);
        else if (facing.equalsIgnoreCase("South"))
            loc.setZ(loc.getZ() + 1);
        else if (facing.equalsIgnoreCase("West"))
            loc.setX(loc.getX() - 1);
        else if (facing.equalsIgnoreCase("North"))
            loc.setZ(loc.getZ() - 1);

        else if (facing.equalsIgnoreCase("Up"))
            loc.setY(loc.getY() + 2);
        else if (facing.equalsIgnoreCase("Down"))
            loc.setY(loc.getY() - 1);


        return loc;
    }

    private DoorSize ConvertDoorSizeFromRightUpIn(Player player, DoorSize original) {
        DoorSize result = null;

        //Location loc = player.getLocation();
        String facing = getCardinalDirection(player, true);
        if (facing.equalsIgnoreCase("East")) {
            result = new DoorSize(original.Z, original.Y, original.X);

        } else if (facing.equalsIgnoreCase("South")) {
            result = new DoorSize(-original.X, original.Y, original.Z);

        } else if (facing.equalsIgnoreCase("West")) {
            result = new DoorSize(-original.Z, original.Y, -original.X);

        } else if (facing.equalsIgnoreCase("North")) {
            result = new DoorSize(original.X, original.Y, -original.Z);

        } else if (facing.equalsIgnoreCase("Up")) {
            String secondaryFacing = getCardinalDirection(player, false);
            if (secondaryFacing.equalsIgnoreCase("East")) {
                result = new DoorSize(-original.Y, original.Z, original.X); //done

            } else if (secondaryFacing.equalsIgnoreCase("South")) {
                result = new DoorSize(-original.X, original.Z, -original.Y); //done

            } else if (secondaryFacing.equalsIgnoreCase("West")) {
                result = new DoorSize(original.Y, original.Z, -original.X); // test

            } else if (secondaryFacing.equalsIgnoreCase("North")) {
                result = new DoorSize(original.X, original.Z, original.Y); //done

                // Not sure how we'd ever get here, but n/m. Pretend it's east.
            } else {
                result = new DoorSize(-original.Y, original.Z, original.X);
            }

        } else if (facing.equalsIgnoreCase("Down")) {
            String secondaryFacing = getCardinalDirection(player, false);
            if (secondaryFacing.equalsIgnoreCase("East")) {
                result = new DoorSize(original.Y, -original.Z, original.X); //done

            } else if (secondaryFacing.equalsIgnoreCase("South")) {
                result = new DoorSize(-original.X, -original.Z, original.Y); //done

            } else if (secondaryFacing.equalsIgnoreCase("West")) {
                result = new DoorSize(-original.Y, -original.Z, -original.X); //done

            } else if (secondaryFacing.equalsIgnoreCase("North")) {
                result = new DoorSize(original.X, -original.Z, -original.Y); //done

                // Not sure how we'd ever get here, but n/m. Pretend it's east.
            } else {
                result = new DoorSize(original.Y, -original.Z, original.X);
            }
        }

        Bukkit.broadcastMessage(result.toString());
        if (result != null) return result;

        Bukkit.broadcastMessage("Dunno what to do. Returning original.");
        return original;
    }


    public boolean CreateDoor(CommandSender sender, final CommandArguments parsedArguments) {
        String owner = "Console";

        BigDoor previousDoor = GetBigDoor(parsedArguments.doorName);
        if (previousDoor != null) {
            sender.sendMessage("There is already a door with that name.");
            return false;
        }


        if (sender instanceof Player) {
            Player player = (Player) sender;

            String facing = getCardinalDirection(player, true);
            String subFacing = getCardinalDirection(player, false);
            Bukkit.broadcastMessage("Facing: " + facing);
            if (facing.equalsIgnoreCase("Up") || facing.equalsIgnoreCase("Down"))
                Bukkit.broadcastMessage(" (and a bit " + subFacing.toLowerCase() + ")");


            Location loc = GetLocationInFrontOfPlayer(player);
            World w = loc.getWorld();

            DoorSize doorSize = ConvertDoorSizeFromRightUpIn(player, parsedArguments.doorSize);

            BigDoor newDoor = new BigDoor(parsedArguments.doorName, player, w, loc, doorSize);
            newDoor.Close();

            return Doors.add(newDoor);
        }

        sender.sendMessage("Console cannot currently create doors. Enable setting of location.");
        return false;
    }
}




class BigDoor {

    World _world;
    Location _root;
    CommandSender _messages;

    public String Name;
    public Player Owner;
    public DoorSize Size;
    public Block Trigger;

    public boolean isOpen = false;

    BigDoor(String name, Player owner, World w, Location root, DoorSize d) {
        Name = name;
        Owner = owner;
        _world = w;
        _root = root;
        Size = d;
    }

    public String Info() {
        return "Name        : " + Name + "\n" +
                "Owner       : " + (Owner == null ? "<no-one>" : Owner.getDisplayName()) + "\n" +
                "------------ -------------------------------\n" +
                "Description : A sliding door which opens from the middle outwards.\n" +
                "            : A vanishing door.\n" +
                "            : An opening door which opens from the top downwards.\n" +
                "Orientation : The top of the door is (up|north|etc), and it will open (up|north|etc)wards.\n" +
                "Location    : x,y,z to x,y,z \n" +
                "Trigger     : Button at x,y,z\n" +
                "Style       : SLIDING\n" +
                "Direction   : OUT\n" +
                "   (middle) : x = 435 z = 64\n" +
                "Empty Block : AIR\n" +
                "\n" +
                "Allow List  : (everyone)\n" +
                "Deny List   : (no-one)\n";
    }

    public void Toggle() {
        if (isOpen)
            Close();
        else
            Open();
    }

    public void Open() {
        // Should this be changing the world?
        //Location workingBlock = _root.clone();
        double rootX = _root.getX();
        double rootY = _root.getY();
        double rootZ = _root.getZ();

        int xMult = (Size.X < 0 ? -1 : +1);
        int yMult = (Size.Y < 0 ? -1 : +1);
        int zMult = (Size.Z < 0 ? -1 : +1);

        int xMax = Math.abs(Size.X);
        int yMax = Math.abs(Size.Y);
        int zMax = Math.abs(Size.Z);

        Location workingBlock = _root.clone();
        for (int x = 0; x < xMax; x++) {
            workingBlock.setX(rootX + (x * xMult));
            for (int y = 0; y < yMax; y++) {
                workingBlock.setY(rootY + (y * yMult));
                for (int z = 0; z < zMax; z++) {
                    workingBlock.setZ(rootZ + (z * zMult));

                    Block b = _world.getBlockAt(workingBlock);
                    b.setType(Material.AIR);

                }
            }
        }
        isOpen = true;
    }

    public void Close() {
        // This should definitely change the world!
        double rootX = _root.getX();
        double rootY = _root.getY();
        double rootZ = _root.getZ();

        int xMult = (Size.X < 0 ? -1 : +1);
        int yMult = (Size.Y < 0 ? -1 : +1);
        int zMult = (Size.Z < 0 ? -1 : +1);

        int xMax = Math.abs(Size.X);
        int yMax = Math.abs(Size.Y);
        int zMax = Math.abs(Size.Z);

        Location workingBlock = _root.clone();
        for (int x = 0; x < xMax; x++) {
            workingBlock.setX(rootX + (x * xMult));
            for (int y = 0; y < yMax; y++) {
                workingBlock.setY(rootY + (y * yMult));
                for (int z = 0; z < zMax; z++) {
                    workingBlock.setZ(rootZ + (z * zMult));

                    Block b = _world.getBlockAt(workingBlock);
                    b.setType(Material.STONE);

                }
            }
        }
        isOpen = false;
    }

}