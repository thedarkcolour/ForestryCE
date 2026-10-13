package forestry.core.engine.genetics;

import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IFlowerType;

public class FlowerTypeManager extends DefinitionManager<IFlowerType> {
	public static final FlowerTypeManager INSTANCE = new FlowerTypeManager();

	private FlowerTypeManager() {
		super("flower_type", IFlowerType.CODEC, FlowerTypeManager::rebuild);
	}

	// datapack definitions replace code definitions with the same ID
	public static void rebuild(Map<ResourceLocation, IFlowerType> dataDefinitions) {
		((ForestryFlowerTypeManager) IForestryApi.INSTANCE.getFlowerTypeManager()).rebuild(dataDefinitions);
	}
}
