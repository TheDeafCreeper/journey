package edu.whimc.journey.spigot.data;

import edu.whimc.journey.common.data.sql.DataConverter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import java.util.UUID;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class SpigotDataConverter implements DataConverter<LocationCell, World> {

  @Override
  @NotNull
  public String getDomainIdentifier(@NotNull World domain) {
    return domain.getUID().toString();
  }

  @Override
  @NotNull
  public LocationCell makeCell(int x, int y, int z, @NotNull String domainId) {
    return new LocationCell(x, y, z, UUID.fromString(domainId));
  }

}
