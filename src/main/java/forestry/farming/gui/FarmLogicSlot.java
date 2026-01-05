package forestry.farming.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmType;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.farming.multiblock.IFarmControllerInternal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FarmLogicSlot extends Widget {
	private final IFarmControllerInternal farmController;
	private final Direction farmDirection;

	public FarmLogicSlot(IFarmControllerInternal farmController, WidgetManager manager, int xPos, int yPos, Direction farmDirection) {
		super(manager, xPos, yPos);
		this.farmController = farmController;
		this.farmDirection = farmDirection;
	}

	private IFarmLogic getLogic() {
		return this.farmController.getFarmLogic(this.farmDirection);
	}

	private IFarmType getProperties() {
		return getLogic().getType();
	}

	private ItemStack getStackIndex() {
		return getProperties().getIcon();
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		if (!getStackIndex().isEmpty()) {
			Minecraft minecraft = Minecraft.getInstance();
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
			GuiUtil.drawItemStack(graphics, minecraft.font, getStackIndex(), startX + this.xPos, startY + this.yPos);
		}
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			return this.toolTip;
		} else {
			return null;
		}
	}

	protected final ToolTip toolTip = new ToolTip(250) {
		@Override
		public void refresh() {
            FarmLogicSlot.this.toolTip.clear();
            FarmLogicSlot.this.toolTip.add(getProperties().getDisplayName(getLogic().isManual()));
            FarmLogicSlot.this.toolTip.add(Component.translatable("for.gui.farm.fertilizer", getProperties().getFertilizerConsumption(FarmLogicSlot.this.farmController)));
            FarmLogicSlot.this.toolTip.add(Component.translatable("for.gui.farm.water", getProperties().getWaterConsumption(FarmLogicSlot.this.farmController, FarmLogicSlot.this.farmController.getFarmLedgerDelegate().getHydrationModifier())));
		}
	};
}
