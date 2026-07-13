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
import forestry.api.recipes.IFermenterFuel;
import forestry.core.features.CoreItems;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.FermenterFuelRecipe;

/**
 * Behavioral oracle for "fermenter fuels as recipes". Proves that the datapack-loaded {@code forestry:fermenter_fuel}
 * recipes were parsed into the runtime {@link RecipeManager} and are resolvable through the same {@link RecipeUtils}
 * lookup the fermenter uses, that the classic default values are preserved, that non-fuels resolve to nothing, and
 * that the recipe codec survives NBT and network round-trips.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class FermenterFuelRecipeTest {
	@GameTest(template = "empty")
	public static void fuelsLoaded(GameTestHelper helper) {
		RecipeManager manager = helper.getLevel().getRecipeManager();

		IFermenterFuel fertilizer = RecipeUtils.getFermenterFuel(manager, CoreItems.FERTILIZER_COMPOUND.stack());
		if (fertilizer == null) {
			helper.fail("Fertilizer is not a registered fermenter fuel; recipes were not loaded");
			return;
		}
		if (fertilizer.fermentPerCycle() != 56 || fertilizer.burnDuration() != 200) {
			helper.fail("Fertilizer fuel defaults changed: ferment=" + fertilizer.fermentPerCycle() + " duration=" + fertilizer.burnDuration());
			return;
		}

		IFermenterFuel compost = RecipeUtils.getFermenterFuel(manager, CoreItems.COMPOST.stack());
		if (compost == null || compost.fermentPerCycle() != 48 || compost.burnDuration() != 250) {
			helper.fail("Compost fuel missing or defaults changed");
			return;
		}

		IFermenterFuel mulch = RecipeUtils.getFermenterFuel(manager, CoreItems.MULCH.stack());
		if (mulch == null || mulch.fermentPerCycle() != 48 || mulch.burnDuration() != 250) {
			helper.fail("Mulch fuel missing or defaults changed");
			return;
		}

		if (RecipeUtils.getFermenterFuel(manager, new ItemStack(Items.DIRT)) != null) {
			helper.fail("Dirt unexpectedly resolved to a fermenter fuel");
			return;
		}

		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void codecRoundTrip(GameTestHelper helper) {
		FermenterFuelRecipe recipe = new FermenterFuelRecipe(
			ForestryConstants.forestry("fermenter_fuel/test_roundtrip"),
			Ingredient.of(CoreItems.COMPOST),
			123, 456
		);
		FermenterFuelRecipe.Serializer serializer = (FermenterFuelRecipe.Serializer) FactoryRecipeTypes.FERMENTER_FUEL.serializer();

		MapCodec<FermenterFuelRecipe> mapCodec = serializer.codec();
		Codec<FermenterFuelRecipe> codec = mapCodec.codec();
		Tag nbt = codec.encodeStart(NbtOps.INSTANCE, recipe).getOrThrow();
		FermenterFuelRecipe fromNbt = codec.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		if (!equal(recipe, fromNbt)) {
			helper.fail("NBT codec round-trip changed the fermenter fuel recipe");
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, FermenterFuelRecipe> streamCodec = serializer.streamCodec();
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, recipe);
		FermenterFuelRecipe fromBuf = streamCodec.decode(buf);
		if (!equal(recipe, fromBuf)) {
			helper.fail("Stream codec round-trip changed the fermenter fuel recipe");
			return;
		}

		helper.succeed();
	}

	private static boolean equal(IFermenterFuel a, IFermenterFuel b) {
		return a.getId().equals(b.getId())
				&& a.fermentPerCycle() == b.fermentPerCycle()
				&& a.burnDuration() == b.burnDuration()
				&& b.getInput().test(CoreItems.COMPOST.stack());
	}
}
