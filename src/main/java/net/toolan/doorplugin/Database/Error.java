package net.toolan.doorplugin.Database;

import java.util.logging.Level;

/**
 * Copied from https://www.spigotmc.org/threads/how-to-sqlite.56847/
 * Thanks to cyberkm aka pablo67340
 */

public class Error {
    public static void execute(Main plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(Main plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
