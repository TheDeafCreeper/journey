package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.Endpoint;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.api.path.Path;
import edu.whimc.indicator.api.path.Step;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.search.mode.ModeTypes;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TrailCommand extends CommandNode {

  private static final int ILLUMINATED_COUNT = 16;
  private static final int TICKS_PER_PARTICLE = 3;
  private static final double CHANCE_OF_PARTICLE = 0.4;

  public TrailCommand() {
    super(null,
        Permissions.TRAIL_PERMISSION,
        "Provide a trail to your destination",
        "trail");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may perform this command"));
      return false;
    }
    Player player = (Player) sender;
    Endpoint<JavaPlugin, LocationCell, World> destination;
    UUID playerUuid = player.getUniqueId();
    if (Indicator.getInstance()
        .getEndpointManager()
        .containsKey(playerUuid)) {
      destination = Indicator.getInstance().getEndpointManager().get(playerUuid);
    } else {
      sender.sendMessage(Format.warn("You don't have a destination set!"));
      sender.sendMessage(Format.warn("Defaulting to your world's spawn."));

      LocationCell spawnLocation = new LocationCell(player.getWorld().getSpawnLocation());
      destination = new Endpoint<>(Indicator.getInstance(),
          spawnLocation,
          loc -> loc.distanceToSquared(spawnLocation) < 9);  // Finish within 3 blocks

      Indicator.getInstance().getEndpointManager().put(playerUuid, destination);
    }

    Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
      IndicatorSearch search = new IndicatorSearch(player);

      // Set up a search cancellation in case it takes too long
      Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () -> {
        search.setCancelled(true);
        Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Search cancelled due to timeout."));
      }, 200 /* 10 seconds */);

      // Set up a "Working..." message if it takes too long
      BukkitTask workingNotification = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () ->
          sender.sendMessage(Format.info("Finding a path to your destination...")), 10);

      Path<LocationCell, World> path = search.findPath(
          new LocationCell(player.getLocation()),
          destination.getLocation());

      // Cancel "Working..." message if it hasn't happened yet
      workingNotification.cancel();

      if (path == null) {
        sender.sendMessage(Format.error("A path to your destination could not be found."));
        return;
      }

      // Set up illumination scheduled task for showing the trails
      Random rand = new Random();
      int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
        Optional<PlayerJourney> journeyOptional = Indicator.getInstance()
            .getJourneyManager()
            .getPlayerJourney(playerUuid);
        if (!journeyOptional.isPresent()) return;
        PlayerJourney journey = journeyOptional.get();

        List<Step<LocationCell, World>> steps = journey.next(ILLUMINATED_COUNT);  // Show 16 steps ahead
        for (int i = 0; i < steps.size() - 1; i++) {
          if (rand.nextDouble() < CHANCE_OF_PARTICLE) {  // make shimmering effect
            Particle particle;
            ModeType modeType = steps.get(i + 1).getModeType();
            if (modeType.equals(ModeTypes.WALK)) {
              particle = Particle.FLAME;
            } else if (modeType.equals(ModeTypes.JUMP)) {
              particle = Particle.HEART;
            } else {
              particle = Particle.CLOUD;
            }
            steps.get(i).getLocatable().getDomain().spawnParticle(particle,
                steps.get(i).getLocatable().getX() + rand.nextDouble(),
                steps.get(i).getLocatable().getY() + 0.2f,
                steps.get(i).getLocatable().getZ() + rand.nextDouble(),
                1,
                0, 0, 0,
                0);
          }
        }
      }, 0, TICKS_PER_PARTICLE).getTaskId();

      // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
      PlayerJourney journey = new PlayerJourney(playerUuid, path, cell -> {
        boolean completed = cell.distanceToSquared(destination.getLocation()) < 9;
        if (completed) {
          Bukkit.getScheduler().cancelTask(illuminationTaskId);
        }
        return completed;
      });
      journey.setIlluminationTaskId(illuminationTaskId);

      // Save the journey and stop the illumination from the other one
      Indicator.getInstance().getJourneyManager()
          .putPlayerJourney(playerUuid, journey)
          .ifPresent(previousJourney ->
              Bukkit.getScheduler().cancelTask(previousJourney.getIlluminationTaskId()));

      sender.sendMessage(Format.success("Showing a trail to your destination"));

    });

    return true;

  }
}
