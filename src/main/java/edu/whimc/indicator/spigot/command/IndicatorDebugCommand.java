/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.command.common.CommandError;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IndicatorDebugCommand extends CommandNode {

  public IndicatorDebugCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.ADMIN_PERMISSION,
        "Enable or disable debug mode",
        "debug");
    setCanBypassInvalid(true);
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Set<String> flags) {
    boolean enabled;
    if (!(sender instanceof Player)) {
      if (sender instanceof ConsoleCommandSender) {
        if (Indicator.getInstance()
            .getDebugManager()
            .isConsoleDebugging()) {
          Indicator.getInstance().getDebugManager().setConsoleDebugging(false);
          enabled = false;
        } else {
          Indicator.getInstance().getDebugManager().setConsoleDebugging(true);
          enabled = true;
        }
      } else {
        sendCommandError(sender, CommandError.NO_PLAYER);
        return false;
      }
    } else {
      Player player = (Player) sender;

      DebugManager debugManager = Indicator.getInstance().getDebugManager();
      if (debugManager.isDebugging(player.getUniqueId())) {
        debugManager.stopDebugging(player.getUniqueId());
        enabled = false;
      } else {
        debugManager.startDebugging(player.getUniqueId());
        enabled = true;
      }
    }

    if (enabled) {
      sender.sendMessage(Format.success("Debug mode " + ChatColor.BLUE + "enabled"));
    } else {
      sender.sendMessage(Format.success("Debug mode " + ChatColor.RED + "disabled"));
    }
    return true;
  }
}
