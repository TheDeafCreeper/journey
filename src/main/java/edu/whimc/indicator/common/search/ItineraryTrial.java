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

package edu.whimc.indicator.common.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.Link;
import edu.whimc.indicator.common.navigation.Locatable;
import edu.whimc.indicator.common.navigation.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class ItineraryTrial<T extends Locatable<T, D>, D> {

  private final Table<Node, Node, Path<T, D>> edges = HashBasedTable.create();

  public void addEdge(Node origin, Node destination, @NotNull Path<T, D> path) {
    this.edges.put(origin, destination, path);
  }

  public Node generateNode() {
    return new Node(Double.MAX_VALUE);
  }

  public Node generateLinkNode(Link<T, D> link) {
    return new LinkNode(link, Double.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public Itinerary<T, D> findMinimumPath(Node origin, Node destination) {
    PriorityQueue<Node> toVisit = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
    Set<Node> visited = new HashSet<>();

    origin.setDistance(0);
    origin.setPrevious(null);
    toVisit.add(origin);

    Node current;
    while (!toVisit.isEmpty()) {
      current = toVisit.poll();
      visited.add(current);

      if (current.equals(destination)) {
        // Backwards traverse to get the correct itinerary, then
        //  backwards traverse again to put it back in the correct
        //  order.
        Stack<Path<T, D>> paths = new Stack<>();
        Stack<Link<T, D>> links = new Stack<>();
        while (current.getPrevious() != null) {
          paths.add(edges.get(current.getPrevious(), current));
          if (current instanceof ItineraryTrial.LinkNode) {
            links.add(((ItineraryTrial<T, D>.LinkNode) current).getLink());
          }
          current = current.getPrevious();
        }
        Itinerary itinerary = new Itinerary();
        while (!links.isEmpty()) {
          itinerary.addLinkedTrail(paths.pop(), links.pop());
        }
        if (!itinerary.addFinalTrail(paths.pop())) {
          throw new RuntimeException("Could not add final trail");
        }
        assert paths.isEmpty();
        assert links.isEmpty();
        return itinerary;
      }

      // Not yet done
      for (Map.Entry<Node, Path<T, D>> outlet : edges.row(current).entrySet()) {
        if (visited.contains(outlet.getKey())) {
          continue;
        }
        if (outlet.getKey().getDistance() > current.getDistance() + outlet.getValue().getLength()) {
          // A better path for this node would be to come from current.
          // We can assume that is already queued. Remove from waiting queue to update.
          toVisit.remove(outlet.getKey());
          outlet.getKey().setDistance(current.getDistance() + outlet.getValue().getLength() + outlet.getKey().getWeight());
          outlet.getKey().setPrevious(current);
          toVisit.add(outlet.getKey());
        }
      }
    }

    return null;  // Could not find it

  }

  public static class Node {
    /**
     * The "weight" of the node, which is the cost of traversing through this node.
     */
    @Getter
    protected double weight = 0;
    @Getter
    @Setter
    private double distance;
    @Setter
    @Getter
    private Node previous;

    private Node(double distance) {
      this.distance = distance;
    }
  }

  private class LinkNode extends Node {
    private final Link<T, D> link;

    public LinkNode(Link<T, D> link, double distance) {
      super(distance);
      this.link = link;
      weight = link.weight();
    }

    public Link<T, D> getLink() {
      return link;
    }
  }

}
