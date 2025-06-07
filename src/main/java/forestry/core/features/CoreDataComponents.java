package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.circuits.CircuitBoard;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

@FeatureProvider
public class CoreDataComponents {
	private static final DeferredRegister<DataComponentType<?>> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(Registries.DATA_COMPONENT_TYPE);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CircuitBoard>> CIRCUIT_BOARD = type("circuit_board", builder -> builder.persistent(CircuitBoard.CODEC));

	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> type(String name, UnaryOperator<DataComponentType.Builder<T>> configure) {
		return REGISTRY.register(name, () -> configure.apply(DataComponentType.builder()).build());
	}
}
