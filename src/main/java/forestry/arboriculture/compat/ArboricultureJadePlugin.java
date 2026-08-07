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
	public void register(IWailaCommonRegistration registration) {
		// Ripening time is not normally synchronized by Forestry, so Jade
		// retrieves the authoritative value from the server.
		registration.registerBlockDataProvider(
			FruitRipenessProvider.INSTANCE,
			TileLeaves.class
		);

		registration.registerBlockDataProvider(
			FruitRipenessProvider.INSTANCE,
			TileFruitPod.class
		);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		// Species-aware sapling name.
		registration.usePickedResult(ArboricultureBlocks.SAPLING_GE.block());

		// Species-aware leaf names.
		registration.usePickedResult(ArboricultureBlocks.LEAVES.block());

		ArboricultureBlocks.LEAVES_DEFAULT.getList()
			.forEach(registration::usePickedResult);

		ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.getList()
			.forEach(registration::usePickedResult);

		ArboricultureBlocks.LEAVES_DECORATIVE.getList()
			.forEach(registration::usePickedResult);

		// Pod blocks return their actual fruit/product as the picked result,
		// e.g. Coconut, Papaya, Date, or Cocoa Beans.
		ArboricultureBlocks.PODS.getList()
			.forEach(registration::usePickedResult);

		// Fruit type is useful for fruit-bearing leaves because the leaf
		// species and active fruit allele can be different.
		registration.registerBlockComponent(
			LeafFruitProvider.INSTANCE,
			BlockAbstractLeaves.class
		);
		registration.markAsClientFeature(LeafFruitProvider.INSTANCE.getUid());

		// One configurable ripeness component shared by leaf fruit and pods.
		registration.registerBlockComponent(
			FruitRipenessProvider.INSTANCE,
			BlockAbstractLeaves.class
		);

		registration.registerBlockComponent(
			FruitRipenessProvider.INSTANCE,
			BlockFruitPod.class
		);

		// BlockFruitPod extends CocoaBlock, so Jade's vanilla crop provider
		// also adds a generic "Growth" line. Forestry already displays the
		// more appropriate "Ripeness" value, so suppress only that built-in
		// crop-progress component for Forestry pods.
		registration.addTooltipCollectedCallback((rootElement, accessor) -> {
			if (accessor instanceof BlockAccessor blockAccessor
				&& blockAccessor.getBlock() instanceof BlockFruitPod) {
				rootElement.getTooltip().remove(JadeIds.MC_CROP_PROGRESS);
			}
		});
	}
}