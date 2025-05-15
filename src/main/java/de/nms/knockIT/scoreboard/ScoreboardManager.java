package de.nms.knockIT.scoreboard;

import de.nms.knockIT.KnockIT;
import de.nms.knockIT.util.Helper;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    public static void init(Player player){
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("knockit", Criteria.DUMMY, MiniMessage.miniMessage().deserialize("<gradient:yellow:gold>KnockIT"));
        Score kills = obj.getScore("kills");
        Score aKills = obj.getScore("a_kills");
        Score name = obj.getScore("name");
        Score aName = obj.getScore("a_name");
        Score deaths = obj.getScore("deaths");
        Score aDeaths = obj.getScore("a_deaths");

        kills.setScore(10);
        aKills.setScore(9);
        name.setScore(8);
        aName.setScore(7);
        deaths.setScore(6);
        aDeaths.setScore(5);

        kills.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Deine Kills:"));
        aKills.customName(MiniMessage.miniMessage().deserialize("<green>" + Helper.Database.getKills(player.getUniqueId())));

        name.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Dein Name:"));
        aName.customName(MiniMessage.miniMessage().deserialize("<gradient:yellow:gold>"+player.getName()));

        deaths.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Deine Deaths:"));
        aDeaths.customName(MiniMessage.miniMessage().deserialize("<red>"+Helper.Database.getDeaths(player.getUniqueId())));

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        new BukkitRunnable() {
            /**
             * Runs this operation.
             */
            @Override
            public void run() {
                kills.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Deine Kills:"));
                aKills.customName(MiniMessage.miniMessage().deserialize("<green>" + Helper.Database.getKills(player.getUniqueId())));

                name.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Dein Name:"));
                aName.customName(MiniMessage.miniMessage().deserialize("<gradient:yellow:gold>"+player.getName()));

                deaths.customName(MiniMessage.miniMessage().deserialize("<gradient:aqua:green>Deine Deaths:"));
                aDeaths.customName(MiniMessage.miniMessage().deserialize("<red>"+Helper.Database.getDeaths(player.getUniqueId())));
            }
        }.runTaskTimer(KnockIT.getPlugin(KnockIT.class), 30L, 30L);

        player.setScoreboard(board);

    }
}
