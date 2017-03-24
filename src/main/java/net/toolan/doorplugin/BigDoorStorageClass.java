package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BigDoorStorageClass {
    String doorName;
    String worldName;
    String ownerName;
    String doorSize;
    String doorRoot;

    String doorData; // Base64 encoded door.

    @SuppressWarnings("deprecation")
    public BigDoor getDoor() {

        DoorInterpreter interpreter = new DoorInterpreter();

        Player owner = Bukkit.getPlayer(ownerName);
        World world = Bukkit.getWorld(worldName);
        Location root = interpreter.InterpretLocation(world, doorRoot);
        DoorSize size = interpreter.InterpretSize(doorSize);

        return new BigDoor(
                doorName,
                owner,
                world,
                root,
                size
        );
   }

    @Override
    public String toString() {
        return "Name: " + (doorName == null ? "(null)" : doorName) + "\n" +
                "Owner: " + (ownerName == null ? "(null)" : ownerName) + "\n" +
                "World: " + (worldName == null ? "(null)" : worldName) + "\n" +
                "Location: " + (doorRoot == null ? "(null)" : doorRoot.toString()) + "\n" +
                "Size: " + (doorSize == null ? "(null)" : doorSize.toString());
    }
}
