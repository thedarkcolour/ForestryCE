package forestry.core.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IContainerSocketed {
	@OnlyIn(Dist.CLIENT)
	void handleChipsetClick(int slot);

	void handleChipsetClickServer(int slot, ServerPlayer player, ItemStack itemstack);

	@OnlyIn(Dist.CLIENT)
	void handleSolderingIronClick(int slot);

	void handleSolderingIronClickServer(int slot, ServerPlayer player, ItemStack itemstack);
}
