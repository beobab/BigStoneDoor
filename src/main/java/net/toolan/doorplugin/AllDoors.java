package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.toolan.doorplugin.DoorInterpreter.InterpretMaterial;

public class AllDoors {
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
         LocationPair pair = DoorInterpreter.InterpretPair(player, args[0]);
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

                    // Not sure of ordering here...
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

    public List<String> ListDoorNames() {
        List<String> lst = new ArrayList<>();
        for (BigDoor b : Doors) {
            lst.add(b.Name);
        }
        return lst;
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
