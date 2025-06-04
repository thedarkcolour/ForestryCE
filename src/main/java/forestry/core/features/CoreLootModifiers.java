package forestry.core.features;

import com.mojang.serialization.MapCodec;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.loot.GrafterLootModifier;
import forestry.core.loot.ConditionLootModifier;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@FeatureProvider
public class CoreLootModifiers {
	private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);

	public static final Holder<MapCodec<? extends IGlobalLootModifier>> CONDITION_MODIFIER = REGISTRY.register("condition_modifier", () -> ConditionLootModifier.CODEC);
	public static final Holder<MapCodec<? extends IGlobalLootModifier>> GRAFTER_MODIFIER = REGISTRY.register("grafter_modifier", () -> GrafterLootModifier.CODEC);
}
