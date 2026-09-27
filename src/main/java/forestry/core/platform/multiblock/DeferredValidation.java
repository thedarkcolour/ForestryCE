package forestry.core.platform.multiblock;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// validations that stopped at an unloaded chunk, retried once it loads because a chunk holding only a structure's
// shell has no member whose onLoad would validate again
public final class DeferredValidation {
	// outer map is concurrent because client levels are cleared on the client thread, inner maps are server thread only
	private static final Map<LevelAccessor, Map<BlockPos, LongSet>> PENDING = new ConcurrentHashMap<>();

	private DeferredValidation() {
	}

	static void defer(Level level, BlockPos member, LongSet unloadedChunks) {
		PENDING.computeIfAbsent(level, l -> new HashMap<>()).put(member.immutable(), unloadedChunks);
	}

	static void forget(Level level, BlockPos member) {
		Map<BlockPos, LongSet> pending = PENDING.get(level);
		if (pending != null && pending.remove(member) != null && pending.isEmpty()) {
			PENDING.remove(level, pending);
		}
	}

	static void clear(LevelAccessor level) {
		PENDING.remove(level);
	}

	static void retryLoaded(ServerLevel level) {
		Map<BlockPos, LongSet> pending = PENDING.get(level);
		if (pending == null) {
			return;
		}
		List<BlockPos> ready = new ArrayList<>();
		for (Map.Entry<BlockPos, LongSet> entry : pending.entrySet()) {
			for (LongIterator it = entry.getValue().iterator(); it.hasNext(); ) {
				long chunk = it.nextLong();
				if (isLoaded(level, ChunkPos.getX(chunk), ChunkPos.getZ(chunk))) {
					ready.add(entry.getKey());
					break;
				}
			}
		}
		for (BlockPos member : ready) {
			// validating again re-defers the member if another chunk is still missing
			forget(level, member);
			if (isLoaded(level, member.getX() >> 4, member.getZ() >> 4)) {
				MultiblockValidation.validateAt(level, member);
			}
		}
	}

	// hasChunk is what the pattern checks, and getChunkNow means sampling the chunk won't force a synchronous load
	private static boolean isLoaded(ServerLevel level, int chunkX, int chunkZ) {
		ServerChunkCache chunks = level.getChunkSource();
		return chunks.hasChunk(chunkX, chunkZ) && chunks.getChunkNow(chunkX, chunkZ) != null;
	}
}
