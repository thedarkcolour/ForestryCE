package forestry.mail.gui;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.ForestryScreen;
import forestry.mail.postoffice.MailboxBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MailboxScreen extends ForestryScreen<MailboxMenu> {
	private final MailboxBlockEntity tile;

	public MailboxScreen(MailboxMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/mailbox.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 230;
		this.imageHeight = 227;
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("mailbox");
	}
}
