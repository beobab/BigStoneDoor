package net.toolan.doorplugin.Database;

/**
 * Copied from https://www.spigotmc.org/threads/how-to-sqlite.56847/
 * Thanks to cyberkm aka pablo67340
  */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.toolan.doorplugin.AllDoors;
import net.toolan.doorplugin.BigDoor;
import net.toolan.doorplugin.BigDoorStorageClass;
import net.toolan.doorplugin.DoorBell;


public abstract class Database {
    Main plugin;
    Connection connection;

    public Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("select sqlite_version();");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    private List<BigDoorStorageClass> getAllDoors() {
        List<BigDoorStorageClass> lst = new ArrayList<BigDoorStorageClass>();
        NonQuery((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM door;"
            )) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BigDoorStorageClass door = new BigDoorStorageClass();
                        door.doorName = rs.getString("name");
                        door.owner = rs.getString("player");
                        door.worldName = rs.getString("world");
                        door.doorRoot = rs.getString("root");
                        door.doorSize = rs.getString("size");
                        door.state = rs.getString("state");
                        door.openBlockMaterial = rs.getString("openBlockMaterial");

                        try(PreparedStatement psT = conn.prepareStatement(
                                "SELECT blockKey FROM doorTrigger WHERE name = ?;"
                        )) {
                            List<String> triggers = new ArrayList<>();
                            psT.setString(1, door.doorName);
                            try(ResultSet rsT = psT.executeQuery()) {
                                while (rsT.next()) {
                                    triggers.add(rsT.getString("blockKey"));
                                }
                            }
                            door.Triggers = triggers;
                        }

                        lst.add(door);
                    }
                }
            }
        });
        return lst;
    }

    public void saveDoor(String name, String owner, String world, String root, String size, String state, String openBlockMaterial, List<String> triggers) {
        NonQuery((Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "REPLACE INTO door" +
                            " (name, player, world, root, size, state, openBlockMaterial)" +
                            " VALUES(?,?,?,?,?,?,?);"))
            {
                ps.setString(1, name); // YOU MUST put these into this line!! And depending on how many
                ps.setString(2, owner);
                ps.setString(3, world);
                ps.setString(4, root);
                ps.setString(5, size);
                ps.setString(6, state);
                ps.setString(7, openBlockMaterial);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM doorTrigger WHERE name = ?;")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }

            for (String key : triggers) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO doorTrigger" +
                            " (name, blockKey)" +
                            " VALUES(?,?);"))
                {
                    ps.setString(1, name);
                    ps.setString(2, key);
                    ps.executeUpdate();
                }
            }
        });
    }

    interface ISqlCommand {
        void execute(Connection conn) throws Exception;
    }

    public void NonQuery(ISqlCommand sql) {
        if (sql == null) return;
        Connection conn = null;
        try {
            conn = getSQLConnection();
            sql.execute(conn);

            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Some other exception, maybe parameter related.", ex);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }

    public void saveDoors(AllDoors allDoors) {
        for (BigDoor door : allDoors.Doors) {
            if (door.isModified) {
                BigDoorStorageClass dc = BigDoorStorageClass.FromDoor(door);
                saveDoor(dc.doorName, dc.owner,
                        dc.worldName, dc.doorRoot, dc.doorSize,
                        dc.state, dc.openBlockMaterial,
                        dc.Triggers);
            }
        }
    }

    public AllDoors loadDoors(DoorBell doorbell) {
        AllDoors allDoors = new AllDoors();

        for (BigDoorStorageClass dc : getAllDoors()) {
            BigDoor door = dc.getDoor(doorbell);
            door.isModified = false;
            allDoors.Doors.add(door);
        }

        return allDoors;
    }
}