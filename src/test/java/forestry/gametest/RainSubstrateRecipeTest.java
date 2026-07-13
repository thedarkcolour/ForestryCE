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
import forestry.api.recipes.IRainSubstrate;
import forestry.core.features.CoreItems;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.RainSubstrateRecipe;

/**
 * Behavioral oracle for "rain substrates as recipes". Proves that the datapack-loaded {@code forestry:rain_substrate}
 * recipes were parsed into the runtime {@link RecipeManager} and are resolvable through the same {@link RecipeUtils}
 * lookup the rainmaker uses, that the classic default values are preserved, that non-substrates resolve to nothing,
 * and that the recipe codec survives NBT and network round-trips.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class RainSubstrateRecipeTest {
	@GameTest(template = "empty")
	public static void substratesLoaded(GameTestHelper helper) {
		RecipeManager manager = helper.getLevel().getRecipeManager();

		// Iodine capsule: starts rain for 10000 ticks at speed 0.01.
		IRainSubstrate iodine = RecipeUtils.getRainSubstrate(manager, CoreItems.IODINE_CHARGE.stack());
		if (iodine == null) {
			helper.fail("Iodine capsule is not a registered rain substrate; recipes were not loaded");
			return;
		}
		if (iodine.duration() != 10000 || iodine.speed() != 0.01f || iodine.reverse()) {
			helper.fail("Iodine substrate defaults changed: duration=" + iodine.duration() + " speed=" + iodine.speed() + " reverse=" + iodine.reverse());
			return;
		}

		// Dissipation charge: stops rain (reverse) at speed 0.075.
		IRainSubstrate dissipation = RecipeUtils.getRainSubstrate(manager, CoreItems.DISSIPATION_CHARGE.stack());
		if (dissipation == null || !dissipation.reverse() || dissipation.speed() != 0.075f) {
			helper.fail("Dissipation substrate missing or defaults changed");
			return;
		}

		// A plain item is not a substrate.
		if (RecipeUtils.getRainSubstrate(manager, new ItemStack(Items.DIRT)) != null) {
			helper.fail("Dirt unexpectedly resolved to a rain substrate");
			return;
		}

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void codecRoundTrip(GameTestHelper helper) {
		RainSubstrateRecipe recipe = new RainSubstrateRecipe(
			ForestryConstants.forestry("rain_substrate/test_roundtrip"),
			Ingredient.of(CoreItems.IODINE_CHARGE),
			1234, 0.5f, true
		);
		RainSubstrateRecipe.Serializer serializer = (RainSubstrateRecipe.Serializer) FactoryRecipeTypes.RAIN_SUBSTRATE.serializer();

		MapCodec<RainSubstrateRecipe> mapCodec = serializer.codec();
		Codec<RainSubstrateRecipe> codec = mapCodec.codec();
		Tag nbt = codec.encodeStart(NbtOps.INSTANCE, recipe).getOrThrow();
		RainSubstrateRecipe fromNbt = codec.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		if (!equal(recipe, fromNbt)) {
			helper.fail("NBT codec round-trip changed the rain substrate recipe");
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, RainSubstrateRecipe> streamCodec = serializer.streamCodec();
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, recipe);
		RainSubstrateRecipe fromBuf = streamCodec.decode(buf);
		if (!equal(recipe, fromBuf)) {
			helper.fail("Stream codec round-trip changed the rain substrate recipe");
			return;
		}

		helper.succeed();
	}

	private static boolean equal(IRainSubstrate a, IRainSubstrate b) {
		return a.getId().equals(b.getId())
				&& a.duration() == b.duration()
				&& a.speed() == b.speed()
				&& a.reverse() == b.reverse()
				&& b.getInput().test(CoreItems.IODINE_CHARGE.stack());
	}
}
