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

package me.pietelite.journey.common.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PersonalWaypointManager;
import me.pietelite.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of an endpoint manager for SQL and a personal endpoint manager.
 */
public class SqlPersonalWaypointManager
    extends SqlWaypointManager
    implements PersonalWaypointManager {

  /**
   * Default constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlPersonalWaypointManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void add(@NotNull UUID playerUuid, @NotNull Cell cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    this.addWaypoint(playerUuid, cell, name);
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    this.removeWaypoint(playerUuid, cell);
  }

  @Override
  public void setPublic(@NotNull UUID playerUuid, @NotNull String name, boolean isPublic) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "UPDATE %s SET %s = ? WHERE %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          "is_public",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setBoolean(2, isPublic);
      statement.setString(3, name.toLowerCase());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    this.removeWaypoint(playerUuid, name);
  }

  @Override
  public @Nullable String getName(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    return this.getWaypointName(playerUuid, cell);
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    return super.getWaypoint(playerUuid, name);
  }

  @Override
  public @Nullable Boolean isPublic(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT is_public FROM %s WHERE %s %s ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getBoolean("is_public");
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Map<String, Cell> getAll(@NotNull UUID playerUuid)
      throws DataAccessException {
    return this.getWaypoints(playerUuid);
  }

  @Override
  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + WAYPOINT_TABLE_NAME + " ("
          + "player_uuid char(36), "
          + "name_id varchar(32) NOT NULL, "
          + "name varchar(32) NOT NULL, "
          + "domain_id char(36) NOT NULL, "
          + "x int(7) NOT NULL, "
          + "y int(7) NOT NULL, "
          + "z int(7) NOT NULL, "
          + "timestamp integer NOT NULL, "
          + "is_public " + getConnectionController().booleanType() + " NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS player_uuid_idx ON "
          + WAYPOINT_TABLE_NAME
          + " (player_uuid);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
