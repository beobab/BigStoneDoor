package net.toolan.doorplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;

import java.util.*;

public class BigDoor {

    public String Name;
    public List<String> TriggerKeys;
    public boolean isOpen = false;
    public boolean isModified = false;

    public String getOwnerName() { return _playerUID == null ? "<no-one>" : getOwner().getName(); }
    public OfflinePlayer getOwner() {return (_playerUID == null ? null : Bukkit.getOfflinePlayer(_playerUID)); }

//    //maybe move this to BigDoor?
//    public static String DoorKey (BigDoor d) {
//        return (d == null ? "" : ((d.Name == null ? "" : d.Name) + "|" + (d._playerUID == null ?  "" : d.getOwnerName())));
//    }

    private UUID _playerUID;

    private String _worldName;
    public String getWorldName() { return _worldName; }

    private World _world;
    public World getWorld() {
        if (_world == null)
            _world = Bukkit.getWorld(_worldName);
        return _world;
    }

    private Location _root;
    public Location getRoot() { return _root; }
    private DoorSize _size;
    public DoorSize getSize() { return _size; }

    BigDoor(String name, UUID ownerId, String worldName, Location root, DoorSize d) {
        Name = name;
        _playerUID = ownerId;
        _worldName = worldName;
        _root = root;
        _size = d;

        TriggerKeys = new ArrayList<>();

        isModified = true;
    }


    public void AddTrigger(String s) {
        TriggerKeys.add(s);
        isModified = true;
    }
    public void RemoveTrigger(String s) {
        TriggerKeys.remove(s);
        isModified = true;
    }


    private String InfoLocation() {
        if (_root == null) return "WARNING! root is null!";
        if (_size == null) return "WARNING! size is null!";
        return "Location    : " +
                Integer.toString(_root.getBlockX()) + "," +
                Integer.toString(_root.getBlockY()) + "," +
                Integer.toString(_root.getBlockZ()) + " to " +
                Integer.toString(_root.getBlockX() + _size.X) + "," +
                Integer.toString(_root.getBlockY() + _size.Y) + "," +
                Integer.toString(_root.getBlockZ() + _size.Z) +
                (getWorld() == null ? "" : " in " + getWorld().getName()) + "\n";
    }

    private String InfoTriggers() {
        if (TriggerKeys == null) return "Trigger     : NONE";

        String str = "";
        for (String key : TriggerKeys) {
            if (str.equals("")) {
                str = key + "\n";
            } else {
                str = "            : " + key + "\n";
            }
        }
        if (str.equals("")) return "";
        return "Trigger     : " + str;
    }

    public String Info() {
        return "Name        : " + Name + "\n" +
                "Owner       : " + getOwnerName() + "\n" +
                "------------ -------------------------------\n" +
                "Description : A vanishing door.\n" +
                //"            : A sliding door which opens from the middle outwards.\n" +
                //"            : An opening door which opens from the top downwards.\n" +
                //"Orientation : The top of the door is (up|north|etc), and it will open (up|north|etc)wards.\n" +
                InfoLocation() +
                InfoTriggers();
//                "Style       : SLIDING\n" +
//                "Direction   : OUT\n" +
//                "   (middle) : x = 435 z = 64\n" +
//                "Empty Block : AIR\n" +
//                "\n" +
//                "Allow List  : (everyone)\n" +
//                "Deny List   : (no-one)";
    }

//    public void Toggle() {
//        if (isOpen)
//            Close();
//        else
//            Open();
//    }

    public boolean isAirDoor() {
        if (isOpen) {
            for (String value : SavedBlock.values())
                if (DoorInterpreter.InterpretMaterial(value).getItemType() != Material.AIR)
                    return false;
            return true;
        } else {
            List<Material> lst = new ArrayList<>();
            ApplyToAllBlocks((Block b) -> lst.add(b.getType()));
            for (Material m : lst)
                if (m != Material.AIR)
                    return false;
            return true;
        }
    }
    public void FillWithLeaves() {
        ApplyToAllBlocks((Block b) -> b.setType(Material.LEAVES));
    }

    interface OperateOnBlock {
        void operation(Block b);
    }

    interface OperateOnLocation {
        void operation(Location location);
    }

    private void ApplyToAllBlocks(OperateOnLocation command) {
        if (command == null) return;
        double rootX = _root.getX();
        double rootY = _root.getY();
        double rootZ = _root.getZ();

        int xMult = (_size.X < 0 ? -1 : +1);
        int yMult = (_size.Y < 0 ? -1 : +1);
        int zMult = (_size.Z < 0 ? -1 : +1);

        int xMax = Math.abs(_size.X);
        int yMax = Math.abs(_size.Y);
        int zMax = Math.abs(_size.Z);

        Location workingBlock = _root.clone();
        workingBlock.setWorld(getWorld());

        for (int x = 0; x < xMax; x++) {
            workingBlock.setX(rootX + (x * xMult));
            for (int y = 0; y < yMax; y++) {
                workingBlock.setY(rootY + (y * yMult));
                for (int z = 0; z < zMax; z++) {
                    workingBlock.setZ(rootZ + (z * zMult));

                    command.operation(workingBlock);

                }
            }
        }
    }

