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

package edu.whimc.indicator.spigot.navigation.mode;

import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.spigot.IndicatorSpigot;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.util.SpigotUtil;
import java.util.Collections;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public abstract class SpigotMode extends Mode<LocationCell, World> {

  private final Set<Material> forcePassable;

  public SpigotMode(Set<Material> forcePassable) {
    this.forcePassable = forcePassable;
  }

  protected Set<Material> getForcedPassable() {
    return this.forcePassable;
  }

  protected boolean isVerticallyPassable(Block block) {
    return SpigotUtil.isVerticallyPassable(block, forcePassable);
  }

  protected boolean isLaterallyPassable(Block block) {
    return SpigotUtil.isLaterallyPassable(block, forcePassable);
  }

  protected boolean isPassable(Block block) {
    return SpigotUtil.isPassable(block, forcePassable);
  }

  protected boolean canStandOn(Block block) {
    return SpigotUtil.canStandOn(block, forcePassable);
  }

  protected boolean canStandIn(Block block) {
    return SpigotUtil.canStandIn(block, forcePassable);
  }

}
