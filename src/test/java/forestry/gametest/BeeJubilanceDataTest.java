package forestry.gametest;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.api.ForestryRegistries;
import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.genetics.ForestryTaxa;
import forestry.apiculture.bees.genetics.ApicultureReloadHandler;
import forestry.apiculture.bees.genetics.BeeJubilanceManager;
import forestry.apiculture.bees.genetics.BeeSpeciesProjector;
import forestry.apiculture.bees.genetics.HermitBeeJubilance;
import forestry.core.platform.util.SpeciesUtil;

@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class BeeJubilanceDataTest {
	private static final ResourceLocation TEST_ID = ForestryConstants.forestry("test_requires_gold");
	private static final String JSON = "{\"type\":\"forestry:requires_resource\",\"blocks\":\"minecraft:gold_block\"}";

	private static IBeeJubilance parse(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		return IBeeJubilance.CODEC.parse(ops, JsonParser.parseString(JSON)).getOrThrow();
	}

	@GameTest(template = "empty")
	public static void requiresResourceTypeIsRegistered(GameTestHelper helper) {
		if (!ForestryRegistries.BEE_JUBILANCE_TYPE.containsKey(ForestryConstants.forestry("requires_resource"))) {
			helper.fail("forestry:requires_resource is not a registered jubilance type");
			return;
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void jubilanceCodecRoundTrip(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		IBeeJubilance parsed = parse(helper);

		JsonElement encoded = IBeeJubilance.CODEC.encodeStart(ops, parsed).getOrThrow();
		if (!encoded.equals(JsonParser.parseString(JSON))) {
			helper.fail("JSON round trip changed the jubilance: " + encoded);
			return;
		}

		// the same stream codec BeeJubilanceSyncPacket uses
		StreamCodec<RegistryFriendlyByteBuf, IBeeJubilance> streamCodec = ByteBufCodecs.fromCodecWithRegistries(IBeeJubilance.CODEC);
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, parsed);
		JsonElement synced = IBeeJubilance.CODEC.encodeStart(ops, streamCodec.decode(buf)).getOrThrow();
		if (!synced.equals(encoded)) {
			helper.fail("stream round trip changed the jubilance: " + synced);
			return;
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void requiresResourceChecksBlockBelowHousing(GameTestHelper helper) {
		IBeeJubilance jubilance = parse(helper);
		IBeeSpecies species = SpeciesUtil.BEE_TYPE.get().getDefaultSpecies();
		BlockPos below = new BlockPos(0, 1, 0);
		BlockPos housingPos = helper.absolutePos(below.above());
		IBeeHousing housing = (IBeeHousing) Proxy.newProxyInstance(IBeeHousing.class.getClassLoader(), new Class<?>[]{IBeeHousing.class}, (proxy, method, args) -> switch (method.getName()) {
			case "getLevel" -> helper.getLevel();
			case "getBlockPos" -> housingPos;
			default -> throw new UnsupportedOperationException(method.getName());
		});

		helper.setBlock(below, Blocks.GOLD_BLOCK);
		if (!jubilance.isJubilant(species, species.getDefaultGenome(), housing)) {
			helper.fail("expected jubilant above a gold block");
			return;
		}
		helper.setBlock(below, Blocks.STONE);
		if (jubilance.isJubilant(species, species.getDefaultGenome(), housing)) {
			helper.fail("expected not jubilant above stone");
			return;
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void datapackJubilanceMergesOntoCodeJubilances(GameTestHelper helper) {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		Map<ResourceLocation, IBeeJubilance> original = BeeJubilanceManager.INSTANCE.getDefinitions();
		try {
			ApicultureReloadHandler.rebuildJubilances(Map.of(TEST_ID, parse(helper)));
			if (type.getJubilanceSafe(TEST_ID) == null) {
				helper.fail("datapack jubilance did not resolve");
				return;
			}
			if (type.getJubilance(ForestryBeeJubilances.HERMIT) != HermitBeeJubilance.INSTANCE) {
				helper.fail("code-registered jubilance was lost by the merge");
				return;
			}
			if (BeeSpeciesProjector.project(type, ForestryConstants.forestry("test_gold_lover"), TestSpeciesDefinitions.bee(ForestryTaxa.GENUS_HONEY, ForestryTaxa.SPECIES_FOREST).jubilance(TEST_ID).build()) == null) {
				helper.fail("a species using the datapack jubilance was skipped");
				return;
			}

			ApicultureReloadHandler.rebuildJubilances(Map.of());
			if (type.getJubilanceSafe(TEST_ID) != null) {
				helper.fail("datapack jubilance survived a reload that no longer defines it");
				return;
			}
		} finally {
			ApicultureReloadHandler.rebuildJubilances(original);
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void jubilanceProviderGenerates(GameTestHelper helper) throws IOException {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		Path dir = Files.createTempDirectory("forestry_jubilance_test");
		try {
			type.createJubilanceProvider(new PackOutput(dir), CompletableFuture.completedFuture(helper.getLevel().registryAccess()), Map.of(TEST_ID, parse(helper)))
				.run(CachedOutput.NO_CACHE).join();

			Path file = dir.resolve("data/forestry/bee_jubilance/test_requires_gold.json");
			if (!Files.exists(file) || !JsonParser.parseString(Files.readString(file)).equals(JsonParser.parseString(JSON))) {
				helper.fail("jubilance provider did not generate the expected file at " + file);
				return;
			}
		} finally {
			Files.deleteIfExists(dir.resolve("data/forestry/bee_jubilance/test_requires_gold.json"));
		}
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void jubilanceFactoryEncodesTag(GameTestHelper helper) {
		IBeeJubilance jubilance = SpeciesUtil.BEE_TYPE.get().getJubilanceFactory().getRequiresResource(BlockTags.DIRT);
		JsonElement json = IBeeJubilance.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess()), jubilance).getOrThrow();
		JsonElement expected = JsonParser.parseString("{\"type\":\"forestry:requires_resource\",\"blocks\":\"#minecraft:dirt\"}");

		if (!json.equals(expected)) {
			helper.fail("tag jubilance encoded as " + json);
			return;
		}
		helper.succeed();
	}
}
