package forestry.core;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.common.MinecraftForge;

import forestry.core.genetics.root.ClientBreedingHandler;
import forestry.core.genetics.root.ServerBreedingHandler;
import forestry.factory.recipes.ClientCraftingHelper;

// Rule of thumb for safely calling client code: client classes must be in the body of a static method
// in another class (like this one), guarded by an if statement checking FMLEnvironment.dist == Dist.CLIENT
// Calls to this class must be guarded by an if statement.
// DistExecutor will be deprecated for removal in 1.20, and it doesn't work anyway.
public class ClientsideCode {
	public static ServerBreedingHandler newBreedingHandler() {
		return new ClientBreedingHandler();
	}

	public static RecipeManager craftingHelperAdjust() {
		return ClientCraftingHelper.adjustClient();
	}

	@Nullable
	public static RecipeManager getRecipeManager() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			return connection.getRecipeManager();
		}
		return null;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
	}

	public static void markForUpdate(BlockPos pos) {
		Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}
}
