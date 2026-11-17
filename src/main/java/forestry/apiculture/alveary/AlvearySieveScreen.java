package forestry.apiculture.alveary;

import forestry.apiculture.alveary.multiblock.AlvearySieveBlockEntity;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlvearySieveScreen extends TitledForestryScreen<AlvearySieveMenu> {
	private final AlvearySieveBlockEntity tile;

	public AlvearySieveScreen(AlvearySieveMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/sieve.png", container, inventory, title);
		this.tile = container.getTile();
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}
