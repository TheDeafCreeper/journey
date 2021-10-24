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

package edu.whimc.journey.spigot.search;

import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.common.navigation.Leap;
import edu.whimc.journey.common.search.ReverseSearchSession;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.navigation.PortalLeap;
import edu.whimc.journey.spigot.navigation.mode.ClimbMode;
import edu.whimc.journey.spigot.navigation.mode.DoorMode;
import edu.whimc.journey.spigot.navigation.mode.FlyMode;
import edu.whimc.journey.spigot.navigation.mode.JumpMode;
import edu.whimc.journey.spigot.navigation.mode.SwimMode;
import edu.whimc.journey.spigot.navigation.mode.WalkMode;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class PlayerSearchSession extends ReverseSearchSession<LocationCell, World> {

  @Getter
  private final SessionState sessionInfo;
  @Getter
  private final AnimationManager animationManager;

  public PlayerSearchSession(Player player, Set<SearchFlag> flags, int algorithmStepDelay) {
    super(player.getUniqueId(), Caller.PLAYER);
    this.sessionInfo = new SessionState();
    this.animationManager = new AnimationManager(this);
    animationManager.setAnimating(flags.contains(SearchFlag.ANIMATE));
    setAlgorithmStepDelay(algorithmStepDelay);
    // Modes - in order of preference
    Set<Material> passableBlocks = collectPassableBlocks(flags);
    if (player.getAllowFlight() && !flags.contains(SearchFlag.NOFLY)) {
      registerMode(new FlyMode(passableBlocks));
    } else {
      registerMode(new WalkMode(passableBlocks));
      registerMode(new JumpMode(passableBlocks));
      registerMode(new SwimMode(passableBlocks));
    }
    registerMode(new DoorMode(passableBlocks));
    registerMode(new ClimbMode(passableBlocks));

    // Links
    registerLeaps(player::hasPermission, player);
  }

  private void registerLeaps(Predicate<String> permissionSupplier) {
    this.registerLeaps(permissionSupplier, null);
  }

  private void registerLeaps(Predicate<String> permissionSupplier, @Nullable Player player) {
    // Links - Nether
    JourneySpigot.getInstance().getNetherManager().makeLeaps().forEach(this::registerLeap);

    // Links - Portals plugin
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream()
          .filter(portal -> portal.getDestination() != null)
          .filter(portal -> portal.getWorld() != null)
          .filter(portal -> portal.getDestination().getLocation().getWorld() != null)
          .filter(portal -> Optional.ofNullable(portal.getPermission()).map(perm ->
              permissionSupplier.test(perm.getName())).orElse(true))
          .map(portal -> {
            try {
              return PortalLeap.from(portal);
            } catch (Exception e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(link -> {
            if (player == null) {
              registerLeap(link);
            } else {
              registerLinkVerbose(player, link);
            }
          });
    }
  }

  private void registerLinkVerbose(Player player, Leap<LocationCell, World> link) {
    if (JourneySpigot.getInstance().getDebugManager().isDebugging(player.getUniqueId())) {
      player.spigot().sendMessage(Format.debug("Registering Link: " + link.toString()));
    }
    super.registerLeap(link);
  }

  private Set<Material> collectPassableBlocks(Set<SearchFlag> flags) {
    Set<Material> passableBlocks = new HashSet<>();
    if (flags.contains(SearchFlag.NODOOR)) {
      passableBlocks.add(Material.ACACIA_DOOR);
      passableBlocks.add(Material.ACACIA_TRAPDOOR);
      passableBlocks.add(Material.BIRCH_DOOR);
      passableBlocks.add(Material.BIRCH_TRAPDOOR);
      passableBlocks.add(Material.CRIMSON_DOOR);
      passableBlocks.add(Material.CRIMSON_TRAPDOOR);
      passableBlocks.add(Material.DARK_OAK_DOOR);
      passableBlocks.add(Material.DARK_OAK_TRAPDOOR);
      passableBlocks.add(Material.IRON_DOOR);
      passableBlocks.add(Material.JUNGLE_DOOR);
      passableBlocks.add(Material.JUNGLE_TRAPDOOR);
      passableBlocks.add(Material.OAK_DOOR);
      passableBlocks.add(Material.OAK_TRAPDOOR);
      passableBlocks.add(Material.SPRUCE_DOOR);
      passableBlocks.add(Material.SPRUCE_TRAPDOOR);
      passableBlocks.add(Material.WARPED_DOOR);
      passableBlocks.add(Material.WARPED_TRAPDOOR);
    }
    return passableBlocks;
  }

}
