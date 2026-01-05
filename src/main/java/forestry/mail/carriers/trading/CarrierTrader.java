package forestry.mail.carriers.trading;

import forestry.api.ForestryConstants;
import forestry.api.client.IForestryClientApi;
import forestry.api.mail.*;
import forestry.mail.MailAddress;
import forestry.mail.postalstates.EnumDeliveryState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CarrierTrader implements IPostalCarrier {
	private final ResourceLocation iconID;

	public CarrierTrader() {
		this.iconID = ForestryConstants.forestry("mail/carrier.trader");
	}

	@Override
	public String getDescriptionId() {
		return "for.gui.addressee.trader";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getSprite() {
		return IForestryClientApi.INSTANCE.getTextureManager().getSprite(this.iconID);
	}

	@Override
	public IPostalState deliverLetter(ServerLevel world, IPostOffice office, IMailAddress recipient, ItemStack letterStack, boolean doDeliver) {
		ITradeStation trade = TradeStationRegistry.getOrCreate(world).getTradeStation(recipient);
		if (trade == null) {
			return EnumDeliveryState.NO_MAILBOX;
		}

		return trade.handleLetter(world, recipient, letterStack, doDeliver);
	}

	@Override
	public IMailAddress getRecipient(MinecraftServer minecraftServer, String recipientName) {
		return new MailAddress(recipientName);
	}

	@Override
	public String toString() {
		return "trader";
	}
}
