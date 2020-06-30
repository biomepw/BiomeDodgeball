package pw.biome.biomedodgeball.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import pw.biome.biomedodgeball.BiomeDodgeball;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;

public class DodgeballListener implements Listener {

    /**
     * Method to handle when a player is hit by a dodgeball
     *
     * @param event projectile hit event
     */
    @EventHandler
    public void dodgeballHit(ProjectileHitEvent event) {
        Entity entity = event.getHitEntity();
        Projectile projectile = event.getEntity();

        if (entity instanceof Player && projectile instanceof Snowball) {
            Player player = (Player) entity;
            Snowball dodgeball = (Snowball) projectile;

            DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());
            if (dodgeballPlayer != null && dodgeballPlayer.isCurrentlyIn()) {
                ProjectileSource projectileSource = dodgeball.getShooter();

                if (projectileSource instanceof Player) {
                    Player shooter = (Player) projectileSource;

                    DodgeballPlayer shooterDodgeballPlayer = DodgeballPlayer.getFromUUID(shooter.getUniqueId());

                    if (shooterDodgeballPlayer != null && shooterDodgeballPlayer.isCurrentlyIn()) {
                        // If they are same team, do nothing
                        if (shooterDodgeballPlayer.getCurrentTeam() == dodgeballPlayer.getCurrentTeam()) return;
                        // It was a success, run code for when player is hit by dodgeball
                        shooterDodgeballPlayer.incrementHits();

                        dodgeballPlayer.decrementLives();

                        // This will teleport them out
                        dodgeballPlayer.setCurrentlyIn(false);

                        BiomeDodgeball.getInstance().getGameManager().stopGame();
                    }
                }
            }
        }
    }


    /**
     * Method to handle when projectiles are launched
     *
     * @param event projectile launch event
     */
    @EventHandler
    public void dodgeballShoot(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Snowball) {
            ProjectileSource projectileSource = projectile.getShooter();

            if (projectileSource instanceof Player) {
                Player player = (Player) projectileSource;
                DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

                if (dodgeballPlayer != null) {
                    // Cancel the event if the player is not currently in (spectators cant shoot!)
                    if (!dodgeballPlayer.isCurrentlyIn()) event.setCancelled(true);
                }
            }
        }
    }
}
