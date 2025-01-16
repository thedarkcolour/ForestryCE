package forestry.arboriculture.blocks;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.material.PushReaction;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;

public class BlockForestryButton extends ButtonBlock implements IWoodTyped {
	private final ForestryWoodType type;

	public BlockForestryButton(ForestryWoodType type) {
		super(Properties.of().noCollission().strength(0.5f).pushReaction(PushReaction.DESTROY), type.getBlockSetType(), 30, true);

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
