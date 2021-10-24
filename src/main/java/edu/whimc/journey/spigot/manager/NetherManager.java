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

package edu.whimc.journey.spigot.manager;

import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.navigation.NetherPort;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.NetherUtil;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class NetherManager implements Listener {

  private final Map<LocationCell, LocationCell> portalConnections = new ConcurrentHashMap<>();

  public Collection<NetherPort> makeLeaps() {
    List<NetherPort> linksUnverified = portalConnections.entrySet().stream()
        .map(entry -> new NetherPort(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    List<NetherPort> linksVerified = new LinkedList<>();
    for (NetherPort leap : linksUnverified) {
      if (leap.verify()) {
        linksVerified.add(leap);
      } else {
        portalConnections.remove(leap.getOrigin(), leap.getDestination());
      }
    }
    return linksVerified;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPortalCreate(PortalCreateEvent e) {
    // TODO implement a way to create link here
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityPortal(EntityPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getEntity());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPortal(PlayerPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getPlayer());
  }

  private void onPortal(Location from, Location to, Entity entity) {
    if (to == null) return;

    LocationCell origin = new LocationCell(from);

    // Wait 1 second, and check for a portal around where the player is now
    Bukkit.getScheduler().runTaskLater(JourneySpigot.getInstance(), () -> {
      Location loc = entity.getLocation();

      // Origin portal
      Optional<NetherUtil.PortalGroup> originGroup = NetherUtil
          .locateAll(origin,
              8,
              origin.getY() - 8,
              origin.getY() + 8)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(from))));
      if (!originGroup.isPresent()) return;  // We can't find the origin portal

      // Destination Portal
      Optional<NetherUtil.PortalGroup> destinationGroup = NetherUtil
          .locateAll(new LocationCell(loc),
              16,
              loc.getBlockY() - 16,
              loc.getBlockY() + 16)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(to))));
      if (!destinationGroup.isPresent()) return;  // We can't find the destination portal

      List<LocationCell> linkedOrigins = new LinkedList<>();
      for (LocationCell originCell : originGroup.get().getBlocks()) {
        for (LocationCell destinationCell : destinationGroup.get().getBlocks()) {
          if (!portalConnections.containsKey(originCell)) {
            continue;
          }
          linkedOrigins.add(originCell);
          if (portalConnections.get(originCell).equals(destinationCell)) {
            return;  // We already have this portal link set up
          }
        }
      }

      // We don't have this portal link set up yet

      // Remove any connections with this origin (the portal link changed)
      linkedOrigins.forEach(portalConnections::remove);
      linkedOrigins.add(originGroup.get().port());

      // Add the portal
      LocationCell previous = portalConnections.put(originGroup.get().port(), destinationGroup.get().port());
      if (previous == null) {
        JourneySpigot.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug("Added nether link:"));
        JourneySpigot.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug(originGroup.get().port() + " -> "));
        JourneySpigot.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug(destinationGroup.get().port().toString()));
      }
    }, 20);
  }

}
