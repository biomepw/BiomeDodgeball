package pw.biome.biomedodgeball.objects;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import pw.biome.biomedodgeball.BiomeDodgeball;

import java.util.ArrayList;
import java.util.List;

public class DodgeballTeam {

    @Getter
    private final List<DodgeballPlayer> teamMembers = new ArrayList<>();

    @Getter
    private final Location spawnLocation;

    @Getter
    private final String teamName;

    @Getter
    private final ChatColor teamColour;

    @Getter
    private final ItemStack[] contents;

    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();

    @Getter
    private final Scoreboard scoreboard = manager.getNewScoreboard();

    private Team scoreboardTeam;
    private Objective stats;

    private int scoreboardTaskId;

    public DodgeballTeam(String teamName, Location spawnLocation, ChatColor teamColour) {
        this.teamName = teamName;
        this.spawnLocation = spawnLocation;
        this.teamColour = teamColour;
        this.contents = loadInventory();

        loadScoreboard();

        BiomeDodgeball.getInstance().getGameManager().getDodgeballTeams().add(this);
    }

    public static DodgeballTeam getFromName(String search) {
        for (DodgeballTeam dodgeballTeam : BiomeDodgeball.getInstance().getGameManager().getDodgeballTeams()) {
            if (dodgeballTeam.getTeamName().equalsIgnoreCase(search)) return dodgeballTeam;
        }
        return null;
    }

    private static ItemStack[] loadInventory() {
        ItemStack snowballs = new ItemStack(Material.SNOWBALL, 10);

        return new ItemStack[]{snowballs};
    }


    public void loadScoreboard() {
        scoreboardTeam = scoreboard.registerNewTeam(teamName);
        stats = scoreboard.registerNewObjective("Stats", "dummy", teamColour + "Stats: ");

        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setDisplayName(teamColour + teamName);
        scoreboardTeam.setColor(teamColour);

        stats.setDisplaySlot(DisplaySlot.SIDEBAR);

        startScoreboardTask();
    }

    /**
     * Helper method to start a team-wide scoreboard updating task to be run every second
     */
    private void startScoreboardTask() {
        this.scoreboardTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(BiomeDodgeball.getInstance(), this::displayScoreboard, 20, 20);
    }

    /**
     * Helper method to display scoreboard to all members async
     * todo test async safety
     */
    public void displayScoreboard() {
        Bukkit.getScheduler().runTaskAsynchronously(BiomeDodgeball.getInstance(), () -> {
            teamMembers.forEach(dodgeballPlayer -> {
                if (!scoreboardTeam.getEntries().contains(dodgeballPlayer.getDisplayName())) {
                    scoreboardTeam.addEntry(dodgeballPlayer.getDisplayName());

                    Score lives = stats.getScore(ChatColor.RED + "Lives");
                    Score hits = stats.getScore(ChatColor.GREEN + "Hits");

                    lives.setScore(dodgeballPlayer.getLives());
                    hits.setScore(dodgeballPlayer.getHits());

                    dodgeballPlayer.getPlayerObject().setScoreboard(scoreboard);
                }
            });
        });
    }

    public void teleportMembersToSpawn() {
        teamMembers.forEach(dodgeballPlayer -> {
            dodgeballPlayer.getPlayerObject().teleportAsync(spawnLocation);
        });
    }

    public void addMember(DodgeballPlayer dodgeballPlayer) {
        Bukkit.broadcastMessage(dodgeballPlayer.getDisplayName() + " has joined " + teamColour + teamName);

        teamMembers.add(dodgeballPlayer);

        dodgeballPlayer.setCurrentTeam(this);
    }

    public void removeMember(DodgeballPlayer dodgeballPlayer) {
        Bukkit.broadcastMessage(dodgeballPlayer.getDisplayName() + " has left " + teamColour + teamName);

        teamMembers.remove(dodgeballPlayer);

        dodgeballPlayer.setCurrentTeam(null);
    }

    public String getColouredName() {
        return teamColour + teamName;
    }
}
