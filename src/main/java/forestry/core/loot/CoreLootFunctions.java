package forestry.core.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.loot.CountBlockFunction;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreLootFunctions {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);
	private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = REGISTRY.getRegistry(Registries.LOOT_FUNCTION_TYPE);

	public static final RegistryObject<LootItemFunctionType> ORGANISM = LOOT_FUNCTIONS.register("set_species_nbt", () -> new LootItemFunctionType(new OrganismFunction.Serializer()));
	public static final RegistryObject<LootItemFunctionType> COUNT = LOOT_FUNCTIONS.register("count_from_block", () -> new LootItemFunctionType(new CountBlockFunction.Serializer()));
}
