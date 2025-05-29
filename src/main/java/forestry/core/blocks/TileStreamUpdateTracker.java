package forestry.core.blocks;

import forestry.core.network.packets.PacketTileStream;
import forestry.core.tiles.TileForestry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Based on https://github.com/thedarkcolour/ExDeorum/blob/1.21.1/src/main/java/thedarkcolour/exdeorum/network/VisualUpdateTracker.java
public class TileStreamUpdateTracker {
	// Use sets to avoid duplicate updates
	private static final Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> UPDATES = new HashMap<>();

	public static void sendVisualUpdate(TileForestry tile) {
		var level = tile.getLevel();

		if (level != null && !level.isClientSide) {
			Map<ChunkPos, Set<BlockPos>> chunkUpdates = UPDATES.computeIfAbsent(level.dimension(), key -> new HashMap<>());
			chunkUpdates.computeIfAbsent(new ChunkPos(tile.getBlockPos()), key -> new HashSet<>()).add(tile.getBlockPos());
		}
	}

	public static void syncVisualUpdates(MinecraftServer server) {
		for (var entry : UPDATES.entrySet()) {
			var level = server.getLevel(entry.getKey());

			if (level != null) {
				var pendingUpdates = entry.getValue();

				for (var chunkUpdates : pendingUpdates.entrySet()) {
					var chunkPos = chunkUpdates.getKey();

					for (var updatePos : chunkUpdates.getValue()) {
						if (level.getBlockEntity(updatePos) instanceof TileForestry blockEntity) {
							PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new PacketTileStream(blockEntity));
						}
					}
				}
			}
		}
	}
}
