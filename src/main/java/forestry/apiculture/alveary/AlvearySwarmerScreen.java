package forestry.apiculture.alveary;

import forestry.apiculture.alveary.multiblock.AlvearySwarmerBlockEntity;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlvearySwarmerScreen extends TitledForestryScreen<AlvearySwarmerMenu> {
	private final AlvearySwarmerBlockEntity tile;

	public AlvearySwarmerScreen(AlvearySwarmerMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/swarmer.png", container, inventory, title);
		this.tile = container.getTile();
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}
