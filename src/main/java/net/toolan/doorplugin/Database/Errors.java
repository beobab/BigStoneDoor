package net.toolan.doorplugin.Database;

/**
 * Copied from https://www.spigotmc.org/threads/how-to-sqlite.56847/
 * Thanks to cyberkm aka pablo67340
 */
public class Errors {
    public static String sqlConnectionExecute(){
        return "Couldn't execute MySQL statement: ";
    }
    public static String sqlConnectionClose(){
        return "Failed to close MySQL connection: ";
    }
    public static String noSQLConnection(){
        return "Unable to retreive MYSQL connection: ";
    }
    public static String noTableFound(){
        return "Database Error: No Table Found";
    }
}
