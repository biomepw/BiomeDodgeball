package pw.biome.biomedodgeball.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;
import pw.biome.biomedodgeball.objects.DodgeballTeam;
import pw.biome.biomedodgeball.objects.GameManager;

@CommandAlias("dodgeball|db")
@Description("Dodgeball commands")
public class DodgeballCommands extends BaseCommand {

    @Subcommand("join")
    @Description("Joins a dodgeball game")
    public void onDodgeballJoin(DodgeballPlayer dodgeballPlayer, GameManager gameManager, String team) {
        if (dodgeballPlayer.getCurrentTeam() == null && !gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
            if (team != null) {
                DodgeballTeam dodgeballTeam = DodgeballTeam.getFromName(team);

                if (dodgeballTeam != null) {
                    dodgeballTeam.addMember(dodgeballPlayer);
                    dodgeballPlayer.getPlayerObject().sendMessage(ChatColor.DARK_AQUA + "You have joined the team '" +
                            dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + "'!");
                }
            } else {
                gameManager.getQueuedPlayers().add(dodgeballPlayer);
                Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just joined the queue!");
            }
        }
        gameManager.processAutoRun();
    }

    @Subcommand("leave")
    @Description("Joins a dodgeball game")
    public void onDodgeballLeave(DodgeballPlayer dodgeballPlayer, GameManager gameManager) {
        if (gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
            gameManager.getQueuedPlayers().remove(dodgeballPlayer);
            Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just left the queue!");
        } else {
            dodgeballPlayer.getPlayerObject().sendMessage(ChatColor.RED + "You're not in the queue!");
        }
    }

    @Subcommand("create")
    @CommandPermission("dodgeball.admin")
    @Description("Creates a dodgeball game")
    public void onDodgeballCreate(Player player, GameManager gameManager, String teamName, String teamColourString) {
        if (!gameManager.isGameRunning()) {
            if (teamName != null && teamColourString != null) {
                ChatColor teamColour = ChatColor.valueOf(teamColourString);
                Location spawnLocation = player.getLocation();
                DodgeballTeam dodgeballTeam = new DodgeballTeam(teamName, spawnLocation, teamColour);
                player.sendMessage(ChatColor.DARK_AQUA + "Team '" + dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + " created");
            } else {
                player.sendMessage(ChatColor.RED + "/db create <team name> <team colour>");
            }
        }
    }

    @Subcommand("start")
    @CommandPermission("dodgeball.admin")
    @Description("Starts dodgeball game")
    public void onDodgeballStart(Player player, GameManager gameManager) {
        if (!gameManager.isGameRunning()) {
            gameManager.startGame();
            player.sendMessage(ChatColor.GREEN + "Started dodgeball game!");
        } else {
            player.sendMessage(ChatColor.RED + "Game already running!");
        }
    }

    @Subcommand("set")
    @CommandPermission("dodgeball.admin")
    @Description("Adds location to dodgeball team")
    public void onDodgeballSet(Player player, GameManager gameManager, String teamName) {
        if (teamName.equalsIgnoreCase("red")) {
            gameManager.setRedSpawnLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Red team spawn location stored.");
        } else if (teamName.equalsIgnoreCase("blue")) {
            gameManager.setBlueSpawnLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Blue team spawn location stored.");
        } else if (teamName.equalsIgnoreCase("lobby")) {
            gameManager.setLobbyLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Lobby location stored.");
        } else if (teamName.equalsIgnoreCase("spectator")) {
            gameManager.addSpectatorLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Spectator location stored.");
        }
    }

    @Subcommand("autostart")
    @CommandPermission("dodgeball.admin")
    @Description("Toggles autostart of future dodgeball games")
    public void onDodgeballAutostart(Player player, GameManager gameManager) {
        gameManager.setAutoRun(!gameManager.isAutoRun());
        player.sendMessage(ChatColor.DARK_AQUA + "Autorun now: " + gameManager.isAutoRun());
    }

    @Subcommand("stop")
    @CommandPermission("dodgeball.admin")
    @Description("Stops current dodgeball game")
    public void onDodgeballStop(Player player, GameManager gameManager) {
        if (gameManager.isGameRunning()) {
            gameManager.stopGame();
            player.sendMessage(ChatColor.GREEN + "Dodgeball game stopped!");
        } else {
            player.sendMessage(ChatColor.RED + "No game running!");
        }
    }
}