package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.ServerEndpointManager;
import edu.whimc.journey.common.tools.BufferedSupplier;
import edu.whimc.journey.common.util.Extra;
import edu.whimc.journey.common.util.Validator;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NavServerCommand extends FunctionlessCommandNode {

  public NavServerCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.NAV_USE_PERMISSION,
        "Use server-wide locations in paths",
        "server");

    addChildren(new NavServerToCommand(this));
    addChildren(new NavServerDeleteCommand(this));
    addChildren(new NavServerListCommand(this));
    addChildren(new NavServerSaveCommand(this));
  }

  private static BufferedSupplier<List<String>> bufferedServerLocationSupplier() {
    return new BufferedSupplier<>(() -> {
      try {
        return JourneySpigot.getInstance().getDataManager()
            .getServerEndpointManager()
            .getServerEndpoints().keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  public static class NavServerToCommand extends PlayerCommandNode {

    public NavServerToCommand(@NotNull CommandNode parent) {
      super(parent,
          Permissions.NAV_USE_PERMISSION,
          "Blaze a trail to a server destination",
          "to");

      BufferedSupplier<List<String>> serverLocationSupplier = bufferedServerLocationSupplier();
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .allowedEntries((src, prev) -> serverLocationSupplier.get())
              .strict(false)
              .build())
          .build(), "Use a name");

    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      LocationCell endLocation;
      ServerEndpointManager<LocationCell, World> serverEndpointManager = JourneySpigot.getInstance()
          .getDataManager()
          .getServerEndpointManager();
      try {
        endLocation = serverEndpointManager.getServerEndpoint(args[0]);

        if (endLocation == null) {
          player.spigot().sendMessage(Format.error("The server location ",
              Format.toPlain(Format.note(args[0])),
              " could not be found."));
          return false;
        }
      } catch (IllegalArgumentException e) {
        player.spigot().sendMessage(Format.error("Your numbers could not be read."));
        return false;
      }

      if (NavCommand.blazeTrailTo(player, endLocation, flags)) {

        // Check if we should save a server endpoint
        if (args.length >= 5) {
          if (serverEndpointManager.hasServerEndpoint(endLocation)) {
            player.spigot().sendMessage(Format.error("A server location already exists at that location!"));
            return false;
          }
          if (serverEndpointManager.hasServerEndpoint(args[4])) {
            player.spigot().sendMessage(Format.error("A server location already exists with that name!"));
            return false;
          }
          if (!Validator.isValidDataName(args[4])) {
            player.spigot().sendMessage(Format.error("Your server name ",
                Format.toPlain(Format.note(args[4])),
                " contains illegal characters."));
            return false;
          }
          // Save it!
          serverEndpointManager.addServerEndpoint(endLocation, args[4]);
          player.spigot().sendMessage(Format.success("Saved your server location with name ",
              Format.toPlain(Format.note(args[4])),
              "!"));
        }

        return true;
      } else {
        return false;
      }

    }
  }

  public static class NavServerDeleteCommand extends PlayerCommandNode {

    public NavServerDeleteCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.NAV_MANAGE_PERMISSION,
          "Delete a saved server destination",
          "delete");

      BufferedSupplier<List<String>> serverLocationSupplier = bufferedServerLocationSupplier();
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .allowedEntries((src, prev) -> serverLocationSupplier.get())
              .strict(false)
              .build())
          .build(), "Remove a previously saved server location");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {
      if (args.length < 1) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      ServerEndpointManager<LocationCell, World> endpointManager = JourneySpigot.getInstance()
          .getDataManager()
          .getServerEndpointManager();
      if (endpointManager.hasServerEndpoint(args[0])) {
        JourneySpigot.getInstance().getDataManager().getServerEndpointManager().removeServerEndpoint(args[0]);
        player.spigot().sendMessage(Format.success("The server location ",
            Format.toPlain(Format.note(args[0])),
            " has been removed."));
        return true;
      } else {
        player.spigot().sendMessage(Format.error("The server location ",
            Format.toPlain(Format.note(args[0])),
            " could not be found."));
        return false;
      }
    }


  }

  public static class NavServerListCommand extends PlayerCommandNode {


    public NavServerListCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.NAV_USE_PERMISSION,
          "List saved server destinations",
          "list");
      addSubcommand(Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .strict(false)
                  .usage("[page]")
                  .build())
              .build(),
          "View saved server locations");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {
      int pageNumber;
      if (args.length > 0) {
        try {
          pageNumber = Integer.parseInt(args[0]);

          if (pageNumber < 0) {
            player.spigot().sendMessage(Format.error("The page number may not be negative!"));
            return false;
          }
        } catch (NumberFormatException e) {
          player.spigot().sendMessage(Format.error("The page number must be an integer."));
          return false;
        }
      } else {
        pageNumber = 1;
      }

      Map<String, LocationCell> cells = JourneySpigot.getInstance()
          .getDataManager()
          .getServerEndpointManager()
          .getServerEndpoints();

      if (cells.isEmpty()) {
        player.spigot().sendMessage(Format.warn("There are no saved server locations yet!"));
        return true;
      }

      List<Map.Entry<String, LocationCell>> sortedEntryList = new ArrayList<>(cells.entrySet());
      sortedEntryList.sort(Map.Entry.comparingByKey());

      StringBuilder builder = new StringBuilder();
      sortedEntryList.forEach(entry -> builder
          .append(Format.ACCENT2)
          .append(entry.getKey())
          .append(Format.DEFAULT)
          .append(" > ")
          .append(Format.toPlain(Format.locationCell(entry.getValue(), Format.DEFAULT)))
          .append("\n"));
      ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(builder.toString(),
          pageNumber,
          ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH,
          ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 1);

      pageNumber = Math.min(pageNumber, chatPage.getTotalPages());

      player.spigot().sendMessage(Format.success("Server Locations - Page ",
          Format.toPlain(Format.note(Integer.toString(pageNumber))),
          " of ",
          Format.toPlain(Format.note(Integer.toString(chatPage.getTotalPages())))));
      Arrays.stream(chatPage.getLines()).forEach(player::sendMessage);

      return true;
    }

  }

  public static class NavServerSaveCommand extends PlayerCommandNode {

    public NavServerSaveCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.NAV_MANAGE_PERMISSION,
          "Save your current location as a server trail location",
          "save");
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .build())
          .build(), "Save with this name");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      String name = args[0];
      if (!Validator.isValidDataName(name)) {
        player.spigot().sendMessage(Format.error("That name is invalid."));
        return false;
      }

      ServerEndpointManager<LocationCell, World> serverEndpointManager = JourneySpigot.getInstance()
          .getDataManager()
          .getServerEndpointManager();

      String existingName = serverEndpointManager.getServerEndpointName(new LocationCell(player.getLocation()));
      if (existingName != null) {
        player.spigot().sendMessage(Format.error("Server location ",
            Format.toPlain(Format.note(existingName)),
            " already exists at that location!"));
        return false;
      }

      LocationCell existingCell = serverEndpointManager.getServerEndpoint(name);
      if (existingCell != null) {
        player.spigot().sendMessage(Format.error("A server location already exists with that name at",
            Format.toPlain(Format.locationCell(existingCell, Format.DEFAULT)),
            "!"));
        return false;
      }

      serverEndpointManager.addServerEndpoint(new LocationCell(player.getLocation()), name);
      player.spigot().sendMessage(Format.success("Added server location named ",
          Format.toPlain(Format.note(name))));
      return true;
    }
  }

}
