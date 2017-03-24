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
import java.util.List;

public class BigDoor {

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

    public String CurrentDoorMaterial() {
        // start in one corner, and loop through trying to find the biggest chunk of the same material. Data counts as different material.

        String doorBlocks = "DOOR:\n";
        for (Block block : GetDoorBlocks()) {
            doorBlocks += "  " + this.SerialiseBlock(block) + "\n";
        }
        if (Trigger != null) {
            doorBlocks += "TRIGGER:\n";
            doorBlocks += "  " + this.SerialiseBlock(Trigger);
        }
        String result = StringCompressor.crunch(doorBlocks);

        Bukkit.broadcastMessage("DoorMaterial: " + result);
        return result;
    }

    private List<Block> GetDoorBlocks() {
        List<Block> lst = new ArrayList<>();
        ApplyToAllBlocks(lst::add);
        return lst;
    }

    @SuppressWarnings("deprecation")
    public String SerialiseBlock(Block b) {
        Location l = b.getLocation();
        String location =
                Integer.toString(l.getBlockX()) + "," +
                Integer.toString(l.getBlockY()) + "," +
                Integer.toString(l.getBlockZ());

        Material m = b.getType();
        Byte data = b.getData();
        return location + "|" + m.toString() + (data > 0 ? "," + data.toString() : "");
    }

    class BlockInfo {
        public Location location;
        public MaterialData materialData;
        BlockInfo (Location l, MaterialData md) {
            location = l;
            materialData = md;
        }
    }

    @SuppressWarnings("deprecation")
    public BlockInfo DeserialiseBlock(World w, String s) {
        String args[] = s.split("\\|");

        final Location l = DoorInterpreter.InterpretLocation(w, args[0]);
        final MaterialData md = DoorInterpreter.InterpretMaterial(args[1]);

        return new BlockInfo(l, md);
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
                "Deny List   : (no-one)";
    }

    public void Toggle() {
        if (isOpen)
            Close();
        else
            Open();
    }

    interface OperateOnBlock {
        void operation(Block b);
    }

    private void ApplyToAllBlocks(OperateOnBlock command) {
        if (command == null) return;

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
                    command.operation(b);

                }
            }
        }
    }

    public void Open() {
        // Should this be changing the world?
        //Location workingBlock = _root.clone();
        ApplyToAllBlocks((b) -> b.setType(Material.AIR));
        isOpen = true;

        //send "You feel a gust of air" to nearby players. Maybe a whoosh.
    }

    public void Close() {
        // This should definitely change the world!
        ApplyToAllBlocks((b) -> b.setType(Material.STONE));
        isOpen = false;

        // Send "You feel a thud of the ground in your feet" to nearby players.
    }

}
