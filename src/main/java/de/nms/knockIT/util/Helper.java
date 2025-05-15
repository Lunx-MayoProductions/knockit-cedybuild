package de.nms.knockIT.util;

import com.mysql.jdbc.Driver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.nms.knockIT.KnockIT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Helper {
    private static String PREFIX = "<gray>[<gradient:yellow:gold>CedyKnock<gray>] <reset>";

    public static class Items{
        private static ItemStack stick;
        private static ItemStack blocks;
        private static Items instance;

        public Items(){
            stick = new ItemStack(Material.STICK);
            stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
            ItemMeta stickMeta = stick.getItemMeta();
            stickMeta.displayName(MiniMessage.miniMessage().deserialize("<gradient:gold:red>Knockback <gradient:gold:yellow>Stick"));
            stickMeta.setUnbreakable(true);
            stickMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stick.setItemMeta(stickMeta);

            blocks = new ItemStack(Material.SANDSTONE);
            var blockMeta = blocks.getItemMeta();
            blockMeta.displayName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Blocks"));
            blockMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            blocks.setItemMeta(blockMeta);

            instance = this;
        }

        public ItemStack STICK(){
            return stick;
        }

        public ItemStack BLOCK(){
            return blocks;
        }

        public static Items instance(){
            return instance;
        }
    }

    public static class Locations{
        private static Location spawn;
        private static Location loc1;
        private static Location loc2;
        private static Locations instance;

        public Locations(FileConfiguration conf){
            spawn = new Location(
                    Bukkit.getWorld(conf.getString("positions.spawn.world")),
                    conf.getDouble("positions.spawn.x"),
                    conf.getDouble("positions.spawn.y"),
                    conf.getDouble("positions.spawn.z"));

            loc1 = new Location(
                    Bukkit.getWorld(conf.getString("positions.arena.world")),
                    conf.getDouble("positions.arena.point1.x"),
                    conf.getDouble("positions.arena.point1.y"),
                    conf.getDouble("positions.arena.point1.z"));

            loc2 = new Location(
                    Bukkit.getWorld(conf.getString("positions.arena.world")),
                    conf.getDouble("positions.arena.point2.x"),
                    conf.getDouble("positions.arena.point2.y"),
                    conf.getDouble("positions.arena.point2.z"));
            instance = this;
        }


        public Location SPAWN(){
            return spawn;
        }

        public Location LOC1(){
            return loc1;
        }

        public Location LOC2(){
            return loc2;
        }

        public static Locations instance(){
            return instance;
        }

        public List<Location> ARENA(){
            List<Location> locations = new ArrayList<>();
            Vector start = loc1.toVector();
            Vector end = loc2.toVector();
            Vector direction = end.clone().subtract(start);
            double length = direction.length();
            direction.normalize();

            for (double i = 0; i <= length; i += 1.0) {
                Vector point = start.clone().add(direction.clone().multiply(i));
                locations.add(new Location(loc1.getWorld(), point.getX(), point.getY(), point.getZ()));
            }

            return locations;
        }

    }

    public static class Database {
        static Connection conn;

        public Database(String host, int port, String user, String password) throws SQLException {

            /*
                        HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/KnockIT?useSSL=false",  host, port));
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("de.nms.knockIT.libs.mysql.cj.jdbc.Driver"); // <-- WICHTIG
            config.setMaximumPoolSize(10);

            HikariDataSource dataSource = new HikariDataSource(config);

            conn  =dataSource.getConnection();
             */

            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            conn = DriverManager.getConnection("jdbc:sqlite:plugins/KnockIT/data.db");

            String createTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                    "player CHAR(36) PRIMARY KEY," +
                    "kills INT NOT NULL DEFAULT 0," +
                    "deaths INT NOT NULL DEFAULT 0," +
                    "ranking VARCHAR(50) NOT NULL" +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(createTable)) {
                stmt.execute();
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    new Helper.Locations(KnockIT.getPlugin(KnockIT.class).getConfig());
                    try {
                        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                            sendConsole("<yellow>Warn: MySQL Connection Timeouted");
                            sendConsole("<blue>INFO: <yellow>Trying to connect again...");
                            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/", host, port), user, password);
                            sendConsole("<green>SUCCESS: MySQL Connection created.");
                        }
                    } catch (SQLException e) {
                    }
                }
            }.runTaskTimer(KnockIT.getPlugin(KnockIT.class), 1200L, 1200L);
        }


        public static void insert(UUID player, Integer kills, Integer deaths, Rankings rankingE) {
            String dbProduct = null;
            try {
                dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            String sql;
            if (dbProduct.contains("sqlite")) {
                sql = "INSERT INTO player_data (player, kills, deaths, ranking) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(player) DO UPDATE SET " +
                        "kills = excluded.kills, " +
                        "deaths = excluded.deaths, " +
                        "ranking = excluded.ranking";
            } else {
                sql = "INSERT INTO player_data (player, kills, deaths, ranking) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "kills = VALUES(kills), " +
                        "deaths = VALUES(deaths), " +
                        "ranking = VALUES(ranking)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.toString());
                stmt.setInt(2, kills);
                stmt.setInt(3, deaths);
                stmt.setString(4, rankingE.ingN());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static void addKill(UUID player) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE player_data SET kills = kills + 1 WHERE player = ?")) {
                stmt.setString(1, player.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static void addDeath(UUID player) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE player_data SET deaths = deaths + 1 WHERE player = ?")) {
                stmt.setString(1, player.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static int getKills(UUID player) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT kills FROM player_data WHERE player = ?")) {
                stmt.setString(1, player.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("kills");
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static int getDeaths(UUID player) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT deaths FROM player_data WHERE player = ?")) {
                stmt.setString(1, player.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("kills");
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static boolean exists(UUID player) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT 1 FROM player_data WHERE player = ?")) {
                stmt.setString(1, player.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public static ResultSet getTopPlayersByKills(int limit) {
            try {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM player_data ORDER BY kills DESC LIMIT ?");
                stmt.setInt(1, limit);
                return stmt.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static enum Rankings{
        STARTER("Starter"),
        ADVANCED("Fortgeschritten"),
        PROFI("Professionell");

        private String disp;

        Rankings(String ranking) {
            disp = ranking;
        }

        public String ingN(){
            return disp;
        }
    }

    public static void sendConsole(String msg){
        Bukkit.getConsoleSender().sendMessage(comp(msg));
    }

    public static Component prefixedComp(String miniMessage){
        return MiniMessage.miniMessage().deserialize(PREFIX + miniMessage);
    }

    public static Component comp(String miniMessage){
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }
}
