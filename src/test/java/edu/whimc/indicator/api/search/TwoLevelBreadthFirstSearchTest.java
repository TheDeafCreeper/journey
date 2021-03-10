package edu.whimc.indicator.api.search;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.util.*;

class TwoLevelBreadthFirstSearchTest {

  Domain domain1 = new Domain("d1");
  Domain domain2 = new Domain("d2");

  static int boardSize = 12;
  static Cell[][] board1 = new Cell[boardSize][boardSize];
  static Cell[][] board2 = new Cell[boardSize][boardSize];


  @Test
  void findPath() {

    // Initialize domains to be completely free
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        board1[i][j] = new Cell(i, j, domain1);
        board2[i][j] = new Cell(i, j, domain2);
      }
    }

    // Add barriers
    board1[2][3] = null;
    board1[3][4] = null;
    board1[4][5] = null;
    board1[5][5] = null;
    board1[6][5] = null;
    board1[7][5] = null;
    board1[8][5] = null;
    board1[9][4] = null;
    board1[10][3] = null;
    board1[9][2] = null;
    board1[8][1] = null;
    board1[7][1] = null;
    board1[6][1] = null;
    board1[5][1] = null;
    board1[4][1] = null;
    board1[3][1] = null;
    board1[2][2] = null;

    board2[3][2] = null;
    board2[4][3] = null;
    board2[5][4] = null;
    board2[5][5] = null;
    board2[5][6] = null;
    board2[5][7] = null;
    board2[5][8] = null;
    board2[4][9] = null;
    board2[3][10] = null;

    // Printers for our answers
    char[][] printer1 = new char[boardSize][boardSize];
    char[][] printer2 = new char[boardSize][boardSize];

    // Set up parameters for search
    TwoLevelBreadthFirstSearch<Cell, Domain> bfs = new TwoLevelBreadthFirstSearch<>();
    Cell origin = board1[4][4];
    Cell destination = board1[4][8];
    List<Link<Cell, Domain>> links = Lists.newLinkedList();
    links.add(new Link<>(board1[8][4], board2[3][6]));
    links.add(new Link<>(board2[7][7], board1[8][8]));
    links.forEach(bfs::registerLink);

    // Clear printer board
    clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize);

    bfs.registerMode(new StepMode());
    bfs.setLocalSearchVisitationCallback(cell -> {
      System.out.printf("Distance to destination: %d\n", cell.distanceTo(links.get(1).getOrigin()));
      if (cell.getDomain().equals(domain1)) {
        printer1[cell.x][cell.y] = '+';
      }
      if (cell.getDomain().equals(domain2)) {
        printer2[cell.x][cell.y] = '+';
      }
      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    });
//    bfs.setLocalSearchVisitationCallback(x -> {});  // Reset
    bfs.setLocalSearchStepCallback(cell -> {
      if (cell.getDomain().equals(domain1)) {
        printer1[cell.x][cell.y] = '.';
      }
      if (cell.getDomain().equals(domain2)) {
        printer2[cell.x][cell.y] = '.';
      }
      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    });
//    bfs.setLocalSearchStepCallback(x -> {});  // Reset
    bfs.setFinishLocalSearchCallback(() ->
        clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize));
//    bfs.setFinishLocalSearchCallback(() -> {});  // Reset
//    bfs.setMemoryErrorCallback(() -> System.out.println("Memory error"));

    // Solve path
    List<Cell> path = bfs.findPath(origin, destination);

    // Put in path
    if (path == null) {
//      System.out.println("Path not found!");
    } else {
//      System.out.println("Path found!");
      for (Cell cell : path) {
        if (cell.domain.equals(domain1)) {
          printer1[cell.x][cell.y] = 'O';
        }
        if (cell.domain.equals(domain2)) {
          printer2[cell.x][cell.y] = 'O';
        }
      }
    }

    // Print boards
    System.out.println("Board 1:");
    printPrinter(printer1, boardSize);
    System.out.println("Board 2:");
    printPrinter(printer2, boardSize);
  }

  private void clearPrinters(Cell[][] board1, Cell[][] board2,
                             char[][] printer1, char[][] printer2,
                             Cell origin, Cell destination,
                             List<Link<Cell, Domain>> links,
                             int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board1[i][j] == null) {
          printer1[i][j] = '#';
        } else {
          printer1[i][j] = ' ';
        }
        if (board2[i][j] == null) {
          printer2[i][j] = '#';
        } else {
          printer2[i][j] = ' ';
        }
      }
    }

    // Put in origin and destination
    if (origin.getDomain().equals(domain1)) {
      printer1[origin.x][origin.y] = 'A';
    } else {
      printer2[origin.x][origin.y] = 'A';
    }
    if (origin.getDomain().equals(domain1)) {
      printer1[destination.x][destination.y] = 'B';
    } else {
      printer2[destination.x][destination.y] = 'B';
    }

    // Put in links
    for (int i = 0; i < links.size(); i++) {
      if (links.get(i).getOrigin().getDomain().equals(domain1)) {
        printer1[links.get(i).getOrigin().x][links.get(i).getOrigin().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getOrigin().getDomain().equals(domain2)) {
        printer2[links.get(i).getOrigin().x][links.get(i).getOrigin().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain1)) {
        printer1[links.get(i).getDestination().x][links.get(i).getDestination().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain2)) {
        printer2[links.get(i).getDestination().x][links.get(i).getDestination().y] = Character.forDigit(i, 10);
      }
    }
  }

  private void printPrinter(char[][] printer, int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer[i][j]);
      }
      System.out.print('\n');
    }
  }

  public static class Cell implements Locatable<Cell, Domain> {

    private final int x;
    private final int y;
    private final Domain domain;

    Cell(int x, int y, Domain domain) {
      this.x = x;
      this.y = y;
      this.domain = domain;
    }

    @Override
    public double distanceTo(Cell other) {
      return (this.x - other.x)*(this.x - other.x) + (this.y - other.y)*(this.y - other.y);
    }

    @Override
    public Domain getDomain() {
      return this.domain;
    }

    @Override
    public String print() {
      return String.format("(%d, %d, %s)", x, y, domain.getName());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Cell cell = (Cell) o;
      return x == cell.x && y == cell.y && Objects.equals(domain, cell.domain);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, domain);
    }
  }

  @AllArgsConstructor
  public static class Domain {
    @NonNull @Getter
    private final String name;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Domain domain = (Domain) o;
      return name.equals(domain.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  public class StepMode implements Mode<Cell, Domain> {

    @Override
    public Set<Cell> getDestinations(Cell origin) {
      Set<Cell> set = new HashSet<>();
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i == 0 && j == 0) continue;
          if (origin.x + i < 0) continue;
          if (origin.x + i >= boardSize) continue;
          if (origin.y + j < 0) continue;
          if (origin.y + j >= boardSize) continue;
          Cell adding = null;
          if (origin.getDomain().equals(domain1)) {
            // Make sure we can't go diagonally if adjacent borders won't allow such a move
            if (i*i*j*j == 1 && board1[origin.x + i][origin.y] == null && board1[origin.x][origin.y + j] == null) continue;
            adding = board1[origin.x + i][origin.y + j];
          }
          if (origin.getDomain().equals(domain2)) {
            // Make sure we can't go diagonally if adjacent borders won't allow such a move
            if (i*i*j*j == 1 && board2[origin.x + i][origin.y] == null && board2[origin.x][origin.y + j] == null) continue;
            adding = board2[origin.x + i][origin.y + j];
          }
          if (adding != null) {
            set.add(adding);
          }
        }
      }
      return set;
    }
  }
}