/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pietelite.journey.spigot.command.delete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PersonalWaypointManager;
import me.pietelite.journey.common.tools.BufferedFunction;
import me.pietelite.journey.spigot.command.JourneyCommand;
import me.pietelite.journey.spigot.command.common.CommandError;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.Parameter;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to delete a personal search endpoint.
 */
public class JourneyDeletePrivateCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyDeletePrivateCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.JOURNEY_PATH_PRIVATE,
        "Delete a saved personal location",
        "private");

    BufferedFunction<Player, List<String>> customLocationsFunction
        = JourneyCommand.bufferedPersonalEndpointFunction();
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .allowedEntries((src, prev) -> {
              if (src instanceof Player) {
                return customLocationsFunction.apply((Player) src);
              } else {
                return new ArrayList<>();
              }
            }).build())
        .build(), "Use a previously saved custom location");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    if (args.length < 1) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    PersonalWaypointManager endpointManager =
        Journey.get().proxy().dataManager()
            .getPersonalEndpointManager();
    if (endpointManager.hasPersonalEndpoint(player.getUniqueId(), args[0])) {
      Journey.get().proxy().dataManager()
          .getPersonalEndpointManager()
          .removePersonalEndpoint(player.getUniqueId(), args[0]);
      player.spigot().sendMessage(Format.success("The custom location ",
          Format.toPlain(Format.note(args[0])), " has been removed."));
      return true;
    } else {
      player.spigot().sendMessage(Format.error("The custom location ",
          Format.toPlain(Format.note(args[0])), " could not be found."));
      return false;
    }
  }
}
