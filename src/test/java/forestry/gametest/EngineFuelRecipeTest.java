package forestry.gametest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

import forestry.api.ForestryConstants;
import forestry.api.recipes.IBiogasFuel;
import forestry.api.recipes.IPeatFuel;
import forestry.core.features.CoreItems;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.ForestryFluids;
import forestry.core.utils.RecipeUtils;
import forestry.energy.features.EnergyRecipeTypes;
import forestry.energy.recipes.BiogasFuelRecipe;
import forestry.energy.recipes.PeatFuelRecipe;

/**
 * Behavioral oracle for "engine fuels as recipes". Proves that the datapack-loaded {@code forestry:biogas_fuel} and
 * {@code forestry:peat_fuel} recipes were parsed into the runtime {@link RecipeManager} and are resolvable through the
 * same {@link RecipeUtils} lookups the engine tiles use, that the classic Forestry default values are preserved exactly,
 * that non-fuels resolve to nothing, that the biogas tank filter is derived from those recipes, and that both recipe
 * codecs survive JSON/NBT and network round-trips.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class EngineFuelRecipeTest {
	/** The biogas (bronze) engine fuels must load with their classic default power/duration/dissipation. */
	@GameTest(template = "empty")
	public static void biogasFuelsLoaded(GameTestHelper helper) {
		RecipeManager manager = helper.getLevel().getRecipeManager();

		IBiogasFuel biomass = RecipeUtils.getBiogasFuel(manager, ForestryFluids.BIOMASS.getFluid());
		if (biomass == null) {
			helper.fail("Biomass is not a registered biogas fuel; recipes were not loaded");
			return;
		}
		if (biomass.getPowerPerCycle() != 50 || biomass.getBurnDuration() != 2500 || biomass.getDissipationMultiplier() != 1) {
			helper.fail("Biomass biogas fuel defaults changed: " + describe(biomass));
			return;
		}

		// Water is the low-grade coolant fuel: distinguished by a 3x heat dissipation multiplier.
		IBiogasFuel water = RecipeUtils.getBiogasFuel(manager, Fluids.WATER);
		if (water == null || water.getDissipationMultiplier() != 3) {
			helper.fail("Water biogas fuel missing or dissipation multiplier changed: " + describe(water));
			return;
		}

		// Lava is not a biogas fuel and must resolve to nothing.
		if (RecipeUtils.getBiogasFuel(manager, Fluids.LAVA) != null) {
			helper.fail("Lava unexpectedly resolved to a biogas fuel");
			return;
		}

		helper.succeed();
	}

	/** The peat-fired (copper) engine fuels must load with their classic default power/duration. */
	@GameTest(template = "empty")
	public static void peatFuelsLoaded(GameTestHelper helper) {
		RecipeManager manager = helper.getLevel().getRecipeManager();

		IPeatFuel peat = RecipeUtils.getPeatFuel(manager, CoreItems.PEAT.stack());
		if (peat == null || peat.getPowerPerCycle() != 20 || peat.getBurnDuration() != 2500) {
			helper.fail("Peat fuel missing or defaults changed: " + describe(peat));
			return;
		}

		IPeatFuel bituminous = RecipeUtils.getPeatFuel(manager, CoreItems.BITUMINOUS_PEAT.stack());
		if (bituminous == null || bituminous.getPowerPerCycle() != 40 || bituminous.getBurnDuration() != 3000) {
			helper.fail("Bituminous peat fuel missing or defaults changed: " + describe(bituminous));
			return;
		}

		// A plain item is not a peat fuel.
		if (RecipeUtils.getPeatFuel(manager, new ItemStack(Items.DIRT)) != null) {
			helper.fail("Dirt unexpectedly resolved to a peat fuel");
			return;
		}

		helper.succeed();
	}

	/** The biogas engine's tank filter must be derived from the biogas fuel recipes (accepts fuels, rejects non-fuels). */
	@GameTest(template = "empty")
	public static void biogasTankFilterMatchesRecipes(GameTestHelper helper) {
		Set<ResourceLocation> accepted = FluidRecipeFilter.BIOGAS_FUEL.get();

		if (!accepted.contains(fluidId(ForestryFluids.BIOMASS.getFluid())) || !accepted.contains(fluidId(Fluids.WATER))) {
			helper.fail("Biogas tank filter is missing a known fuel fluid: " + accepted);
			return;
		}
		if (accepted.contains(fluidId(Fluids.LAVA))) {
			helper.fail("Biogas tank filter unexpectedly accepts lava");
			return;
		}

		helper.succeed();
	}

	/** A built biogas fuel recipe must survive the serializer's NBT codec and network stream-codec unchanged. */
	@GameTest(template = "empty")
	public static void biogasFuelCodecRoundTrip(GameTestHelper helper) {
		BiogasFuelRecipe recipe = new BiogasFuelRecipe(
			ForestryConstants.forestry("biogas_fuel/test_roundtrip"),
			FluidIngredient.of(ForestryFluids.BIOMASS.getFluid()),
			123, 456, 3
		);
		BiogasFuelRecipe.Serializer serializer = (BiogasFuelRecipe.Serializer) EnergyRecipeTypes.BIOGAS_FUEL.serializer();

		MapCodec<BiogasFuelRecipe> mapCodec = serializer.codec();
		Codec<BiogasFuelRecipe> codec = mapCodec.codec();
		Tag nbt = codec.encodeStart(NbtOps.INSTANCE, recipe).getOrThrow();
		BiogasFuelRecipe fromNbt = codec.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		if (!biogasEqual(recipe, fromNbt)) {
			helper.fail("NBT codec round-trip changed the biogas fuel recipe");
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, BiogasFuelRecipe> streamCodec = serializer.streamCodec();
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, recipe);
		BiogasFuelRecipe fromBuf = streamCodec.decode(buf);
		if (!biogasEqual(recipe, fromBuf)) {
			helper.fail("Stream codec round-trip changed the biogas fuel recipe");
			return;
		}

		helper.succeed();
	}

	/** A built peat fuel recipe must survive the serializer's NBT codec and network stream-codec unchanged. */
	@GameTest(template = "empty")
	public static void peatFuelCodecRoundTrip(GameTestHelper helper) {
		PeatFuelRecipe recipe = new PeatFuelRecipe(
			ForestryConstants.forestry("peat_fuel/test_roundtrip"),
			Ingredient.of(CoreItems.PEAT),
			123, 456
		);
		PeatFuelRecipe.Serializer serializer = (PeatFuelRecipe.Serializer) EnergyRecipeTypes.PEAT_FUEL.serializer();

		MapCodec<PeatFuelRecipe> mapCodec = serializer.codec();
		Codec<PeatFuelRecipe> codec = mapCodec.codec();
		Tag nbt = codec.encodeStart(NbtOps.INSTANCE, recipe).getOrThrow();
		PeatFuelRecipe fromNbt = codec.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		if (!peatEqual(recipe, fromNbt)) {
			helper.fail("NBT codec round-trip changed the peat fuel recipe");
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, PeatFuelRecipe> streamCodec = serializer.streamCodec();
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, recipe);
		PeatFuelRecipe fromBuf = streamCodec.decode(buf);
		if (!peatEqual(recipe, fromBuf)) {
			helper.fail("Stream codec round-trip changed the peat fuel recipe");
			return;
		}

		helper.succeed();
	}

	private static ResourceLocation fluidId(Fluid fluid) {
		return BuiltInRegistries.FLUID.getKey(fluid);
	}

	private static boolean biogasEqual(IBiogasFuel a, IBiogasFuel b) {
		return a.getId().equals(b.getId())
				&& a.getPowerPerCycle() == b.getPowerPerCycle()
				&& a.getBurnDuration() == b.getBurnDuration()
				&& a.getDissipationMultiplier() == b.getDissipationMultiplier()
				&& b.getFluidInput().test(new FluidStack(ForestryFluids.BIOMASS.getFluid(), 1));
	}

	private static boolean peatEqual(IPeatFuel a, IPeatFuel b) {
		return a.getId().equals(b.getId())
				&& a.getPowerPerCycle() == b.getPowerPerCycle()
				&& a.getBurnDuration() == b.getBurnDuration()
				&& b.getInput().test(CoreItems.PEAT.stack());
	}

	private static String describe(IBiogasFuel fuel) {
		return fuel == null ? "null" : ("power=" + fuel.getPowerPerCycle() + " duration=" + fuel.getBurnDuration() + " dissipation=" + fuel.getDissipationMultiplier());
	}

	private static String describe(IPeatFuel fuel) {
		return fuel == null ? "null" : ("power=" + fuel.getPowerPerCycle() + " duration=" + fuel.getBurnDuration());
	}
}
