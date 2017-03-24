package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public String CurrentMaterial() {
        // start in one corner, and loop through trying to find the biggest chunk of the same material. Data counts as different material.

        String doorBlocks = "DOOR:\n";
        for (Block block : GetDoorBlocks()) {
            doorBlocks += "  " + this.SerialiseBlock(block) + "\n";
        }
        if (Trigger != null) {
            doorBlocks += "TRIGGER:\n";
            doorBlocks += "  " + this.SerialiseBlock(Trigger);
        }
        String result = b64encode(compress(doorBlocks));

        Bukkit.broadcastMessage("DoorMaterial: " + result);
        return result;
    }

    private List<Block> GetDoorBlocks() {
        double rootX = _root.getX();
        double rootY = _root.getY();
        double rootZ = _root.getZ();

        int xMult = (Size.X < 0 ? -1 : +1);
        int yMult = (Size.Y < 0 ? -1 : +1);
        int zMult = (Size.Z < 0 ? -1 : +1);

        int xMax = Math.abs(Size.X);
        int yMax = Math.abs(Size.Y);
        int zMax = Math.abs(Size.Z);

        List<Block> lst = new ArrayList<>();

        Location workingBlock = _root.clone();
        for (int x = 0; x < xMax; x++) {
            workingBlock.setX(rootX + (x * xMult));
            for (int y = 0; y < yMax; y++) {
                workingBlock.setY(rootY + (y * yMult));
                for (int z = 0; z < zMax; z++) {
                    workingBlock.setZ(rootZ + (z * zMult));

                    lst.add(_world.getBlockAt(workingBlock));
                }
            }
        }
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
        return location + "|" + m.toString() + (data != null ? "," + data.toString() : "");
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
        final Location l = InterpretLocation(w, args[0]);
        final MaterialData md = InterpretMaterial(args[1]);

        return new BlockInfo(l, md);
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

    private Location InterpretLocation(World world, String location) {
        String[] args = location.split(",");

        return new Location(
            world,
            Double.parseDouble(args[0]),
            Double.parseDouble(args[1]),
            Double.parseDouble(args[2])
        );
    }

    private static String CharsetUTF8 = "UTF-8";

    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(CharsetUTF8));
            gzip.close();
            return out.toString(CharsetUTF8);
        } catch (Exception ex){
            return str; // can't compress it. :(
        }
    }

    public static String decompress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        try {
            InputStream input = new ByteArrayInputStream(str.getBytes(CharsetUTF8));
            InputStream gzip = new GZIPInputStream(input);
            Reader decoder = new InputStreamReader(gzip, CharsetUTF8);
            BufferedReader buffered = new BufferedReader(decoder);

            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();

            for (; ; ) {
                int rsz = buffered.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();

        } catch (Exception ex){
            return ex.getMessage(); // can't decompress it. :(
        }
    }

    private static String b64encode(String s) {
        try {
            byte[] encodedBytes = Base64.getEncoder().encode(s.getBytes(CharsetUTF8));
            return new String(encodedBytes, CharsetUTF8);
        } catch (Exception ex) {
            return "unable to encode";
        }
    }
    private static String b64decode(String s) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(s.getBytes(CharsetUTF8));
            return new String(decodedBytes, CharsetUTF8);
        } catch (Exception ex) {
            return ex.getMessage() + " => unable to decode";
        }
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
                "Deny List   : (no-one)\n" +
                compress("Hello")+ "\n" +
                decompress(compress("Hello")) + "\n" +
                b64decode(b64encode("There")) + "\n" +
                CurrentMaterial()+ "\n" +
                decompress(b64decode("H++/vQgAAAAAAAAAc++/ve+/vQ/vv73vv71SUO+/vTU0MO+/vTE177+90bU077+9CQ7vv73vv71z77+9MUARNe+/vRA1x6rvv70c77+9Wgvvv71qLVDvv70CAGTvv73Ine+/vQAAAA=="));
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
