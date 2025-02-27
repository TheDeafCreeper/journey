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

import net.whimxiqal.journey.search.PathTrial;
import net.whimxiqal.journey.search.SearchSession;

/**
 * A search event to dispatch when a search stops considering cached paths in the overall process.
 * This usually happens when no different solution can be found than the one already found
 * by continuing to use the cached paths, so in an attempt to get a better solution,
 * we ignore the cached objects and recalculate them because the results may change.
 *
 * @see PathTrial#attempt(boolean)
 * @see SearchSession
 */
public class IgnoreCacheSearchEvent extends SearchEvent {

  /**
   * General constructor.
   *
   * @param session the session
   */
  public IgnoreCacheSearchEvent(SearchSession session) {
    super(session);
  }

  @Override
  EventType type() {
    return EventType.IGNORE_CACHE;
  }

}
