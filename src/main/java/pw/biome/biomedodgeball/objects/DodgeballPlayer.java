package pw.biome.biomedodgeball.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
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

    @Getter
    private final PlayerInventory playerInventory;

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
        this.playerInventory = player.getInventory();
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
    }

    public void decrementLives() {
        lives -= 1;

        if (lives == -1) {
            setCurrentlyIn(false);
        }
    }

    /**
     * Do not use this method
     * It is meant for use of leveling playing field only
     * @param lives new lives
     */
    @Deprecated
    public void setLives(int lives) {
        this.lives = lives;
    }

    public void giveDodgeballInventory() {
        playerObject.getInventory().setContents(currentTeam.getContents());
    }

    // todo test this
    public void restoreInventory() {
        playerObject.getInventory().setContents(playerInventory.getContents());
    }

    public static DodgeballPlayer getFromUUID(UUID uuid) {
        return dodgeballPlayers.get(uuid);
    }
}
