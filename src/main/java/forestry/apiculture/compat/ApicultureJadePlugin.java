package forestry.apiculture.compat;

import forestry.apiculture.tiles.TileApiary;
import forestry.core.blocks.BlockBase;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ApicultureJadePlugin implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(
			ApiaryJadeProvider.INSTANCE,
			TileApiary.class
		);

		registration.registerItemStorage(
			ApiaryItemStorageProvider.INSTANCE,
			TileApiary.class
		);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(
			ApiaryJadeProvider.INSTANCE,
			BlockBase.class
		);

		registration.registerItemStorageClient(
			ApiaryItemStorageProvider.INSTANCE
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_PROGRESS,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_ERRORS,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_QUEEN,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_STOCK,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_DRONES,
			ApiaryJadeProvider.Visibility.ON
		);

		/*
		 * Default stats behavior:
		 *
		 * normal -> compact
		 * Shift  -> expanded
		 */
		registration.addConfig(
			ApiaryJadeProvider.COMPACT_STATS,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.EXPANDED_STATS,
			ApiaryJadeProvider.Visibility.SHIFT
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_FRAMES,
			ApiaryJadeProvider.Visibility.ON
		);

		registration.addConfig(
			ApiaryJadeProvider.SHOW_OUTPUT,
			ApiaryJadeProvider.Visibility.ON
		);
	}
}