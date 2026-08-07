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
		/*
		 * Factory machines use both factory block implementations.
		 * FactoryJadeProvider filters by the actual block entity, so
		 * unrelated Factory blocks such as the Rain Tank are ignored.
		 */
		registration.registerBlockComponent(
			FactoryJadeProvider.INSTANCE,
			BlockFactoryTESR.class
		);

		registration.registerBlockComponent(
			FactoryJadeProvider.INSTANCE,
			BlockFactoryPlain.class
		);

		registration.addConfig(
			FactoryJadeProvider.SHOW_PROGRESS,
			FactoryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			FactoryJadeProvider.SHOW_ERRORS,
			FactoryJadeProvider.Visibility.ON
		);
	}
}