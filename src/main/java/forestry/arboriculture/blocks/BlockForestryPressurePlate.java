package forestry.arboriculture.blocks;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BlockForestryPressurePlate extends PressurePlateBlock implements IWoodTyped {
	private final ForestryWoodType type;

	public BlockForestryPressurePlate(ForestryWoodType type) {
		super(type.getBlockSetType(), Properties.of().mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(0.5f).ignitedByLava().pushReaction(PushReaction.DESTROY));

		this.type = type;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.PRESSURE_PLATE;
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
