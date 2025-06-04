package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.circuits.CircuitBoard;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class CoreDataComponents {
	private static final DeferredRegister<DataComponentType<?>> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(Registries.DATA_COMPONENT_TYPE);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CircuitBoard>> CIRCUIT_BOARD = REGISTRY.register("circuit_board", );
}
