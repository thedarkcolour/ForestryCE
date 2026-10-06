package forestry.gametest;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.plugin.IApicultureRegistration;
import forestry.apiculture.bees.genetics.BeeSpeciesDefinition;
import forestry.apiculture.bees.genetics.BeeSpeciesProvider;
import forestry.core.platform.util.SpeciesUtil;

@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class BeeSpeciesProviderTest {
	private static final ResourceLocation SPECIES = ForestryConstants.forestry("test_jubilant");
	private static final ResourceLocation JUBILANCE = ForestryConstants.forestry("test_jubilance");

	@GameTest(template = "empty")
	public static void jubilanceRegisteredByActionIsGenerated(GameTestHelper helper) {
		BeeSpeciesDefinition def = build(reg -> {
			reg.registerBeeJubilance(JUBILANCE, (species, genome, housing) -> true);
			reg.registerSpecies(SPECIES, "Testus", "jubilans", true, TextColor.fromRgb(0)).setJubilance(JUBILANCE);
		}).get(SPECIES);

		helper.assertValueEqual(def.jubilance(), JUBILANCE, "jubilance");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void jubilanceRegisteredByPluginIsGenerated(GameTestHelper helper) {
		BeeSpeciesDefinition def = build(reg -> {
			reg.registerSpecies(SPECIES, "Testus", "jubilans", true, TextColor.fromRgb(0)).setJubilance(ForestryBeeJubilances.HERMIT);
		}).get(SPECIES);

		helper.assertValueEqual(def.jubilance(), ForestryBeeJubilances.HERMIT, "jubilance");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void unregisteredJubilanceThrows(GameTestHelper helper) {
		try {
			build(reg -> reg.registerSpecies(SPECIES, "Testus", "jubilans", true, TextColor.fromRgb(0)).setJubilance(JUBILANCE));
		} catch (IllegalStateException expected) {
			helper.succeed();
			return;
		}
		helper.fail("expected an unregistered jubilance to throw");
	}

	private static Map<ResourceLocation, BeeSpeciesDefinition> build(Consumer<IApicultureRegistration> species) {
		return BeeSpeciesProvider.buildDefinitions(SpeciesUtil.BEE_TYPE.get(), species);
	}
}
