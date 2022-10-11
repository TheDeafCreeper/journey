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

package me.pietelite.journey.common.search;

import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.search.flag.FlagSet;
import me.pietelite.journey.common.search.flag.Flags;
import net.kyori.adventure.audience.Audience;

public class PlayerSurfaceGoalSearchSession extends LocalUpwardsGoalSearchSession {

  private final PlayerSessionState sessionState;

  public PlayerSurfaceGoalSearchSession(UUID player, Cell origin, FlagSet flags) {
    super(player, SearchSession.Caller.PLAYER, origin, flags);
    sessionState = new PlayerSessionState(player);
    if (flags.hasFlag(Flags.ANIMATE)) {
      sessionState.animationManager().setAnimating(true);
      setAlgorithmStepDelay(flags.valueOf(Flags.ANIMATE));
    } else {
      sessionState.animationManager().setAnimating(false);
    }

    // We don't need any ports for this!
    Journey.get().proxy().platform().prepareSearchSession(this, player, flags, false);
  }

  public PlayerSessionState getSessionState() {
    return sessionState;
  }

  @Override
  public boolean reachesGoal(Cell cell) {
    return Journey.get().proxy().platform().isAtSurface(cell);
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(getCallerId());
  }
}
