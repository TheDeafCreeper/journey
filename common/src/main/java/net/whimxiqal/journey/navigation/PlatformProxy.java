/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.navigation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.search.AnimationManager;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bstats.charts.CustomChart;

public interface PlatformProxy {

  boolean isNetherPortal(Cell cell);

  void playSuccess(UUID playerUuid);

  void spawnDestinationParticle(UUID playerUuid, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  void spawnModeParticle(UUID playerUuid, ModeType type, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  Collection<JourneyPlayer> onlinePlayers();

  Optional<JourneyPlayer> onlinePlayer(UUID uuid);

  Optional<JourneyPlayer> onlinePlayer(String name);

  Optional<Cell> entityCellLocation(UUID entityUuid);

  Optional<Vector> entityVector(UUID entityUuid);

  void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts);

  void prepareDestinationSearchSession(SearchSession searchSession, UUID player, FlagSet flags, Cell destination);

  boolean isAtSurface(Cell cell);

  boolean sendBlockData(UUID player, Cell location, AnimationManager.StageType stage, ModeType mode);

  boolean resetBlockData(UUID player, Collection<Cell> locations);

  String domainName(int domainId);

  boolean sendGui(JourneyPlayer source);

  boolean synchronous();

  Consumer<CustomChart> bStatsChartConsumer();

  Map<String, Map<String, Integer>> domainResourceKeys();
}
