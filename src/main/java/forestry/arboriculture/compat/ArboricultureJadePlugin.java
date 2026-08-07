package forestry.arboriculture.compat;

import forestry.arboriculture.blocks.BlockAbstractLeaves;
import forestry.arboriculture.blocks.BlockFruitPod;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.tiles.TileFruitPod;
import forestry.arboriculture.tiles.TileLeaves;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ArboricultureJadePlugin implements IWailaPlugin {
	@Override
	public void register(
		IWailaCommonRegistration registration
	) {
		registration.registerBlockDataProvider(
			FruitJadeProvider.INSTANCE,
			TileLeaves.class
		);

		registration.registerBlockDataProvider(
			FruitJadeProvider.INSTANCE,
			TileFruitPod.class
		);
	}

	@Override
	public void registerClient(
		IWailaClientRegistration registration
	) {
		/*
		 * Use Forestry's pick-block results so Jade gets the proper
		 * species-aware names for genetic saplings and leaves.
		 */
		registration.usePickedResult(
			ArboricultureBlocks.SAPLING_GE.block()
		);

		registration.usePickedResult(
			ArboricultureBlocks.LEAVES.block()
		);

		ArboricultureBlocks.LEAVES_DEFAULT
			.getList()
			.forEach(registration::usePickedResult);

		ArboricultureBlocks.LEAVES_DEFAULT_FRUIT
			.getList()
			.forEach(registration::usePickedResult);

		ArboricultureBlocks.LEAVES_DECORATIVE
			.getList()
			.forEach(registration::usePickedResult);

		ArboricultureBlocks.PODS
			.getList()
			.forEach(registration::usePickedResult);

		/*
		 * One master Jade provider:
		 *
		 * Fruit Details
		 *   Fruit Type: OFF / ON / SHIFT
		 *   Growth:     OFF / ON / SHIFT
		 */
		registration.registerBlockComponent(
			FruitJadeProvider.INSTANCE,
			BlockAbstractLeaves.class
		);

		registration.registerBlockComponent(
			FruitJadeProvider.INSTANCE,
			BlockFruitPod.class
		);

		registration.addConfig(
			FruitJadeProvider.SHOW_FRUIT_TYPE,
			FruitJadeProvider.Visibility.ON
		);

		registration.addConfig(
			FruitJadeProvider.SHOW_GROWTH,
			FruitJadeProvider.Visibility.ON
		);

		/*
		 * BlockFruitPod extends CocoaBlock, which makes Jade add its
		 * vanilla crop-growth component. Forestry supplies its own
		 * accurate fruit-growth value instead.
		 */
		registration.addTooltipCollectedCallback(
			(rootElement, accessor) -> {
				if (
					accessor instanceof BlockAccessor blockAccessor
						&& blockAccessor.getBlock()
							instanceof BlockFruitPod
				) {
					rootElement
						.getTooltip()
						.remove(
							JadeIds.MC_CROP_PROGRESS
						);
				}
			}
		);
	}
}