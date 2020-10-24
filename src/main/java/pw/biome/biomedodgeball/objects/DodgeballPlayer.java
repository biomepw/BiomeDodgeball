package pw.biome.biomedodgeball.objects;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private final BPlayerBoard playerBoard;
    private ItemStack[] contents;

    @Getter
    private Location preJoinLocation;

    @Getter
    private boolean pendingRestore;

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
        this.preJoinLocation = player.getLocation();
        this.contents = null;
        this.lives = 3;
        this.playerBoard = Netherboard.instance().createBoard(player, ChatColor.GOLD + "» Dodgeball");

        dodgeballPlayers.put(uuid, this);
    }

    public static DodgeballPlayer getFromUUID(UUID uuid) {
        return dodgeballPlayers.get(uuid);
    }


    public static ContextResolver<DodgeballPlayer, BukkitCommandExecutionContext> getContextResolver() {
        return (c) -> {
            Player player = c.getPlayer();
            DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

            if (dodgeballPlayer == null) {
                dodgeballPlayer = new DodgeballPlayer(player);
            }

            return dodgeballPlayer;
        };
    }

    public void setCurrentlyIn(boolean currentlyIn) {
        this.currentlyIn = currentlyIn;
        if (!currentlyIn) {
            BiomeDodgeball.getInstance().getGameManager().teleportOut(this); // this looks gross
        } else {
            prepareForGame();
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
     * Method only meant for use of leveling playing field
     * use decrementLives
     *
     * @param lives new lives
     */
    public void setLives(int lives) {
        this.lives = lives;
    }

    public void prepareForGame() {
        this.contents = playerObject.getInventory().getContents();
        playerObject.getInventory().setContents(currentTeam.getContents());
    }

    public void restoreInventory() {
        playerObject.getInventory().setContents(contents);
    }

    public void displayScoreboard() {
        playerBoard.set(ChatColor.RED + "» Lives", getLives());
        playerBoard.set(ChatColor.RED + "» Hits:", getHits());
    }

    public void removeScoreboard() {
        playerBoard.delete();
    }

    public void setPendingRestore(boolean isPendingRestore) {
        if (isPendingRestore) {
            if (playerObject.isOnline()) {
                restoreInventory();
                playerObject.teleportAsync(preJoinLocation);
            } else {
                this.pendingRestore = true;
            }
        }
    }
}
