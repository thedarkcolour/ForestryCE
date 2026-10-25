package forestry.core.platform.block;

import forestry.core.platform.network.packets.PacketTileStream;
import forestry.core.platform.tile.ForestryBlockEntity;
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
	private static final Map<ResourceKey<Level>, Map<ChunkPos, Set<BlockPos>>> UPDATES = new HashMap<>();

	public static void sendVisualUpdate(ForestryBlockEntity tile) {
		var level = tile.getLevel();

		if (level != null && !level.isClientSide) {
			Map<ChunkPos, Set<BlockPos>> chunkUpdates = UPDATES.computeIfAbsent(level.dimension(), key -> new HashMap<>());
			chunkUpdates.computeIfAbsent(new ChunkPos(tile.getBlockPos()), key -> new HashSet<>()).add(tile.getBlockPos());
		}
	}

	public static void syncVisualUpdates(MinecraftServer server) {
		for (var levelUpdates : UPDATES.entrySet()) {
			var level = server.getLevel(levelUpdates.getKey());

			if (level != null) {
				var pendingUpdates = levelUpdates.getValue();

				for (var entry : pendingUpdates.entrySet()) {
					var chunkPos = entry.getKey();

					for (var updatePos : entry.getValue()) {
						if (level.getBlockEntity(updatePos) instanceof ForestryBlockEntity blockEntity) {
							PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new PacketTileStream(blockEntity));
						}
					}
				}

				pendingUpdates.clear();
			}
		}
	}
}
