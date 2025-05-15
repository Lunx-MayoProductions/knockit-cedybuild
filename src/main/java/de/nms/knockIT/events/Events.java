package de.nms.knockIT.events;

import de.nms.knockIT.KnockIT;
import de.nms.knockIT.scoreboard.ScoreboardManager;
import de.nms.knockIT.util.Helper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Events implements Listener {

    @EventHandler
    public void me(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        p.getInventory().setItem(1, Helper.Items.instance().STICK());
        p.getInventory().setItem(2, Helper.Items.instance().BLOCK());
    }

    @EventHandler
    public void se(PlayerJoinEvent event){
        event.joinMessage(Helper.prefixedComp("<gray>[<green>+<gray>] <yellow>$p".replace("$p", event.getPlayer().getName()))); //1
        new Helper.Locations(KnockIT.getPlugin(KnockIT.class).getConfig());
        event.getPlayer().teleport(Helper.Locations.instance().SPAWN());
        Player p = event.getPlayer();
        p.getInventory().clear();
        p.getInventory().setItem(0, Helper.Items.instance().STICK());
        p.getInventory().setItem(1, Helper.Items.instance().BLOCK());
        p.sendMessage(Helper.prefixedComp("<green>Willkommen zu CedyKnock! Unsere Variante von KnockIT!"));

        if (p.hasPlayedBefore() == false){
            Helper.Database.insert(p.getUniqueId(), 0, 0, Helper.Rankings.STARTER);
        }

        ScoreboardManager.init(event.getPlayer());
    }

    @EventHandler
    public void de(PlayerDeathEvent e){
        e.deathMessage(Helper.prefixedComp(String.format("<gradient:red:dark_red:red>Player %s was killed by %s", e.getPlayer().getName(), e.getPlayer().getKiller() == null ? "Console" : e.getPlayer().getKiller().getName())));
        Helper.Database.addDeath(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void edbee(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim) || !(e.getDamager() instanceof Player damager)) return;
        Location loc = damager.getLocation();
        Location min = Helper.Locations.instance().LOC1();
        Location max = Helper.Locations.instance().LOC2();
        if (!loc.getWorld().equals(min.getWorld())) return;
        if (!(loc.getX() >= Math.min(min.getX(), max.getX()) && loc.getX() <= Math.max(min.getX(), max.getX())
                && loc.getY() >= Math.min(min.getY(), max.getY()) && loc.getY() <= Math.max(min.getY(), max.getY())
                && loc.getZ() >= Math.min(min.getZ(), max.getZ()) && loc.getZ() <= Math.max(min.getZ(), max.getZ()))) return;
        victim.damage(0.0);
        victim.setVelocity(damager.getLocation().getDirection().setY(0.5).normalize().multiply(1.3));
    }

    @EventHandler
    public void bbe(BlockBreakEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void bpe(BlockPlaceEvent e){
        boolean inArena = Helper.Locations.instance().ARENA().stream()
                .anyMatch(loc -> e.getPlayer().getLocation().getBlock().equals(loc.getBlock()));
        if(!inArena){
            e.setCancelled(true);
            return;
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                e.getBlock().breakNaturally();
            }
        }.runTaskLater(KnockIT.getPlugin(KnockIT.class), 500L);
    }
}
