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

package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.SpigotUtil;
import org.bukkit.World;

public class JumpMode extends Mode<LocationCell, World> {
  @Override
  public void collectDestinations(LocationCell origin) {
    LocationCell cell;

    cell = origin.createLocatableAtOffset(0, -1, 0);
    if (SpigotUtil.isVerticallyPassable(cell.getBlock())) {
      // Nothing to jump off of
      reject(cell);
      return;
    }

    cell = origin.createLocatableAtOffset(0, 2, 0);
    if (!SpigotUtil.isVerticallyPassable(cell.getBlock())) {
      // No room to jump
      reject(cell);
      return;
    }
    // 1 block up
    accept(origin.createLocatableAtOffset(0, 1, 0), 1.0d);

    // 1 block away and up
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
            if (offXIn == 0 && offZIn == 0) continue;
            // Check two blocks tall
            cell = origin.createLocatableAtOffset(
                offXIn * offX /* get sign back */,
                1,
                offZIn * offZ /* get sign back */);
            if (!SpigotUtil.isLaterallyPassable(cell.getBlock())) {
              reject(cell);
              continue outerZ;
            }
            cell = origin.createLocatableAtOffset(
                offXIn * offX /* get sign back */,
                2,
                offZIn * offZ /* get sign back */);
            if (!SpigotUtil.isPassable(cell.getBlock())) {
              reject(cell);
              continue outerZ;
            }
          }
        }
        double jumpDistance = (origin.getBlockAtOffset(offX, 1, offZ).getBoundingBox().getMaxY()
            + 1.0
            - (origin.getBlockAtOffset(0, 0, 0).isPassable()
            ? origin.getBlockAtOffset(0, -1, 0).getBoundingBox().getMaxY() - 1
            : origin.getBlockAtOffset(0, 0, 0).getBoundingBox().getMaxY()));
        LocationCell other = origin.createLocatableAtOffset(offX, 1, offZ);
        if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(offX, 0, offZ))
            && jumpDistance <= 1.2) {
          // Can stand here
          accept(other, origin.distanceTo(other));
//          break; I don't think this goes here?
        } else {
          reject(other);
        }
      }
    }
  }

  @Override
  public ModeType getType() {
    return ModeTypes.JUMP;
  }
}
