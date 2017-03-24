package net.toolan.doorplugin;

import org.bukkit.Location;

public class CommandArguments {
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
