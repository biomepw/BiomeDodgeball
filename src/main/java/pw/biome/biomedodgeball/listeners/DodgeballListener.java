package pw.biome.biomedodgeball.listeners;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import pw.biome.biomedodgeball.BiomeDodgeball;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;

import java.util.ArrayList;
import java.util.List;

public class DodgeballListener implements Listener {

    @Getter
    private static final List<Integer> droppedItemsEntityIds = new ArrayList<>();

    /**
     * Method to handle when a player is hit by a dodgeball
     *
     * @param event projectile hit event
     */
    @EventHandler
    public void dodgeballHit(ProjectileHitEvent event) {
        if (!BiomeDodgeball.getInstance().getGameManager().isGameRunning()) return;
        Entity entity = event.getHitEntity();
        Projectile projectile = event.getEntity();

        if (projectile instanceof Snowball) {
            Snowball dodgeball = (Snowball) projectile;
            if (entity instanceof Player) {
                Player player = (Player) entity;
                DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

                if (dodgeballPlayer != null && dodgeballPlayer.isCurrentlyIn()) {
                    ProjectileSource projectileSource = dodgeball.getShooter();

                    if (projectileSource instanceof Player) {
                        Player shooter = (Player) projectileSource;

                        DodgeballPlayer shooterDodgeballPlayer = DodgeballPlayer.getFromUUID(shooter.getUniqueId());

                        if (shooterDodgeballPlayer != null && shooterDodgeballPlayer.isCurrentlyIn()) {
                            // If they are same team, do nothing
                            if (shooterDodgeballPlayer.getCurrentTeam() == dodgeballPlayer.getCurrentTeam()) return;

                            Bukkit.broadcastMessage(shooterDodgeballPlayer.getCurrentTeam().getTeamColour() +
                                    shooterDodgeballPlayer.getDisplayName() + " just shot " +
                                    dodgeballPlayer.getCurrentTeam().getTeamColour() + dodgeballPlayer.getDisplayName());

                            shooterDodgeballPlayer.incrementHits();
                            dodgeballPlayer.decrementLives();

                            // Poll the game to see if players are in
                            BiomeDodgeball.getInstance().getGameManager().checkGameStatus();
                        }
                    }
                }
            }

            // Spawn a snowball where the dodgeball landed
            Location hitLocation = projectile.getLocation();
            Item item = hitLocation.getWorld().dropItem(hitLocation, new ItemStack(Material.SNOWBALL));
            droppedItemsEntityIds.add(item.getEntityId());
        }
    }


    /**
     * Method to handle when projectiles are launched
     *
     * @param event projectile launch event
     */
    @EventHandler
    public void dodgeballShoot(ProjectileLaunchEvent event) {
        if (!BiomeDodgeball.getInstance().getGameManager().isGameRunning()) return;
        Projectile projectile = event.getEntity();

        if (projectile instanceof Snowball) {
            ProjectileSource projectileSource = projectile.getShooter();

            if (projectileSource instanceof Player) {
                Player player = (Player) projectileSource;
                DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

                if (dodgeballPlayer != null) {
                    // Cancel the event if the player is not currently in (spectators cant shoot!)
                    if (dodgeballPlayer.getCurrentTeam() != null && !dodgeballPlayer.isCurrentlyIn())
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        if (!BiomeDodgeball.getInstance().getGameManager().isGameRunning()) return;
        Player player = event.getPlayer();

        DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

        if (dodgeballPlayer != null) {
            dodgeballPlayer.getCurrentTeam().removeMember(dodgeballPlayer);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

        if (dodgeballPlayer != null) {
            if (dodgeballPlayer.isPendingRestore()) {
                dodgeballPlayer.setPendingRestore(true);
            }
        }
    }

    @EventHandler
    public void hungerChangeEvent(FoodLevelChangeEvent event) {
        if (!BiomeDodgeball.getInstance().getGameManager().isGameRunning()) return;
        Player player = (Player) event.getEntity();

        DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

        if (dodgeballPlayer != null) {
            if (dodgeballPlayer.isCurrentlyIn() || dodgeballPlayer.getCurrentTeam() != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!BiomeDodgeball.getInstance().getGameManager().isGameRunning()) return;
        Player player = event.getPlayer();

        DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

        if (dodgeballPlayer != null) {
            if (dodgeballPlayer.isCurrentlyIn() || dodgeballPlayer.getCurrentTeam() != null) {
                event.setCancelled(true);
            }
        }
    }
}
