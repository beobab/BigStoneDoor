package net.toolan.doorplugin.Database;

/**
 * Copied from https://www.spigotmc.org/threads/how-to-sqlite.56847/
 * Thanks to cyberkm aka pablo67340
 */
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;


public class SQLite extends Database {
    String dbname;
    public SQLite(Main instance){
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.filename", "doors"); // Set the table name here e.g player_kills
    }

    public String SQLiteCreateDoorTable = "CREATE TABLE IF NOT EXISTS door (" + // make sure to put your table name in here too.
            "`name` varchar(32) NOT NULL," + // This creates the different columns you will save data too. varchar(32) Is a string, int = integer
            "`player` varchar(36) NOT NULL," +
            "`world` varchar(32) NOT NULL," +
            "`root` varchar(32) NOT NULL," +
            "`size` varchar(32) NOT NULL," +
            "`state` varchar(32) NOT NULL," +
            "`openBlockMaterial` varchar(32) NOT NULL," +
            "PRIMARY KEY (`name`)" +  // This is creating 3 columns Player, Kills, Total. Primary key is what you are going to use as your indexer. Here we want to use player so
            ");"; // we can search by player, and get kills and total. If you some how were searching kills it would provide total and player.

    public String SQLiteCreateTriggerTable = "CREATE TABLE IF NOT EXISTS doorTrigger (" + // make sure to put your table name in here too.
            "`triggerId` integer NOT NULL," +
            "`name` varchar(32) NOT NULL," + // This creates the different columns you will save data too. varchar(32) Is a string, int = integer
            "`blockKey` varchar(32) NOT NULL," +
            "PRIMARY KEY (`triggerId`)" +  // This is creating 3 columns Player, Kills, Total. Primary key is what you are going to use as your indexer. Here we want to use player so
            ");"; // we can search by player, and get kills and total. If you some how were searching kills it would provide total and player.


    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        Bukkit.broadcastMessage("Getting connection");
        connection = getSQLConnection();
        Bukkit.broadcastMessage("Load door table");
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateDoorTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.broadcastMessage("Load trigger table");
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTriggerTable);
            s.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
