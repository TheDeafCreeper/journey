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
import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.event.ModeFailureEvent;
import net.whimxiqal.journey.search.event.ModeSuccessEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A general mode of transportation which determines whether certain locations can be reached by
 * a regular humanoid entity.
 */
public abstract class Mode {

  private final SearchSession session;

  /**
   * General constructor.
   *
   * @param session the search session requesting information from this mode
   */
  public Mode(@NotNull SearchSession session) {
    this.session = session;
  }

  /**
   * Collect and return all the destinations that are reachable from an original location
   * based on the implementation of this mode.
   * The returned value stores all possible movement options.
   *
   * @param origin the original (current) location
   * @return all options
   */
  @NotNull
  public final Collection<Option> getDestinations(@NotNull Cell origin) {
    List<Option> options = new LinkedList<>();
    collectDestinations(origin, options);
    return options;
  }

  /**
   * Accept a location and its distance to the list of possible options.
   * This adds it to the list and performs other somewhat unnecessary management operations.
   * All implementations of {@link Mode} should use accept instead of adding directly to the option list.
   *
   * @param destination the accepted destination
   * @param distance    the distance to the destination
   * @param options     the options list, passed from the previous caller
   */
  protected final void accept(@NotNull Cell destination,
                              double distance,
                              @NotNull List<Option> options) {
    options.add(new Option(destination, distance));
    delay();
    Journey.get().dispatcher().dispatch(new ModeSuccessEvent(session, destination, type()));
  }

  /**
   * Reject a location and its distance.
   * This performs somewhat unnecessary management operations.
   * All implementations of {@link Mode} should use this method if the mode operation
   * determined that a block is unreachable given the current circumstances.
   *
   * @param destination the rejected destination
   */
  protected final void reject(@NotNull Cell destination) {
    delay();
    Journey.get().dispatcher().dispatch(new ModeFailureEvent(session, destination, type()));
  }

  private void delay() {
    // Delay the algorithm, if requested by implementation of search session
    // (Primarily used in animating the search process)
    if (session.getAlgorithmStepDelay() != 0) {
      try {
        Thread.sleep(session.getAlgorithmStepDelay());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  protected abstract void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options);

  /**
   * Get the mode type.
   *
   * @return the mode type
   */
  @NotNull
  public abstract ModeType type();

  /**
   * A record to store a movement option. It just contains a location and a distance to that location.
   */
  public static class Option {

    final Cell location;
    final double cost;

    public static Option between(Cell origin, int destinationX, int destinationY, int destinationZ) {
      Cell destination = new Cell(destinationX, destinationY, destinationZ, origin.domain());
      return new Option(destination, origin.distanceTo(destination));
    }

    public static Option between(Cell origin, Cell destination, double costMultiplier) {
      return new Option(destination, origin.distanceTo(destination));
    }

    /**
     * General constructor.
     *
     * @param location the location
     * @param cost the destination
     */
    public Option(@NotNull Cell location, double cost) {
      this.location = location;
      this.cost = cost;
    }

    /**
     * Get location.
     *
     * @return the location
     */
    @NotNull
    public Cell location() {
      return location;
    }

    /**
     * Get the distance it would take to reach the location.
     *
     * @return the distance
     */
    public double cost() {
      return cost;
    }
  }

}
