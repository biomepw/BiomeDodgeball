package pw.biome.biomedodgeball.objects;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {

    @Getter
    private final List<DodgeballTeam> dodgeballTeams = new ArrayList<>();

    @Getter
    private final List<DodgeballPlayer> queuedPlayers = new ArrayList<>();

    @Getter
    private final Location[] spectateLocations;

    // todo something about this
    private final Location spawnLocation1 = new Location(Bukkit.getWorld("world"), -6, 4, -1059);
    private final Location spawnLocation2 = new Location(Bukkit.getWorld("world"), 9, 4, -1061);
    private final Location lobbyLocation = new Location(Bukkit.getWorld("world"), 1, 16, -1063);

    private final ThreadLocalRandom threadLocalRandom;

    private final Timer gameTimer;

    @Getter
    private boolean gameRunning;

    public GameManager() {
        spectateLocations = new Location[]{spawnLocation1.clone().add(0, 10, 0), spawnLocation2.clone().add(0, 10, 0)}; // todo something about this
        threadLocalRandom = ThreadLocalRandom.current();
        gameTimer = new Timer();
    }

    public void startGame() {
        // If there are no teams, but queued players, make new teams!
        if (dodgeballTeams.isEmpty() && queuedPlayers.size() >= 2) { // todo change to 6
            DodgeballTeam red = new DodgeballTeam("Red", spawnLocation1, org.bukkit.ChatColor.RED);
            DodgeballTeam blue = new DodgeballTeam("Blue", spawnLocation2, org.bukkit.ChatColor.BLUE);

            queuedPlayers.forEach(dodgeballPlayer -> {
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
                dodgeballTeam.getTeamMembers().forEach(dodgeballPlayer -> dodgeballPlayer.setCurrentlyIn(true));
            });

            Bukkit.broadcastMessage(ChatColor.AQUA + "Dodgeball game is starting! "
                    + team1.getColouredName() + ChatColor.AQUA + " vs " + team2.getColouredName());
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
                Bukkit.broadcastMessage(ChatColor.AQUA + "Dodgeball game is over, with " +
                        winningTeam.getColouredName() + ChatColor.AQUA + " winning the game in " +
                        timeOfGame + " seconds");

                // Restore original inventory
                dodgeballTeams.forEach(dodgeballTeam -> dodgeballTeam.getTeamMembers().forEach(dodgeballPlayer -> {
                    dodgeballPlayer.restoreInventory();
                    dodgeballPlayer.getPlayerObject().teleportAsync(lobbyLocation);
                }));

                // Clear resources
                dodgeballTeams.clear();
                DodgeballPlayer.getDodgeballPlayers().clear();
            }
        }
    }

    /**
     * Helper method to teleport player out to a random location inside the 'arena'
     *
     * @param dodgeballPlayer to teleport out
     */
    public void teleportOut(DodgeballPlayer dodgeballPlayer) {
        int randomInt = threadLocalRandom.nextInt(spectateLocations.length);
        dodgeballPlayer.getPlayerObject().teleport(spectateLocations[randomInt]);
    }
}
