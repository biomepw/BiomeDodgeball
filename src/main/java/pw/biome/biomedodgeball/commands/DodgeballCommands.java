package pw.biome.biomedodgeball.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.biome.biomedodgeball.BiomeDodgeball;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;
import pw.biome.biomedodgeball.objects.DodgeballTeam;
import pw.biome.biomedodgeball.objects.GameManager;

public class DodgeballCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        GameManager gameManager = BiomeDodgeball.getInstance().getGameManager();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

            if (dodgeballPlayer == null) {
                dodgeballPlayer = new DodgeballPlayer(player);
            }

            if (args.length >= 1) {
                if (!gameManager.isGameRunning()) {
                    if (player.hasPermission("dodgeball.admin")) {
                        if (args[0].equalsIgnoreCase("create")) {
                            if (args.length == 3) {
                                String teamName = args[1];
                                ChatColor teamColour = ChatColor.valueOf(args[2]);
                                Location spawnLocation = player.getLocation();

                                DodgeballTeam dodgeballTeam = new DodgeballTeam(teamName, spawnLocation, teamColour);

                                player.sendMessage(ChatColor.DARK_AQUA + "Team '" + dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + " created");
                            }
                        } else if (args[0].equalsIgnoreCase("start")) {
                            gameManager.startGame();
                            player.sendMessage(ChatColor.DARK_AQUA + "Starting game!");
                        } else if (args[0].equalsIgnoreCase("stop")) {
                            gameManager.stopGame();
                            player.sendMessage(ChatColor.DARK_AQUA + "Stopping game!");
                        } else if (args[0].equalsIgnoreCase("set")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("red")) {
                                    gameManager.setRedSpawnLocation(player.getLocation());
                                    player.sendMessage(ChatColor.DARK_AQUA + "Red team spawn location stored.");
                                } else if (args[1].equalsIgnoreCase("blue")) {
                                    gameManager.setBlueSpawnLocation(player.getLocation());
                                    player.sendMessage(ChatColor.DARK_AQUA + "Blue team spawn location stored.");
                                } else if (args[1].equalsIgnoreCase("lobby")) {
                                    gameManager.setLobbyLocation(player.getLocation());
                                    player.sendMessage(ChatColor.DARK_AQUA + "Lobby location stored.");
                                } else if (args[1].equalsIgnoreCase("spectator")) {
                                    gameManager.addSpectatorLocation(player.getLocation());
                                    player.sendMessage(ChatColor.DARK_AQUA + "Spectator location stored.");
                                }
                            }
                        }
                    }

                    if (args[0].equalsIgnoreCase("join")) {
                        if (dodgeballPlayer.getCurrentTeam() == null &&
                                !gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
                            if (args.length == 2) {
                                DodgeballTeam dodgeballTeam = DodgeballTeam.getFromName(args[1]);

                                if (dodgeballTeam != null) {
                                    dodgeballTeam.addMember(dodgeballPlayer);
                                    player.sendMessage(ChatColor.DARK_AQUA + "You have joined the team '" +
                                            dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + "'!");
                                }
                            } else {
                                gameManager.getQueuedPlayers().add(dodgeballPlayer);
                                Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just joined the queue!");
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("leave") && gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
                        gameManager.getQueuedPlayers().remove(dodgeballPlayer);
                        Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just left the queue!");
                    }
                }
            }
        }
        return true;
    }
}
