package net.toolan.doorplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

/**
 * Created by jonathan on 24/03/2017.
 */
public class DoorInterpreter {

    @SuppressWarnings("deprecation")
    public static MaterialData InterpretMaterial(String material) {
        String[] args = material.split(",");
        Material m = Material.valueOf(args[0]);
        if (args.length > 1) {
            byte b = (byte) (Integer.parseInt(args[1]));
            return new MaterialData(m, b);
        } else {
            return new MaterialData(m);
        }
    }

    public static LocationPair InterpretPair(Player player, String location) {
        String[] args = location.split("-");
        Location loc1 = InterpretLocation(player, args[0]);
        Location loc2;
        if (args.length > 1)
            loc2 = InterpretLocation(player, args[1]);
        else
            loc2 = loc1.clone();
        return new LocationPair(loc1, loc2);
    }
    public static Location InterpretLocation(Player player, String location) {
        String[] args = location.split(",");
        Location root = player.getLocation();

        return new Location(
                player.getWorld(),
                root.getX() + Double.parseDouble(args[0]),
                root.getY() + Double.parseDouble(args[1]),
                root.getZ() + Double.parseDouble(args[2])
        );
    }

    public static Location InterpretLocation(World world, String location) {
        String[] args = location.split(",");

        return new Location(
                world,
                Double.parseDouble(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2])
        );
    }

    public static String LocationAsString(Location loc) {
        return Integer.toString(loc.getBlockX()) + "," +
                Integer.toString(loc.getBlockY()) + "," +
                Integer.toString(loc.getBlockZ());
    }

    public static DoorSize InterpretSize(String doorSize) {
        String[] args = doorSize.split(",");
        return new DoorSize(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2]));
    }

    public static String DoorSizeAsString(DoorSize doorSize) {
        return Integer.toString(doorSize.X) + "," +
                Integer.toString(doorSize.Y) + "," +
                Integer.toString(doorSize.Z);
    }
}

