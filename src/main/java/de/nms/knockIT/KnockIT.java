package de.nms.knockIT;

import de.nms.knockIT.events.Events;
import de.nms.knockIT.util.Helper;
import de.nms.knockIT.util.Helper.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

import static de.nms.knockIT.util.Helper.sendConsole;
public final class KnockIT extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveConfig();

        sendConsole("|------------------------|");
        sendConsole("|  <green>Enabling CedyKnock    |");
        sendConsole("|   Version: 1.0.0       |");
        sendConsole("|------------------------|");
        sendConsole("");
        sendConsole("");

        new Helper.Items();

        new Helper.Locations(KnockIT.getPlugin(KnockIT.class).getConfig());

        try {
            new Database(getConfig().getString("database.host"), getConfig().getInt("database.port"), getConfig().getString("database.user"), getConfig().getString("database.password"));
        } catch (SQLException e) {
            sendConsole("<red>ERROR: MySQL could not be connected! Disabling...");
            sendConsole("<yellow>Error: " +e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        sendConsole("<green>MySQL connected.");

        Bukkit.getPluginManager().registerEvents(new Events(), this);

    }
    @Override
    public void onDisable() {
    }
}
