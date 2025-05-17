package forestry.arboriculture;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

import forestry.api.arboriculture.ICharcoalManager;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.arboriculture.IWoodAccess;

public class TreeManager implements ITreeManager {
	private final ImmutableMap<Block, Block> refractoryWaxables;
	private final ICharcoalManager charcoalManager;

	public TreeManager(ImmutableMap<Block, Block> refractoryWaxables, ICharcoalManager charcoalManager) {
		this.refractoryWaxables = refractoryWaxables;
		this.charcoalManager = charcoalManager;
	}

	@Nullable
	@Override
	public Block getRefractoryWaxed(Block block) {
		return this.refractoryWaxables.get(block);
	}

	@Override
	public ICharcoalManager getCharcoalManager() {
		return this.charcoalManager;
	}

	@Override
	public IWoodAccess getWoodAccess() {
		// todo only make accessible after wood access is populated
		return WoodAccess.INSTANCE;
	}
}
