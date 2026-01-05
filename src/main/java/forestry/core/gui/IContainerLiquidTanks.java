package forestry.core.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public interface IContainerLiquidTanks {
	@OnlyIn(Dist.CLIENT)
	void handlePipetteClickClient(int slot, Player player);

	void handlePipetteClick(int slot, ServerPlayer player);

	@Nullable
	IFluidTank getTank(int slot);
}
