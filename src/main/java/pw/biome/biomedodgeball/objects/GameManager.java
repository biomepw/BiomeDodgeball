package pw.biome.biomedodgeball.objects;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pw.biome.biomechat.BiomeChat;
import pw.biome.biomechat.obj.PlayerCache;
import pw.biome.biomechat.obj.ScoreboardHook;
import pw.biome.biomedodgeball.BiomeDodgeball;
import pw.biome.biomedodgeball.listeners.DodgeballListener;
import pw.biome.biomedodgeball.utils.LocationUtil;
import pw.biome.biomedodgeball.utils.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager implements ScoreboardHook {

    @Getter
    private final List<DodgeballTeam> dodgeballTeams = new ArrayList<>();

    @Getter
    private final HashSet<DodgeballPlayer> queuedPlayers = new HashSet<>();
    private final ThreadLocalRandom threadLocalRandom;
    private final Timer gameTimer;

    @Getter
    private List<Location> spectateLocations;
    private Location redSpawnLocation;
    private Location blueSpawnLocation;
    private Location lobbyLocation;

    @Getter
    private boolean gameRunning;

    @Getter
    @Setter
    private boolean autoRun;

    private int scoreboardTaskId;

    public GameManager() {
        loadLocations();
        threadLocalRandom = ThreadLocalRandom.current();
        gameTimer = new Timer();
    }

    public void processAutoRun() {
        if (autoRun) {
            if (queuedPlayers.size() >= 4) {
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Dodgeball game starting in 10 seconds... " +
                        "Use /dodgeball join to join!");
                Bukkit.getScheduler().runTaskLater(BiomeDodgeball.getInstance(), this::startGame, 10 * 20);
            } else {
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Dodgeball game requires " + (4 - queuedPlayers.size()) +
                        " players to start... Use /dodgeball join to join!");
            }
        }
    }

    public void startGame() {
        // If there are no teams, but queued players, make new teams!
        if (dodgeballTeams.isEmpty() && queuedPlayers.size() >= 4) {
            DodgeballTeam red = new DodgeballTeam("Red", redSpawnLocation, org.bukkit.ChatColor.RED);
            DodgeballTeam blue = new DodgeballTeam("Blue", blueSpawnLocation, org.bukkit.ChatColor.BLUE);

            queuedPlayers.forEach(dodgeballPlayer -> {
                if (!dodgeballPlayer.getPlayerObject().isOnline()) return;
                if (red.getTeamMembers().size() <= blue.getTeamMembers().size()) {
                    red.addMember(dodgeballPlayer);
                } else {
                    blue.addMember(dodgeballPlayer);
                }
            });

            // Clear once processed
            queuedPlayers.clear();
        }

        // Make sure there's only 2 teams!
        if (dodgeballTeams.size() == 2) {
            this.gameRunning = true;
            gameTimer.start();

            DodgeballTeam team1 = dodgeballTeams.get(0);
            DodgeballTeam team2 = dodgeballTeams.get(1);

            int team1size = team1.getTeamMembers().size();
            int team2size = team2.getTeamMembers().size();

            // Give the larger team 2 lives, instead of 3
            if (team1size != team2size) {
                if (team1size > team2size) {
                    team1.getTeamMembers().forEach(dodgeballPlayer -> dodgeballPlayer.setLives(2));
                } else {
                    team2.getTeamMembers().forEach(dodgeballPlayer -> dodgeballPlayer.setLives(2));
                }
            }

            dodgeballTeams.forEach(dodgeballTeam -> {
                dodgeballTeam.teleportMembersToSpawn();
                dodgeballTeam.getTeamMembers().forEach(dodgeballPlayer -> {
                    dodgeballPlayer.setCurrentlyIn(true);
                    dodgeballPlayer.displayScoreboard();
                });
            });

            Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Dodgeball game is starting! "
                    + team1.getColouredName() + ChatColor.DARK_AQUA + " vs " + team2.getColouredName());

            // Stop scoreboard tasks
            BiomeChat biomeChat = BiomeChat.getPlugin();
            biomeChat.stopScoreboardTask();
            biomeChat.getScoreboardHookList().forEach(ScoreboardHook::stopScoreboardTask);
            biomeChat.registerHook(this);

            // Start our own
            scoreboardTaskId = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously
                    (BiomeDodgeball.getInstance(), this::restartScoreboardTask, 20, 20).getTaskId();
        }
    }

    public void stopGame() {
        if (gameRunning) {
            this.gameRunning = false;
            gameTimer.stop();

            int timeOfGame = gameTimer.getTimeSeconds();

            DodgeballTeam winningTeam = null;
            for (DodgeballTeam dodgeballTeam : dodgeballTeams) {
                if (winningTeam != null) {
                    if (winningTeam.getCurrentlyIn() < dodgeballTeam.getCurrentlyIn()) {
                        winningTeam = dodgeballTeam;
                    }
                } else {
                    winningTeam = dodgeballTeam;
                }
            }

            if (winningTeam != null) {
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Dodgeball game is over, with " +
                        winningTeam.getColouredName() + ChatColor.DARK_AQUA + " winning the game in " +
                        timeOfGame + " seconds");

                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "The MVP of the game was: "
                        + winningTeam.getTeamColour() + winningTeam.getMVP().getDisplayName());

                // Restore original inventory
                dodgeballTeams.forEach(dodgeballTeam -> dodgeballTeam.getTeamMembers().forEach(dodgeballPlayer -> {
                    dodgeballPlayer.restoreInventory();
                    dodgeballPlayer.removeScoreboard();
                    dodgeballPlayer.getPlayerObject().teleportAsync(lobbyLocation);
                }));

                // Clear resources
                dodgeballTeams.forEach(dodgeballTeam -> dodgeballTeam.getTeamMembers().clear());
                dodgeballTeams.clear();
                DodgeballPlayer.getDodgeballPlayers().clear();

                // Restart scoreboard update task
                BiomeChat biomeChat = BiomeChat.getPlugin();
                biomeChat.unregisterHook(this);

                // Try restart hooks
                biomeChat.getScoreboardHookList().forEach(ScoreboardHook::restartScoreboardTask);

                // Remove old snowballs
                DodgeballListener.getDroppedItemsEntities().forEach(Entity::remove);

                // Clear resources
                DodgeballListener.getDroppedItemsEntities().clear();
            }
        }
    }

    /**
     * Method to check the game to ensure there are still players playing!
     */
    public void checkGameStatus() {
        if (gameRunning) {
            for (DodgeballTeam dodgeballTeam : dodgeballTeams) {
                if (dodgeballTeam.getCurrentlyIn() == 0) {
                    stopGame();
                    break;
                }
            }
        }
    }

    /**
     * Helper method to teleport player out to a random location inside the 'arena'
     *
     * @param dodgeballPlayer to teleport out
     */
    public void teleportOut(DodgeballPlayer dodgeballPlayer) {
        int randomInt = threadLocalRandom.nextInt(spectateLocations.size());
        dodgeballPlayer.getPlayerObject().teleport(spectateLocations.get(randomInt));
    }

    private void loadLocations() {
        FileConfiguration config = BiomeDodgeball.getInstance().getConfig();
        this.redSpawnLocation = LocationUtil.toLocation(config.getString("red.spawn_location"));
        this.blueSpawnLocation = LocationUtil.toLocation(config.getString("blue.spawn_location"));
        this.lobbyLocation = LocationUtil.toLocation(config.getString("lobby.spawn_location"));

        List<Location> locations = new ArrayList<>();

        List<String> locationStrings = config.getStringList("spectator.locations");
        for (String locationString : locationStrings) {
            Location location = LocationUtil.toLocation(locationString);
            locations.add(location);
        }

        this.spectateLocations = locations;
    }

    public void setRedSpawnLocation(Location location) {
        this.redSpawnLocation = location;
        String serialised = LocationUtil.fromLocation(location);
        BiomeDodgeball.getInstance().getConfig().set("red.spawn_location", serialised);
        BiomeDodgeball.getInstance().saveConfig();
    }

    public void setBlueSpawnLocation(Location location) {
        this.blueSpawnLocation = location;
        String serialised = LocationUtil.fromLocation(location);
        BiomeDodgeball.getInstance().getConfig().set("blue.spawn_location", serialised);
        BiomeDodgeball.getInstance().saveConfig();
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        String serialised = LocationUtil.fromLocation(location);
        BiomeDodgeball.getInstance().getConfig().set("lobby.spawn_location", serialised);
        BiomeDodgeball.getInstance().saveConfig();
    }

    public void addSpectatorLocation(Location location) {
        spectateLocations.add(location);

        List<String> serialisedLocationList = new ArrayList<>();
        spectateLocations.forEach(spectateLocation -> serialisedLocationList.add(LocationUtil.fromLocation(spectateLocation)));

        BiomeDodgeball.getInstance().getConfig().set("spectator.locations", serialisedLocationList);
    }

    /**
     * Helper method to show the stats of players in the scoreboard
     */
    @Override
    public void restartScoreboardTask() {
        ImmutableList<Player> playerList = ImmutableList.copyOf(Bukkit.getServer().getOnlinePlayers());
        for (Player player : playerList) {
            PlayerCache playerCache = PlayerCache.getFromUUID(player.getUniqueId());

            if (playerCache == null) return;
            DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

            player.setPlayerListHeader(ChatColor.BLUE + "Biome");

            if (dodgeballPlayer != null && dodgeballPlayer.getCurrentTeam() != null) {
                int lives = dodgeballPlayer.getLives();
                player.setPlayerListName(dodgeballPlayer.getCurrentTeam().getTeamColour() + player.getDisplayName() + ChatColor.GOLD + " | Lives:" + lives);
            } else {
                player.setPlayerListName(playerCache.getRank().getPrefix() + player.getDisplayName());
            }
        }
    }

    @Override
    public void stopScoreboardTask() {
        if (scoreboardTaskId != 0) {
            Bukkit.getScheduler().cancelTask(scoreboardTaskId);
            scoreboardTaskId = 0;
        }
    }
}
