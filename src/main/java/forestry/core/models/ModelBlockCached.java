package forestry.core.models;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class ModelBlockCached<B extends Block, K> extends ModelBlockDefault<B, K> {
	private static final Set<ModelBlockCached<?, ?>> CACHE_PROVIDERS = new HashSet<>();

	// todo test performance of this. from my experience, Cache is slow.
	private final Cache<K, BakedModel> inventoryCache;
	private final Cache<K, BakedModel> worldCache;

	public static void clear() {
		for (ModelBlockCached<?, ?> modelBlockCached : CACHE_PROVIDERS) {
			modelBlockCached.worldCache.invalidateAll();
			modelBlockCached.inventoryCache.invalidateAll();
		}
	}

	protected ModelBlockCached(Class<B> blockClass) {
		super(blockClass);

		this.worldCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
		this.inventoryCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

		CACHE_PROVIDERS.add(this);
	}

	@Override
	protected BakedModel getModel(BlockState state, ModelData extraData) {
		K key = getWorldKey(state, extraData);

		BakedModel model = this.worldCache.getIfPresent(key);
		if (model == null) {
			model = super.getModel(state, extraData);
			this.worldCache.put(key, model);
		}
		return model;
	}

	@Override
	protected BakedModel getModel(ItemStack stack, Level world) {
		K key = getInventoryKey(stack);

		BakedModel model = this.inventoryCache.getIfPresent(key);
		if (model == null) {
			model = bakeModel(stack, world, key);
			this.inventoryCache.put(key, model);
		}
		return model;
	}
}