    private void ApplyToAllBlocks(OperateOnBlock command) {
        if (command == null) return;

        ApplyToAllBlocks((Location location) -> {
            Block b = getWorld().getBlockAt(location);
            command.operation(b);
        });
    }

    public Map<String, String> SavedBlock = new HashMap<>();
    public String SavedDefaultBlock = null;

    public void Toggle() {
        // Save what is currently in the world.
        Map<String, String> currentBlocks = ReadMapFromWorld();
        String mostFrequentCurrent = MostFrequentlyHashed(currentBlocks);
        RemoveValueFromHash(currentBlocks, mostFrequentCurrent);


        // This should only be null on first open, but in case not, use sensible defaults.
        if (SavedDefaultBlock == null)
            SavedDefaultBlock = DoorInterpreter.MaterialAsString(
                    isOpen ? new MaterialData(Material.STONE) : new MaterialData(Material.AIR)
            );

        SetMapToWorld(SavedBlock, SavedDefaultBlock);

        // Now we have written the saved one to the world, save what we previously had in variables.
        SavedDefaultBlock = mostFrequentCurrent;
        SavedBlock = currentBlocks;

        isOpen = !isOpen;
        isModified = true;
    }

    // Set the map to the world, but if there isn't an entry, assume it's the default block.
    // On first open, there will be no entries in map, so all will be set to Material.AIR.
    private void SetMapToWorld(Map<String, String> map, String defaultBlock) {
        ApplyToAllBlocks((Location location) -> {
            Block b = getWorld().getBlockAt(location);
            String material = map.get(DoorInterpreter.LocationAsString(location));
            if (material == null)
                material = defaultBlock;
            MaterialData md = DoorInterpreter.InterpretMaterial(material);
            b.setType(md.getItemType());
            b.setData(md.getData());
        });
    }

    private Map<String, String> ReadMapFromWorld() {
        Map<String, String> currentBlocks = new HashMap<>();
        ApplyToAllBlocks((Location location) -> {
            Block b = getWorld().getBlockAt(location);
            MaterialData md = DoorInterpreter.MaterialDataFromBlock(b);
            currentBlocks.put(
                    DoorInterpreter.LocationAsString(location),
                    DoorInterpreter.MaterialAsString(md)
            );
        });
        return currentBlocks;
    }

    private void RemoveValueFromHash(Map<String, String> map, String value) {
        map.values().removeIf(val -> value.equals(val));
    }

    private String MostFrequentlyHashed(Map<String, String> map) {
        Integer occurences = 0;
        String mostFrequent = "";
        Map<String, Integer>reverseHash = new HashMap<>();
        for(String item : map.values()) {
            Integer itemCount = reverseHash.get(item);
            itemCount = (itemCount == null ? 0 : itemCount) + 1;
            if (itemCount > occurences) {
                occurences = itemCount;
                mostFrequent = item;
            }
            reverseHash.put(item, itemCount);
        }
        return mostFrequent;
    }

    public void Open() {
        if (isOpen) return;

        Toggle();

        return;
//        // Save all blocks currently there:
//        ApplyToAllBlocks((Location location) -> {
//            Block b = getWorld().getBlockAt(location);
//            MaterialData md = DoorInterpreter.MaterialDataFromBlock(b);
//            SavedBlock.put(DoorInterpreter.LocationAsString(location),
//                           DoorInterpreter.MaterialAsString(md));
//        });
//
//        // Need to check if these blocks are loaded.
//        ApplyToAllBlocks((Block b) -> b.setType(Material.AIR));
//        isOpen = true;
//        isModified = true;
//        //send "You feel a gust of air" to nearby players. Maybe a whoosh.
    }

    public void Close() {
        if (!isOpen) return;

        Toggle();
        return;

//        // This should definitely change the world!
//        ApplyToAllBlocks((Location location) -> {
//            Block b = getWorld().getBlockAt(location);
//            String material = SavedBlock.get(DoorInterpreter.LocationAsString(location));
//            MaterialData md = (material == null ?
//                                    new MaterialData(Material.STONE) :
//                                    DoorInterpreter.InterpretMaterial(material));
//            b.setType(md.getItemType());
//            b.setData(md.getData());
//        });
//
//        // To save space, don't remember this list whilst it's closed.
//        SavedBlock.clear();
//
//        isOpen = false;
//        isModified = true;
//        // Send "You feel a thud of the ground in your feet" to nearby players.
    }

}
