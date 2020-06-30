package pw.biome.biomedodgeball.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import pw.biome.biomedodgeball.BiomeDodgeball;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DodgeballPlayer {

    @Getter
    private static final ConcurrentHashMap<UUID, DodgeballPlayer> dodgeballPlayers = new ConcurrentHashMap<>();

    @Getter
    private final UUID uuid;

    @Getter
    private final String displayName;

    @Getter
    private final Player playerObject;

    private final ItemStack[] contents;

    @Getter
    @Setter
    private DodgeballTeam currentTeam;

    @Getter
    private boolean currentlyIn;

    @Getter
    private int hits;

    @Getter
    private int lives;

    public DodgeballPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.displayName = player.getDisplayName();
        this.playerObject = player;
        this.contents = player.getInventory().getContents();
        this.lives = 3;

        dodgeballPlayers.put(uuid, this);
    }

    public void setCurrentlyIn(boolean currentlyIn) {
        this.currentlyIn = currentlyIn;
        if (!currentlyIn) {
            BiomeDodgeball.getInstance().getGameManager().teleportOut(this); // this looks gross
        } else {
            giveDodgeballInventory();
        }
    }

    public void incrementHits() {
        hits += 1;

        displayScoreboard();
    }

    public void decrementLives() {
        lives -= 1;

        if (lives == 0) {
            setCurrentlyIn(false);
        }

        displayScoreboard();
    }

    /**
     * Do not use this method
     * It is meant for use of leveling playing field only
     *
     * @param lives new lives
     */
    @Deprecated
    public void setLives(int lives) {
        this.lives = lives;
    }

    public void giveDodgeballInventory() {
        playerObject.getInventory().setContents(currentTeam.getContents());
    }

    public void restoreInventory() {
        playerObject.getInventory().setContents(contents);
    }

    public void displayScoreboard() {
        Score lives = getCurrentTeam().getStats().getScore(ChatColor.RED + "Lives");
        Score hits = getCurrentTeam().getStats().getScore(ChatColor.GREEN + "Hits");

        lives.setScore(getLives());
        hits.setScore(getHits());

        playerObject.setScoreboard(getCurrentTeam().getScoreboard());
    }

    public static DodgeballPlayer getFromUUID(UUID uuid) {
        return dodgeballPlayers.get(uuid);
    }
}
