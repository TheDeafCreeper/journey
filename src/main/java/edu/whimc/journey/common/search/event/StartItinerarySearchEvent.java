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

package edu.whimc.journey.common.search.event;

import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.search.ItineraryTrial;
import edu.whimc.journey.common.search.SearchSession;

public class StartItinerarySearchEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final ItineraryTrial<T, D> itineraryTrial;

  public StartItinerarySearchEvent(SearchSession<T, D> session, ItineraryTrial<T, D> itineraryTrial) {
    super(session);
    this.itineraryTrial = itineraryTrial;
  }

  public ItineraryTrial<T, D> getItineraryTrial() {
    return itineraryTrial;
  }

  @Override
  EventType type() {
    return EventType.START_ITINERARY;
  }
}