package edu.whimc.journey.common.data;

import edu.whimc.journey.common.navigation.Cell;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to handle public endpoints.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public interface PublicEndpointManager<T extends Cell<T, D>, D> {

  /**
   * Add a server endpoint.
   *
   * @param cell the cell to add
   * @param name the name of the location
   * @throws IllegalArgumentException for invalid inputs
   */
  void addPublicEndpoint(@NotNull T cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException;

  /**
   * Remove a cell. Name is irrelevant. Does nothing if cell isn't saved.
   *
   * @param cell the cell location
   */
  void removePublicEndpoint(@NotNull T cell) throws DataAccessException;

  /**
   * Remove a cell from the database by name. Cell location is irrelevant.
   * Does nothing if the name doesn't exist
   *
   * @param name the name of the location
   */
  void removePublicEndpoint(@NotNull String name) throws DataAccessException;

  /**
   * Check if a saved cell exists at this location.
   *
   * @param cell the cell location
   * @return true if the cell exists
   */
  default boolean hasPublicEndpoint(@NotNull T cell) throws DataAccessException {
    return getPublicEndpointName(cell) != null;
  }

  /**
   * Check if a saved cell exists with this name.
   *
   * @param name the cell name
   * @return true if the cell exists
   */
  default boolean hasPublicEndpoint(@NotNull String name) throws DataAccessException {
    return getPublicEndpoint(name) != null;
  }

  /**
   * Get the name of a saved location.
   *
   * @param cell the saved location
   * @return the cell's name, or null if it doesn't exist
   */
  @Nullable
  String getPublicEndpointName(@NotNull T cell) throws DataAccessException;

  /**
   * Get a specific cell by its given name.
   *
   * @param name the cell name
   * @return the cell, or null if it doesn't
   */
  @Nullable
  T getPublicEndpoint(@NotNull String name) throws DataAccessException;

  /**
   * Get a list of all saved endpoints.
   *
   * @return all names of cells mapped to their corresponding cells
   */
  Map<String, T> getPublicEndpoints() throws DataAccessException;

}
