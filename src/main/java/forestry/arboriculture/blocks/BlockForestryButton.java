package forestry.arboriculture.blocks;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.material.PushReaction;

public class BlockForestryButton extends ButtonBlock implements IWoodTyped {
	private final ForestryWoodType type;

	public BlockForestryButton(ForestryWoodType type) {
		super(type.getBlockSetType(), 30, Properties.of().noCollission().strength(0.5f).pushReaction(PushReaction.DESTROY));

		this.type = type;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.BUTTON;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return this.type;
	}
}
