package net.toolan.doorplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BigDoorStorageClass {
    public String doorName;
    public String worldName;
    public String owner;
    public String doorSize;
    public String doorRoot;

    public String state;
    public String openBlockMaterial;

    public List<String> Triggers;
    public Map<String, String> SavedBlocks;

    public BigDoor getDoor(DoorBell doorbell) {

        UUID ownerId = UUID.fromString(owner);

        Location root = DoorInterpreter.InterpretLocation((World) null, doorRoot);
        DoorSize size = DoorInterpreter.InterpretSize(doorSize);

        BigDoor d = new BigDoor(
                doorName,
                ownerId,
                worldName,
                root,
                size
        );
        d.TriggerKeys = Triggers;
        d.SavedBlock = SavedBlocks;
        d.SavedDefaultBlock = openBlockMaterial;

        d.isOpen = (state.equalsIgnoreCase("OPEN"));

        for (String key : Triggers) {
            doorbell.setDoorBell(key, d);
        }

        return d;
   }

   public static BigDoorStorageClass FromDoor(BigDoor door) {
       BigDoorStorageClass ds = new BigDoorStorageClass();
       ds.doorName = door.Name;
       ds.worldName = door.getWorldName();
       ds.owner = door.getOwner().getUniqueId().toString();
       ds.doorSize = DoorInterpreter.DoorSizeAsString(door.getSize());
       ds.doorRoot = DoorInterpreter.LocationAsString(door.getRoot());

       ds.state = door.isOpen ? "OPEN" : "CLOSED";
       ds.openBlockMaterial = door.SavedDefaultBlock;
//       ds.openBlockMaterial = Material.AIR.toString();

       ds.Triggers = door.TriggerKeys;
       ds.SavedBlocks = door.SavedBlock;

       return ds;
   }

    @Override
    public String toString() {
        return "Name: " + (doorName == null ? "(null)" : doorName) + "\n" +
                "World: " + (worldName == null ? "(null)" : worldName) + "\n" +
                "Location: " + (doorRoot == null ? "(null)" : doorRoot.toString()) + "\n" +
                "Size: " + (doorSize == null ? "(null)" : doorSize.toString());
    }
}
