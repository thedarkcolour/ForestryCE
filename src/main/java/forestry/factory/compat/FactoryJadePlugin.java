package forestry.factory.compat;

import forestry.factory.blocks.BlockFactoryPlain;
import forestry.factory.blocks.BlockFactoryTESR;
import forestry.factory.tiles.TileBottler;
import forestry.factory.tiles.TileCarpenter;
import forestry.factory.tiles.TileCentrifuge;
import forestry.factory.tiles.TileFabricator;
import forestry.factory.tiles.TileFermenter;
import forestry.factory.tiles.TileMillRainmaker;
import forestry.factory.tiles.TileMoistener;
import forestry.factory.tiles.TileSqueezer;
import forestry.factory.tiles.TileStill;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FactoryJadePlugin implements IWailaPlugin {
	@Override
	public void register(
		IWailaCommonRegistration registration
	) {
		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileFabricator.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileSqueezer.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileCentrifuge.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileStill.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileFermenter.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileCarpenter.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileBottler.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileMillRainmaker.class
		);

		registration.registerBlockDataProvider(
			FactoryJadeProvider.INSTANCE,
			TileMoistener.class
		);
	}

	@Override
	public void registerClient(
		IWailaClientRegistration registration
	) {
		registration.registerBlockComponent(
			FactoryJadeProvider.INSTANCE,
			BlockFactoryTESR.class
		);

		registration.registerBlockComponent(
			FactoryJadeProvider.INSTANCE,
			BlockFactoryPlain.class
		);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.FABRICATOR_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.FABRICATOR_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.FABRICATOR_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.FABRICATOR_HEAT_CONFIG, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.FABRICATOR_RECIPE_CONFIG, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.SQUEEZER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.SQUEEZER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.SQUEEZER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.SQUEEZER_REMNANT_CONFIG, FactoryJadeProvider.Visibility.SHIFT);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.CENTRIFUGE_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.CENTRIFUGE_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.CENTRIFUGE_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.CENTRIFUGE_PRODUCTS_CONFIG, FactoryJadeProvider.Visibility.SHIFT);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.STILL_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.STILL_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.STILL_ERRORS, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.FERMENTER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.FERMENTER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.FERMENTER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.FERMENTER_FUEL_CONFIG, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.CARPENTER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.CARPENTER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.CARPENTER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.CARPENTER_RECIPE_CONFIG, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.BOTTLER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.BOTTLER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.BOTTLER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.BOTTLER_MODE_CONFIG, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.RAINMAKER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.RAINMAKER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.RAINMAKER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.RAINMAKER_STATUS_CONFIG, FactoryJadeProvider.Visibility.ON);

		registerMachineCategory(
			registration,
			FactoryJadeProvider.MOISTENER_DETAILS
		);
		registerOption(registration, FactoryJadeProvider.MOISTENER_PROGRESS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.MOISTENER_ERRORS, FactoryJadeProvider.Visibility.ON);
		registerOption(registration, FactoryJadeProvider.MOISTENER_SPEED_CONFIG, FactoryJadeProvider.Visibility.ON);
		registerOption(
			registration,
			FactoryJadeProvider.MOISTENER_RESOURCE_PROGRESS_CONFIG,
			FactoryJadeProvider.Visibility.ON
		);
	}

	private static void registerMachineCategory(
		IWailaClientRegistration registration,
		ResourceLocation key
	) {
		registration.addConfig(
			key,
			FactoryJadeProvider.Visibility.ON
		);
	}

	private static void registerOption(
		IWailaClientRegistration registration,
		ResourceLocation key,
		FactoryJadeProvider.Visibility defaultVisibility
	) {
		registration.addConfig(key, defaultVisibility);
	}
}
