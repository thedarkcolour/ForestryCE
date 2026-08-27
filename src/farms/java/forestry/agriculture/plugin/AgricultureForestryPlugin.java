package forestry.agriculture.plugin;

import net.minecraft.resources.ResourceLocation;

import forestry.api.core.circuits.ForestryCircuitLayouts;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.plugin.ICircuitRegistration;
import forestry.api.plugin.IFarmingRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.core.features.CoreItems;
import forestry.core.content.resources.EnumElectronTube;
import forestry.agriculture.multifarm.circuits.MultifarmCircuit;

/**
 * Base Forestry's farming registrations. Split out of {@code forestry.core.plugin.DefaultForestryPlugin}
 * so the base artifact does not register farm content. The machine-upgrade layout and its circuits
 * both stay with core - registering the layout here crashed a core-only install, because core
 * registers circuits against it. Circuits key by string id, so splitting registerCircuits across
 * two plugins changes nothing.
 */
public class AgricultureForestryPlugin implements IForestryPlugin {
	@Override
	public void registerCircuits(ICircuitRegistration circuits) {
		circuits.registerLayout(ForestryCircuitLayouts.MANAGED_FARM, ForestryCircuitSocketTypes.FARM);
		circuits.registerLayout(ForestryCircuitLayouts.MANUAL_FARM, ForestryCircuitSocketTypes.FARM);

		// Managed Farms
		registerFarmCircuit(circuits, EnumElectronTube.COPPER, ForestryFarmTypes.ARBOREAL, false);
		registerFarmCircuit(circuits, EnumElectronTube.TIN, ForestryFarmTypes.PEAT, false);
		registerFarmCircuit(circuits, EnumElectronTube.BRONZE, ForestryFarmTypes.CROPS, false);
		registerFarmCircuit(circuits, EnumElectronTube.IRON, ForestryFarmTypes.ENDER, false);
		registerFarmCircuit(circuits, EnumElectronTube.BLAZE, ForestryFarmTypes.INFERNAL, false);
		registerFarmCircuit(circuits, EnumElectronTube.OBSIDIAN, ForestryFarmTypes.GOURD, false);
		registerFarmCircuit(circuits, EnumElectronTube.APATITE, ForestryFarmTypes.SHROOM, false);

		// Manual Farms
		registerFarmCircuit(circuits, EnumElectronTube.COPPER, ForestryFarmTypes.ORCHARD, true);
		registerFarmCircuit(circuits, EnumElectronTube.TIN, ForestryFarmTypes.PEAT, true);
		registerFarmCircuit(circuits, EnumElectronTube.BRONZE, ForestryFarmTypes.CROPS, true);
		registerFarmCircuit(circuits, EnumElectronTube.IRON, ForestryFarmTypes.ENDER, true);
		registerFarmCircuit(circuits, EnumElectronTube.GOLD, ForestryFarmTypes.SUCCULENTES, true);
		registerFarmCircuit(circuits, EnumElectronTube.DIAMOND, ForestryFarmTypes.POALES, true);
		registerFarmCircuit(circuits, EnumElectronTube.OBSIDIAN, ForestryFarmTypes.GOURD, true);
		registerFarmCircuit(circuits, EnumElectronTube.APATITE, ForestryFarmTypes.SHROOM, true);
		registerFarmCircuit(circuits, EnumElectronTube.LAPIS, ForestryFarmTypes.COCOA, true);
	}

	private static void registerFarmCircuit(ICircuitRegistration circuits, EnumElectronTube tube, ResourceLocation typeId, boolean manual) {
		String id = manual ? "farm.manual." + typeId.getPath() : "farm.managed." + typeId.getPath();
		circuits.registerCircuit(manual ? ForestryCircuitLayouts.MANUAL_FARM : ForestryCircuitLayouts.MANAGED_FARM, CoreItems.ELECTRON_TUBES.stack(tube, 1), new MultifarmCircuit(id, typeId, manual));
	}

	@Override
	public void registerFarming(IFarmingRegistration farming) {
		DefaultFarms.registerFarmTypes(farming);

	}

	@Override
	public ResourceLocation id() {
		return ForestryModuleIds.FARMING;
	}
}
