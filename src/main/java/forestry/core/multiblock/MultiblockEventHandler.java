package forestry.core.multiblock;

import forestry.api.ForestryConstants;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Per-level lifecycle cleanup for the new declarative engine (plan Task 2.5; spec §7.1). Replaces the
 * deleted heavyweight {@code MultiblockEventHandler}/{@code MultiblockWorldRegistry.onWorldUnloaded}
 * pair: the new engine has no global registry, flood-fill, or tick loop, so the only per-level state to
 * release on unload is the {@link MultiblockIndex} lookup map.
 *
 * <p>Registered on the FORGE event bus ({@link Mod.EventBusSubscriber}); runs on the main thread for both
 * the server and client levels (each is a distinct {@code MultiblockIndex} key, spec §9).
 */
@Mod.EventBusSubscriber(modid = ForestryConstants.MOD_ID)
public class MultiblockEventHandler {
	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		// Drop all controller tracking for the unloaded level so deactivated/abandoned controllers don't
		// accumulate across world reloads (the old MultiblockWorldRegistry.onWorldUnloaded cleared the same
		// in-memory state; MultiblockIndex.clear covers all of it — there is no flood-fill/orphan/dead state).
		MultiblockIndex.clear(event.getLevel());
	}
}
