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

package net.whimxiqal.journey.search.event;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.SearchSession;

/**
 * An event dispatched by the search process when a mode is found to be successful
 * in stepping a new location.
 *
 */
public class ModeSuccessEvent extends SearchEvent {

  private final Cell cell;
  private final ModeType modeType;

  /**
   * General constructor.
   *
   * @param session  the search session
   * @param cell     the location
   * @param modeType the type of mode
   */
  public ModeSuccessEvent(SearchSession session, Cell cell, ModeType modeType) {
    super(session);
    this.cell = cell;
    this.modeType = modeType;
  }

  /**
   * Get the cell that we can reach.
   *
   * @return the cell
   */
  public Cell getCell() {
    return cell;
  }

  /**
   * Get the type of mode.
   *
   * @return the mode type
   */
  public ModeType getModeType() {
    return modeType;
  }

  @Override
  EventType type() {
    return EventType.MODE_SUCCESS;
  }
}
