package forestry.worktable.screens;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.buttons.GuiBetterButton;
import forestry.core.gui.buttons.StandardButtonTextureSets;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.utils.SoundUtil;
import forestry.worktable.recipes.RecipeMemory;
import forestry.worktable.screens.widgets.ClearWorktable;
import forestry.worktable.screens.widgets.MemorizedRecipeSlot;
import forestry.worktable.tiles.WorktableTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorktableScreen extends GuiForestryTitled<WorktableMenu> {
	private static final int SPACING = 18;

	private final WorktableTile worktable;
	private boolean hasRecipeConflict;

	public WorktableScreen(WorktableMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/worktable2.png", container, inv, title);

		this.worktable = container.getTile();
		this.imageHeight = 218;

		RecipeMemory memory = this.worktable.getMemory();

		int slot = 0;
		for (int y = 0; y < 3; y++) {
			int yPos = 20 + y * SPACING;

			for (int x = 0; x < 3; x++) {
				int xPos = 110 + x * SPACING;

                this.widgetManager.add(new MemorizedRecipeSlot(this.widgetManager, xPos, yPos, memory, slot++));
			}
		}

        this.widgetManager.add(new ClearWorktable(this.widgetManager, 66, 19));
	}

	@Override
	public void containerTick() {
		super.containerTick();

		if (this.hasRecipeConflict != this.worktable.hasRecipeConflict()) {
            this.hasRecipeConflict = this.worktable.hasRecipeConflict();
			if (this.hasRecipeConflict) {
				addButtons();
			} else {
                this.renderables.clear();
			}
		}
	}

	private void addButtons() {
		addRenderableWidget(new GuiBetterButton(this.leftPos + 76, this.topPos + 56, StandardButtonTextureSets.LEFT_BUTTON_SMALL, b -> {
            IForestryPacketServer packet = new PacketGuiSelectRequest(100, 0);
            PacketDistributor.sendToServer(packet);
            SoundUtil.playButtonClick();
		}));
		addRenderableWidget(new GuiBetterButton(this.leftPos + 85, this.topPos + 56, StandardButtonTextureSets.RIGHT_BUTTON_SMALL, b -> {
            IForestryPacketServer packet = new PacketGuiSelectRequest(101, 0);
            PacketDistributor.sendToServer(packet);
            SoundUtil.playButtonClick();
		}));
	}


	@Override
	protected void addLedgers() {
		addErrorLedger(this.worktable);
		addHintLedger("worktable");
	}
}
