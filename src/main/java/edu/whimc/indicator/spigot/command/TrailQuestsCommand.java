package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.common.tools.BufferedFunction;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.spigot.command.common.*;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrailQuestsCommand extends FunctionlessCommandNode {

  public TrailQuestsCommand(@Nullable CommandNode parent, @NotNull Quests quests) {
    super(parent, Permissions.TRAIL_USE_PERMISSION,
        "Blaze trails to destinations for quests using the Quests plugin",
        "quests");
    addChildren(new TrailNextQuestCommand(this, quests));
  }

  public static class TrailNextQuestCommand extends PlayerCommandNode {

    private final Quests quests;

    public TrailNextQuestCommand(@Nullable CommandNode parent, @NotNull Quests quests) {
      super(parent, Permissions.TRAIL_USE_PERMISSION,
          "Blaze trails to your quest objectives",
          "next");
      this.quests = quests;
      BufferedFunction<Player, List<String>> questFunction = new BufferedFunction<>(player -> {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Quests");
        if (!(plugin instanceof Quests)) {
          return new LinkedList<>();
        }
        return quests.getQuester(player.getUniqueId())
            .getCurrentQuests()
            .keySet()
            .stream()
            .map(quest -> Extra.quoteStringWithSpaces(quest.getName()))
            .collect(Collectors.toList());
      }, 1000);
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<quest>")
              .allowedEntries((src, list) -> {
                if (src instanceof Player) {
                  return questFunction.apply((Player) src);
                } else {
                  return new LinkedList<>();
                }
              })
              .strict(false)
              .build())
          .build(), "Blaze trails to your next destination for a quest");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      Quest quest = quests.getQuest(args[0]);
      if (quest == null) {
        player.spigot().sendMessage(Format.error("That quest doesn't exist"));
        return false;
      }

      if (!quests.getQuester(player.getUniqueId()).getCurrentQuests().containsKey(quest)) {
        player.spigot().sendMessage(Format.error("You are not doing that quest"));
        return false;
      }

      LinkedList<Location> locationsToReach = quests.getQuester(player.getUniqueId()).getCurrentStage(quest).getLocationsToReach();
      if (locationsToReach.isEmpty()) {
        player.spigot().sendMessage(Format.error("That quest has no destination"));
        return false;
      }

      LocationCell endLocation = new LocationCell(locationsToReach.getFirst());

      TrailCommand.blazeTrailTo(player, endLocation, flags);
      return true;
    }
  }
}
