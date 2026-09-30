package forestry.core.platform.multiblock;

import forestry.api.ForestryConstants;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Per-level lifecycle cleanup for the declarative engine (plan Task 2.5, spec 7.1). Replaces the deleted
 * heavyweight {@code MultiblockEventHandler} and {@code MultiblockWorldRegistry.onWorldUnloaded} pair. The
 * new engine has no global registry or flood-fill, so the per-level state to release on unload is the
 * {@link MultiblockIndex} lookup map and the {@link DeferredValidation} retries, which are the only tick work.
 *
 * <p>Registered on the game event bus ({@link EventBusSubscriber}). Runs on the main thread for both the
 * server and client levels, each of which is a distinct {@code MultiblockIndex} key (spec 9).
 */
@EventBusSubscriber(modid = ForestryConstants.MOD_ID)
public class MultiblockEventHandler {
	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		// Drop all controller tracking for the unloaded level so deactivated or abandoned controllers don't
		// accumulate across world reloads. The old MultiblockWorldRegistry.onWorldUnloaded cleared the same
		// in-memory state, and MultiblockIndex.clear covers all of it. There is no flood-fill, orphan or
		// dead state.
		MultiblockIndex.clear(event.getLevel());
		DeferredValidation.clear(event.getLevel());
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel level) {
			DeferredValidation.retryLoaded(level);
		}
	}
}
