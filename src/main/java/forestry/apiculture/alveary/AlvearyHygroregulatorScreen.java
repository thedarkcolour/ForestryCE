package forestry.apiculture.alveary;

import forestry.apiculture.alveary.multiblock.AlvearyHygroregulatorBlockEntity;
import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.TankWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlvearyHygroregulatorScreen extends TitledForestryScreen<AlvearyHygroregulatorMenu> {
	private final AlvearyHygroregulatorBlockEntity tile;

	public AlvearyHygroregulatorScreen(AlvearyHygroregulatorMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/hygroregulator.png", container, inventory, title);
		this.tile = container.getTile();

        this.widgetManager.add(new TankWidget(this.widgetManager, 104, 17, 0));
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}
