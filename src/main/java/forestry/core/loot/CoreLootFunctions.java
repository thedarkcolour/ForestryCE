package forestry.core.loot;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.loot.CountBlockFunction;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class CoreLootFunctions {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);
	private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = REGISTRY.getRegistry(Registries.LOOT_FUNCTION_TYPE);

	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<OrganismFunction>> ORGANISM = LOOT_FUNCTIONS.register("set_species_nbt", () -> new LootItemFunctionType<>(OrganismFunction.CODEC));
	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CountBlockFunction>> COUNT = LOOT_FUNCTIONS.register("count_from_block", () -> new LootItemFunctionType<>(CountBlockFunction.CODEC));
}
