package forestry.gametest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.api.recipes.IMoistenerFuel;
import forestry.core.features.CoreItems;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.MoistenerFuelRecipe;

/**
 * Behavioral oracle for "moistener resources as recipes". Proves that the datapack-loaded {@code forestry:moistener_fuel}
 * recipes were parsed into the runtime {@link RecipeManager} and are resolvable through the same {@link RecipeUtils}
 * lookup the moistener uses, that the classic wheat -> mouldy -> decaying -> mulch decay chain (stage ordering,
 * moistener values, and decay products) is preserved, that non-resources resolve to nothing, and that the recipe
 * codec survives NBT and network round-trips.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MoistenerFuelRecipeTest {
	@GameTest(template = "empty")
	public static void resourcesLoaded(GameTestHelper helper) {
		RecipeManager manager = helper.getLevel().getRecipeManager();

		// Wheat (stage 0) decays into mouldy wheat over 300 moistener ticks.
		IMoistenerFuel wheat = RecipeUtils.getMoistenerFuel(manager, new ItemStack(Items.WHEAT));
		if (wheat == null) {
			helper.fail("Wheat is not a registered moistener resource; recipes were not loaded");
			return;
		}
		if (wheat.stage() != 0 || wheat.moistenerValue() != 300 || !ItemStack.isSameItem(wheat.product(), CoreItems.MOULDY_WHEAT.stack())) {
			helper.fail("Wheat resource defaults changed: stage=" + wheat.stage() + " value=" + wheat.moistenerValue() + " product=" + wheat.product());
			return;
		}

		// Mouldy wheat (stage 1) -> decaying wheat over 600 ticks.
		IMoistenerFuel mouldy = RecipeUtils.getMoistenerFuel(manager, CoreItems.MOULDY_WHEAT.stack());
		if (mouldy == null || mouldy.stage() != 1 || mouldy.moistenerValue() != 600 || !ItemStack.isSameItem(mouldy.product(), CoreItems.DECAYING_WHEAT.stack())) {
			helper.fail("Mouldy wheat resource missing or defaults changed");
			return;
		}

		// Decaying wheat (stage 2) -> mulch over 900 ticks.
		IMoistenerFuel decaying = RecipeUtils.getMoistenerFuel(manager, CoreItems.DECAYING_WHEAT.stack());
		if (decaying == null || decaying.stage() != 2 || decaying.moistenerValue() != 900 || !ItemStack.isSameItem(decaying.product(), CoreItems.MULCH.stack())) {
			helper.fail("Decaying wheat resource missing or defaults changed");
			return;
		}

		if (RecipeUtils.getMoistenerFuel(manager, new ItemStack(Items.DIRT)) != null) {
			helper.fail("Dirt unexpectedly resolved to a moistener resource");
			return;
		}

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void codecRoundTrip(GameTestHelper helper) {
		MoistenerFuelRecipe recipe = new MoistenerFuelRecipe(
			ForestryConstants.forestry("moistener_fuel/test_roundtrip"),
			Ingredient.of(Items.WHEAT),
			CoreItems.MOULDY_WHEAT.stack(),
			1, 234
		);
		MoistenerFuelRecipe.Serializer serializer = (MoistenerFuelRecipe.Serializer) FactoryRecipeTypes.MOISTENER_FUEL.serializer();

		MapCodec<MoistenerFuelRecipe> mapCodec = serializer.codec();
		Codec<MoistenerFuelRecipe> codec = mapCodec.codec();
		Tag nbt = codec.encodeStart(NbtOps.INSTANCE, recipe).getOrThrow();
		MoistenerFuelRecipe fromNbt = codec.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		if (!equal(recipe, fromNbt)) {
			helper.fail("NBT codec round-trip changed the moistener fuel recipe");
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, MoistenerFuelRecipe> streamCodec = serializer.streamCodec();
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, recipe);
		MoistenerFuelRecipe fromBuf = streamCodec.decode(buf);
		if (!equal(recipe, fromBuf)) {
			helper.fail("Stream codec round-trip changed the moistener fuel recipe");
			return;
		}

		helper.succeed();
	}

	private static boolean equal(IMoistenerFuel a, IMoistenerFuel b) {
		return a.getId().equals(b.getId())
				&& a.stage() == b.stage()
				&& a.moistenerValue() == b.moistenerValue()
				&& ItemStack.isSameItem(a.product(), b.product())
				&& b.getInput().test(new ItemStack(Items.WHEAT));
	}
}
