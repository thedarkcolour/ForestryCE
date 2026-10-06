package forestry.gametest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import io.netty.buffer.Unpooled;

import forestry.api.ForestryConstants;
import forestry.api.ForestryRegistries;
import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.TemperatureType;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.apiculture.bees.genetics.ApicultureReloadHandler;
import forestry.apiculture.bees.genetics.BeeEffectManager;
import forestry.apiculture.bees.genetics.effects.AgingBeeEffect;
import forestry.apiculture.bees.genetics.effects.DamageBeeEffect;
import forestry.apiculture.bees.genetics.effects.PotionBeeEffect;
import forestry.apiculture.bees.genetics.effects.ResurrectionBeeEffect;
import forestry.apiculture.bees.genetics.effects.ThrottleSettings;
import forestry.apiculture.bees.genetics.effects.TransformBlockBeeEffect;
import forestry.core.platform.damage.CoreDamageTypes;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.platform.util.SpeciesUtil;

/**
 * Behavioral oracle for the data-driven bee effect system ported onto the upstream foundation (migration Module 2).
 * Proves the three invariants of the port: the 5 parameterized effect primitives are registered as serializer types;
 * {@link IBeeEffect#CODEC} round-trips a datapack effect definition through both JSON and the network stream codec used
 * by {@code BeeEffectSyncPacket}; and {@code ApicultureReloadHandler.rebuildBeeEffects} merges datapack effects onto the
 * code-registered builtins reloadably (datapack entries resolve, builtins survive, and an empty reload keeps builtins).
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class BeeEffectSystemTest {
	/** The effect a species' default genome resolves to, via the reference {@code bee_effect} chromosome. */
	private static IBeeEffect effectOf(ResourceLocation speciesId) {
		return SpeciesUtil.BEE_TYPE.get().getSpecies(speciesId).getDefaultGenome().resolveActive(BeeChromosomes.EFFECT);
	}

	/**
	 * Exactly the five primitives a built-in effect consumes are registered, and no more. A primitive with no built-in
	 * consumer is addon material: it is speculative surface area that base Forestry pays for in maintenance and that
	 * no shipped content proves works.
	 */
	@GameTest(template = "empty")
	public static void allEffectPrimitiveTypesRegistered(GameTestHelper helper) {
		String[] types = {"apply_potion", "damage_entities", "transform_block", "resurrect", "aging"};
		for (String type : types) {
			ResourceLocation id = ForestryConstants.forestry(type);
			if (!ForestryRegistries.BEE_EFFECT_TYPE.containsKey(id)) {
				helper.fail("bee effect primitive type not registered: " + id);
				return;
			}
		}
		if (ForestryRegistries.BEE_EFFECT_TYPE.size() != types.length) {
			helper.fail("expected exactly " + types.length + " bee effect primitive types, found "
				+ ForestryRegistries.BEE_EFFECT_TYPE.size() + ": " + ForestryRegistries.BEE_EFFECT_TYPE.keySet());
			return;
		}
		helper.succeed();
	}

	/**
	 * A datapack effect definition round-trips through {@link IBeeEffect#CODEC} (JSON) preserving its type and
	 * parameters, and through the network stream codec the sync packet uses.
	 */
	@GameTest(template = "empty")
	public static void effectDefinitionRoundTrips(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());

		TransformBlockBeeEffect original = new TransformBlockBeeEffect(
			new ThrottleSettings(false, 30, true, false),
			List.of(new TransformBlockBeeEffect.Transform(
				new TransformBlockBeeEffect.BlockMatcher.Tag(BlockTags.DIRT),
				new TransformBlockBeeEffect.To.Fixed(Blocks.COARSE_DIRT.defaultBlockState()),
				true)),
			10, 0.34f, Optional.of(TemperatureType.NORMAL));

		JsonElement json = IBeeEffect.CODEC.encodeStart(ops, original).getOrThrow();
		if (!json.getAsJsonObject().get("max_temperature").getAsString().equals("normal")) {
			helper.fail("transform_block did not encode max_temperature as a lowercase name: " + json);
			return;
		}
		IBeeEffect fromJson = IBeeEffect.CODEC.parse(ops, json).getOrThrow();
		if (!(fromJson instanceof TransformBlockBeeEffect transform)) {
			helper.fail("transform_block did not decode to TransformBlockBeeEffect (got " + fromJson.getClass().getSimpleName() + ")");
			return;
		}
		if (transform.getThrottle() != 30 || transform.isDominant() || transform.attempts() != 10
			|| !transform.maxTemperature().equals(Optional.of(TemperatureType.NORMAL))) {
			helper.fail("transform_block parameters not preserved through JSON: " + json);
			return;
		}
		TransformBlockBeeEffect.Transform decodedRule = transform.transforms().getFirst();
		if (!decodedRule.requiresAirAbove()
			|| !decodedRule.from().matches(Blocks.DIRT.defaultBlockState())
			|| !(decodedRule.to() instanceof TransformBlockBeeEffect.To.Fixed fixed)
			|| !fixed.state().is(Blocks.COARSE_DIRT)) {
			helper.fail("transform_block transform rule not preserved through JSON: " + json);
			return;
		}

		StreamCodec<RegistryFriendlyByteBuf, IBeeEffect> streamCodec = ByteBufCodecs.fromCodecWithRegistries(IBeeEffect.CODEC);
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		streamCodec.encode(buf, original);
		IBeeEffect fromBuf = streamCodec.decode(buf);
		if (!(fromBuf instanceof TransformBlockBeeEffect decoded) || decoded.getThrottle() != 30 || decoded.attempts() != 10) {
			helper.fail("transform_block did not survive the network stream codec round-trip");
			return;
		}

		helper.succeed();
	}

	/**
	 * The identity guard's decision function: {@code To.apply} returns the state it was given when the transform is a
	 * no-op, which is exactly what {@code doEffectThrottled} branches on to skip the write. This subsumes SIFTER's
	 * coarse-dirt exclusion (coarse dirt is in {@code #minecraft:dirt}, so the tag matches and only the identity check
	 * stops the rewrite) and GLOW_BERRY_GROW's already-berried check.
	 */
	@GameTest(template = "empty")
	public static void transformIdentityGuardSkipsNoOps(GameTestHelper helper) {
		BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
		if (!coarseDirt.is(BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.DIRT))) {
			helper.fail("#minecraft:dirt no longer contains coarse_dirt; SIFTER's identity guard assumption is void");
			return;
		}
		if (new TransformBlockBeeEffect.To.Fixed(coarseDirt).apply(coarseDirt) != coarseDirt) {
			helper.fail("Fixed.apply did not return the identical state for a coarse dirt no-op");
			return;
		}

		BlockState berried = Blocks.CAVE_VINES.defaultBlockState().setValue(BlockStateProperties.BERRIES, true);
		TransformBlockBeeEffect.To setBerries = new TransformBlockBeeEffect.To.SetProperties(Map.of("berries", "true"));
		if (setBerries.apply(berried) != berried) {
			helper.fail("SetProperties.apply did not return the identical state for an already-berried vine");
			return;
		}
		// ...and it is a real mutation on an unberried vine, preserving the vine's other properties.
		BlockState bare = Blocks.CAVE_VINES.defaultBlockState().setValue(BlockStateProperties.AGE_25, 7);
		BlockState grown = setBerries.apply(bare);
		if (grown == bare || !grown.getValue(BlockStateProperties.BERRIES) || grown.getValue(BlockStateProperties.AGE_25) != 7) {
			helper.fail("SetProperties.apply did not set berries while preserving the vine's other properties");
			return;
		}

		helper.succeed();
	}

	/**
	 * {@code rebuildBeeEffects} merges datapack effects onto the code builtins: the datapack entry resolves, a builtin
	 * still resolves, and after an empty reload the builtin survives (self-resetting). Restores the live effect map
	 * afterwards so the mutation does not leak into other tests.
	 */
	@GameTest(template = "empty")
	public static void datapackEffectsMergeOntoBuiltins(GameTestHelper helper) {
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		Map<ResourceLocation, IBeeEffect> original = BeeEffectManager.INSTANCE.getDefinitions();
		ResourceLocation testId = ForestryConstants.forestry("gametest_transform");
		IBeeEffect testEffect = new TransformBlockBeeEffect(
			new ThrottleSettings(true, 30, false, false),
			List.of(new TransformBlockBeeEffect.Transform(
				new TransformBlockBeeEffect.BlockMatcher.Tag(BlockTags.DIRT),
				new TransformBlockBeeEffect.To.Fixed(Blocks.COARSE_DIRT.defaultBlockState()),
				false)),
			1, 0.34f, Optional.empty());
		try {
			ApicultureReloadHandler.rebuildBeeEffects(Map.of(testId, testEffect));

			if (!(beeType.getBeeEffect(testId) instanceof TransformBlockBeeEffect)) {
				helper.fail("datapack effect " + testId + " did not resolve after rebuildBeeEffects");
				return;
			}
			// A code builtin (registered by DefaultForestryPlugin) must survive the merge.
			if (beeType.getBeeEffect(ForestryBeeEffects.NONE) == null) {
				helper.fail("builtin bee effect NONE was dropped by the datapack merge");
				return;
			}

			// Empty reload: builtins remain (self-resetting when a datapack is removed).
			ApicultureReloadHandler.rebuildBeeEffects(Map.of());
			if (beeType.getBeeEffect(ForestryBeeEffects.NONE) == null) {
				helper.fail("builtin bee effect NONE was dropped after an empty reload");
				return;
			}
		} finally {
			ApicultureReloadHandler.rebuildBeeEffects(original);
		}

		helper.succeed();
	}

	/**
	 * The plain-potion built-in effects are no longer code-registered: they are defined in the mod's own datapack
	 * (generated by {@code BeeEffectProvider}) and must resolve to a {@link PotionBeeEffect} through the same merged
	 * effect map any datapack effect uses. Proves base content flows through the data-driven pipe, not just new
	 * primitives.
	 */
	@GameTest(template = "empty")
	public static void potionBuiltinsAreDatapackDefined(GameTestHelper helper) {
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		for (ResourceLocation id : new ResourceLocation[]{
			ForestryBeeEffects.BEATIFIC, ForestryBeeEffects.MIASMIC, ForestryBeeEffects.DRUNKARD, ForestryBeeEffects.DARKNESS
		}) {
			if (!(beeType.getBeeEffect(id) instanceof PotionBeeEffect)) {
				helper.fail("datapack-defined potion built-in " + id + " did not resolve to a PotionBeeEffect");
				return;
			}
		}
		// ...and the bees that carry them resolve their genome's effect to that datapack instance: IMPERIAL/BEATIFIC,
		// AQUATIC/MIASMIC, ABYSSAL/DARKNESS, TIPSY/DRUNKARD.
		for (ResourceLocation speciesId : new ResourceLocation[]{
			ForestryBeeSpecies.IMPERIAL, ForestryBeeSpecies.AQUATIC, ForestryBeeSpecies.ABYSSAL, ForestryBeeSpecies.TIPSY
		}) {
			if (!(effectOf(speciesId) instanceof PotionBeeEffect)) {
				helper.fail("bee " + speciesId + " did not resolve its genome effect to the datapack PotionBeeEffect");
				return;
			}
		}
		helper.succeed();
	}

	/**
	 * REANIMATION and RESURRECTION are generalized into one {@code forestry:resurrect} primitive parameterized by an
	 * item&rarr;mob table: the type is registered, both built-ins are datapack-defined and resolve to a
	 * {@link ResurrectionBeeEffect}, and a definition round-trips through {@link IBeeEffect#CODEC} preserving its
	 * entries.
	 */
	@GameTest(template = "empty")
	public static void resurrectBuiltinsAreDatapackDefined(GameTestHelper helper) {
		if (!ForestryRegistries.BEE_EFFECT_TYPE.containsKey(ForestryConstants.forestry("resurrect"))) {
			helper.fail("resurrect effect type not registered: forestry:resurrect");
			return;
		}
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		for (ResourceLocation id : new ResourceLocation[]{ForestryBeeEffects.REANIMATION, ForestryBeeEffects.RESURRECTION}) {
			if (!(beeType.getBeeEffect(id) instanceof ResurrectionBeeEffect)) {
				helper.fail("datapack-defined built-in " + id + " did not resolve to a ResurrectionBeeEffect");
				return;
			}
		}
		// The Spectral (REANIMATION) and Phantasmal (RESURRECTION) bees carry these effects; their genomes must
		// resolve to the datapack instance.
		if (!(effectOf(ForestryBeeSpecies.SPECTRAL) instanceof ResurrectionBeeEffect)
			|| !(effectOf(ForestryBeeSpecies.PHANTASMAL) instanceof ResurrectionBeeEffect)) {
			helper.fail("Spectral/Phantasmal bees did not resolve their genome effect to the datapack ResurrectionBeeEffect");
			return;
		}

		// The item->mob table survives a JSON round trip through the dispatch codec.
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		ResurrectionBeeEffect original = new ResurrectionBeeEffect(true, 40, ResurrectionBeeEffect.getReanimationList());
		JsonElement json = IBeeEffect.CODEC.encodeStart(ops, original).getOrThrow();
		if (!(IBeeEffect.CODEC.parse(ops, json).getOrThrow() instanceof ResurrectionBeeEffect)) {
			helper.fail("resurrect definition did not round-trip through IBeeEffect.CODEC");
			return;
		}

		helper.succeed();
	}

	/**
	 * REJUVENATION and CHRONOPHAGE are generalized into one {@code forestry:aging} primitive differing only by the
	 * {@code aging} flag: the type is registered, both built-ins resolve to an {@link AgingBeeEffect}, and the bees
	 * that carry them (CHRONOFUGE/REJUVENATION, ANACHRONE/CHRONOPHAGE) resolve their genome effect to the datapack instance.
	 */
	@GameTest(template = "empty")
	public static void agingBuiltinsAreDatapackDefined(GameTestHelper helper) {
		if (!ForestryRegistries.BEE_EFFECT_TYPE.containsKey(ForestryConstants.forestry("aging"))) {
			helper.fail("aging effect type not registered: forestry:aging");
			return;
		}
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		for (ResourceLocation id : new ResourceLocation[]{ForestryBeeEffects.REJUVENATION, ForestryBeeEffects.CHRONOPHAGE}) {
			if (!(beeType.getBeeEffect(id) instanceof AgingBeeEffect)) {
				helper.fail("datapack-defined built-in " + id + " did not resolve to an AgingBeeEffect");
				return;
			}
		}
		if (!(effectOf(ForestryBeeSpecies.CHRONOFUGE) instanceof AgingBeeEffect)
			|| !(effectOf(ForestryBeeSpecies.ANACHRONE) instanceof AgingBeeEffect)) {
			helper.fail("Chronofuge/Anachrone bees did not resolve their genome effect to the datapack AgingBeeEffect");
			return;
		}

		// The tunable strength multiplier survives the dispatch codec (built-ins omit it; a pack can set it).
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		JsonElement json = IBeeEffect.CODEC.encodeStart(ops, new AgingBeeEffect(false, true, 3.0f)).getOrThrow();
		if (json.getAsJsonObject().get("strength").getAsFloat() != 3.0f
			|| !(IBeeEffect.CODEC.parse(ops, json).getOrThrow() instanceof AgingBeeEffect)) {
			helper.fail("aging strength did not round-trip through IBeeEffect.CODEC");
			return;
		}

		helper.succeed();
	}

	/**
	 * AGGRESSIVE, MISANTHROPE and HEROIC are generalized into the {@code forestry:damage_entities} primitive,
	 * differing only by damage, armor scaling, damage type and target filter. The type is registered, all three
	 * built-ins are datapack-defined, and the bees that carry them (SINISTER/AGGRESSIVE, ENDED/MISANTHROPE,
	 * HEROIC/HEROIC) resolve their genome effect to the datapack instance.
	 */
	@GameTest(template = "empty")
	public static void damageEntitiesBuiltinsAreDatapackDefined(GameTestHelper helper) {
		if (!ForestryRegistries.BEE_EFFECT_TYPE.containsKey(ForestryConstants.forestry("damage_entities"))) {
			helper.fail("damage_entities effect type not registered: forestry:damage_entities");
			return;
		}
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		for (ResourceLocation id : new ResourceLocation[]{
			ForestryBeeEffects.AGGRESSIVE, ForestryBeeEffects.MISANTHROPE, ForestryBeeEffects.HEROIC
		}) {
			if (!(beeType.getBeeEffect(id) instanceof DamageBeeEffect)) {
				helper.fail("datapack-defined built-in " + id + " did not resolve to a DamageBeeEffect");
				return;
			}
		}
		// All three are non-combinable; the code default for damage_entities is combinable, so the migrated built-ins
		// must keep that flag off (the JSON sets "combinable": false).
		for (ResourceLocation id : new ResourceLocation[]{
			ForestryBeeEffects.AGGRESSIVE, ForestryBeeEffects.MISANTHROPE, ForestryBeeEffects.HEROIC
		}) {
			if (beeType.getBeeEffect(id).isCombinable()) {
				helper.fail(id + " must resolve as non-combinable");
				return;
			}
		}
		// HEROIC is the migration that needed requires_working exposed: it only fires for a working queen.
		if (!(beeType.getBeeEffect(ForestryBeeEffects.HEROIC) instanceof DamageBeeEffect heroic)
			|| !heroic.settings().requiresWorking() || heroic.settings().dominant() || heroic.getThrottle() != 40) {
			helper.fail("HEROIC did not preserve its throttle settings through the datapack migration");
			return;
		}
		if (!(effectOf(ForestryBeeSpecies.SINISTER) instanceof DamageBeeEffect)
			|| !(effectOf(ForestryBeeSpecies.ENDED) instanceof DamageBeeEffect)
			|| !(effectOf(ForestryBeeSpecies.HEROIC) instanceof DamageBeeEffect)) {
			helper.fail("Sinister/Ended/Heroic bees did not resolve their genome effect to the datapack DamageBeeEffect");
			return;
		}

		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		// The rest of HEROIC's migrated values, pinned against the runtime-resolved datapack instance rather than a
		// synthetic one, so a wrong BeeEffectProvider entry fails here instead of only in the generated JSON.
		JsonObject heroicJson = IBeeEffect.CODEC.encodeStart(ops, heroic).getOrThrow().getAsJsonObject();
		if (heroicJson.get("damage").getAsFloat() != 2.0f
			|| heroicJson.get("armor_scaling").getAsBoolean()
			|| !heroicJson.get("damage_type").getAsString().equals("forestry:heroic")
			|| !heroicJson.get("target").getAsString().equals("monsters")) {
			helper.fail("HEROIC did not preserve its damage/armor_scaling/damage_type/target through the datapack migration: " + heroicJson);
			return;
		}

		// The damage type and target filter survive the dispatch codec. `target` accepts the class-based builtins and
		// an entity-type tag; MONSTERS exists as a builtin because Monster is a class, so it catches modded monsters
		// and no vanilla entity-type tag is equivalent.
		DamageBeeEffect misanthrope = new DamageBeeEffect(new ThrottleSettings(true, 20, false, false), 4f, true, 1.0f,
			CoreDamageTypes.MISANTHROPE, DamageBeeEffect.Target.Builtin.PLAYERS);
		JsonElement json = IBeeEffect.CODEC.encodeStart(ops, misanthrope).getOrThrow();
		if (!json.getAsJsonObject().get("target").getAsString().equals("players")
			|| !json.getAsJsonObject().get("damage_type").getAsString().equals("forestry:misanthrope")
			|| json.getAsJsonObject().get("combinable").getAsBoolean()
			|| !(IBeeEffect.CODEC.parse(ops, json).getOrThrow() instanceof DamageBeeEffect)) {
			helper.fail("damage_entities damage_type/target/combinable did not round-trip through IBeeEffect.CODEC: " + json);
			return;
		}
		// The default target is every living entity and stays out of the JSON.
		JsonElement aggressive = IBeeEffect.CODEC.encodeStart(ops, new DamageBeeEffect(new ThrottleSettings(true, 40, false, false),
			4f, true, 1.0f, CoreDamageTypes.AGGRESSIVE, DamageBeeEffect.Target.Builtin.ALL)).getOrThrow();
		if (aggressive.getAsJsonObject().has("target")) {
			helper.fail("damage_entities emitted the defaulted target: " + aggressive);
			return;
		}
		// The tag branch round-trips too.
		DamageBeeEffect tagged = new DamageBeeEffect(new ThrottleSettings(true, 40, false, true), 1f, false, 1.0f,
			DamageTypes.GENERIC, new DamageBeeEffect.Target.TagTarget(EntityTypeTags.SKELETONS));
		JsonElement taggedJson = IBeeEffect.CODEC.encodeStart(ops, tagged).getOrThrow();
		if (!taggedJson.getAsJsonObject().getAsJsonObject("target").get("tag").getAsString().equals("#minecraft:skeletons")
			|| !(IBeeEffect.CODEC.parse(ops, taggedJson).getOrThrow() instanceof DamageBeeEffect decodedTag)
			|| !(decodedTag.target() instanceof DamageBeeEffect.Target.TagTarget)) {
			helper.fail("damage_entities tag target did not round-trip: " + taggedJson);
			return;
		}

		helper.succeed();
	}

	/**
	 * Every {@code ThrottledBeeEffect}-derived primitive exposes the four common fields through
	 * {@link ThrottleSettings}, so a pack can retune them. Proven on {@code apply_potion} and {@code resurrect}.
	 */
	@GameTest(template = "empty")
	public static void throttleSettingsRoundTrip(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());

		ThrottleSettings tuned = new ThrottleSettings(false, 50, false, true);
		JsonElement json = IBeeEffect.CODEC.encodeStart(ops, new PotionBeeEffect(tuned, MobEffects.REGENERATION, 100, 1.0f)).getOrThrow();
		if (json.getAsJsonObject().get("throttle").getAsInt() != 50
			|| json.getAsJsonObject().get("requires_working").getAsBoolean()
			|| !json.getAsJsonObject().get("combinable").getAsBoolean()
			|| json.getAsJsonObject().get("dominant").getAsBoolean()) {
			helper.fail("apply_potion did not emit the ThrottleSettings fields: " + json);
			return;
		}
		if (!(IBeeEffect.CODEC.parse(ops, json).getOrThrow() instanceof PotionBeeEffect decoded)
			|| !decoded.settings().equals(tuned)) {
			helper.fail("apply_potion did not decode back to the tuned ThrottleSettings");
			return;
		}

		ThrottleSettings resurrectTuned = new ThrottleSettings(false, 7, false, false);
		JsonElement resurrectJson = IBeeEffect.CODEC.encodeStart(ops,
			new ResurrectionBeeEffect(resurrectTuned, ResurrectionBeeEffect.getReanimationList())).getOrThrow();
		if (!(IBeeEffect.CODEC.parse(ops, resurrectJson).getOrThrow() instanceof ResurrectionBeeEffect decodedResurrect)
			|| !decodedResurrect.settings().equals(resurrectTuned)) {
			helper.fail("resurrect did not round-trip its ThrottleSettings");
			return;
		}

		helper.succeed();
	}

	/**
	 * Each primitive's {@link ThrottleSettings} defaults are its historical hardcoded values, so an effect built the
	 * way {@code BeeEffectProvider} builds the built-ins emits none of the four fields it did not already emit. This
	 * is what keeps the generated JSON for the pre-existing built-ins unchanged.
	 */
	@GameTest(template = "empty")
	public static void throttleSettingsDefaultsStayOutOfJson(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());

		// BEATIFIC, verbatim from BeeEffectProvider: dominant=false is the only non-default of the four.
		JsonElement potion = IBeeEffect.CODEC.encodeStart(ops, new PotionBeeEffect(false, MobEffects.REGENERATION, 100)).getOrThrow();
		if (potion.getAsJsonObject().has("throttle") || potion.getAsJsonObject().has("requires_working")
			|| potion.getAsJsonObject().has("combinable")) {
			helper.fail("apply_potion emitted a defaulted ThrottleSettings field: " + potion);
			return;
		}

		// REANIMATION, verbatim from BeeEffectProvider: all four are defaults.
		JsonElement resurrect = IBeeEffect.CODEC.encodeStart(ops,
			new ResurrectionBeeEffect(true, 40, ResurrectionBeeEffect.getReanimationList())).getOrThrow();
		if (resurrect.getAsJsonObject().has("throttle") || resurrect.getAsJsonObject().has("requires_working")
			|| resurrect.getAsJsonObject().has("combinable") || resurrect.getAsJsonObject().has("dominant")) {
			helper.fail("resurrect emitted a defaulted ThrottleSettings field: " + resurrect);
			return;
		}

		helper.succeed();
	}

	/**
	 * SIFTER, GLACIAL and GLOW_BERRY_GROW are generalized into the {@code forestry:transform_block} primitive: the
	 * type is registered, all three built-ins are datapack-defined, and each preserves the throttle settings and the
	 * knobs that blocked its migration (GLACIAL's temperature ceiling and its 10 attempts; SIFTER's 550-tick throttle).
	 */
	@GameTest(template = "empty")
	public static void transformBlockBuiltinsAreDatapackDefined(GameTestHelper helper) {
		if (!ForestryRegistries.BEE_EFFECT_TYPE.containsKey(ForestryConstants.forestry("transform_block"))) {
			helper.fail("transform_block effect type not registered: forestry:transform_block");
			return;
		}
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		for (ResourceLocation id : new ResourceLocation[]{
			ForestryBeeEffects.SIFTER, ForestryBeeEffects.GLACIAL, ForestryBeeEffects.GLOW_BERRY_GROW
		}) {
			if (!(beeType.getBeeEffect(id) instanceof TransformBlockBeeEffect)) {
				helper.fail("datapack-defined built-in " + id + " did not resolve to a TransformBlockBeeEffect");
				return;
			}
		}

		// None of transform_block's codec defaults match what these three effects need — chance in particular
		// defaults to 0.06, and all three must be always-on. Every migrated effect states chance explicitly, so
		// assert it: a silent fallback to the default would drop them to ~6% activation and nothing else would notice.
		for (ResourceLocation id : new ResourceLocation[]{
			ForestryBeeEffects.SIFTER, ForestryBeeEffects.GLACIAL, ForestryBeeEffects.GLOW_BERRY_GROW
		}) {
			float chance = ((TransformBlockBeeEffect) beeType.getBeeEffect(id)).chance();
			if (chance != 1.0f) {
				helper.fail(id + " must be always-on, but its chance is " + chance);
				return;
			}
		}

		// SIFTER: dominant, combinable, 550-tick throttle, requires a working queen, one attempt.
		TransformBlockBeeEffect sifter = (TransformBlockBeeEffect) beeType.getBeeEffect(ForestryBeeEffects.SIFTER);
		if (!sifter.settings().equals(new ThrottleSettings(true, 550, true, true)) || sifter.attempts() != 1) {
			helper.fail("SIFTER did not preserve its settings through the datapack migration: " + sifter.settings());
			return;
		}
		if (!sifter.transforms().getFirst().from().matches(Blocks.DIRT.defaultBlockState())
			|| !sifter.transforms().getFirst().from().matches(Blocks.GRASS_BLOCK.defaultBlockState())) {
			helper.fail("SIFTER's from tag no longer matches the dirt blocks it used to sift");
			return;
		}

		// GLACIAL: 10 attempts, a NORMAL temperature ceiling, and air required above the water it freezes.
		TransformBlockBeeEffect glacial = (TransformBlockBeeEffect) beeType.getBeeEffect(ForestryBeeEffects.GLACIAL);
		if (!glacial.settings().equals(new ThrottleSettings(false, 200, true, false)) || glacial.attempts() != 10
			|| !glacial.maxTemperature().equals(Optional.of(TemperatureType.NORMAL))) {
			helper.fail("GLACIAL did not preserve its settings/attempts/temperature ceiling: " + glacial.settings());
			return;
		}
		TransformBlockBeeEffect.Transform freeze = glacial.transforms().getFirst();
		if (!freeze.requiresAirAbove() || !freeze.from().matches(Blocks.WATER.defaultBlockState())
			|| !(freeze.to() instanceof TransformBlockBeeEffect.To.Fixed ice) || !ice.state().is(Blocks.ICE)) {
			helper.fail("GLACIAL no longer freezes water into ice with air above it");
			return;
		}

		// GLOW_BERRY_GROW: a property mutation over the cave-vine tag, preserving the vine's other properties.
		TransformBlockBeeEffect glow = (TransformBlockBeeEffect) beeType.getBeeEffect(ForestryBeeEffects.GLOW_BERRY_GROW);
		if (!glow.settings().equals(new ThrottleSettings(false, 200, true, true)) || glow.attempts() != 1) {
			helper.fail("GLOW_BERRY_GROW did not preserve its settings through the datapack migration: " + glow.settings());
			return;
		}
		TransformBlockBeeEffect.Transform grow = glow.transforms().getFirst();
		if (!grow.from().matches(Blocks.CAVE_VINES.defaultBlockState())
			|| !grow.from().matches(Blocks.CAVE_VINES_PLANT.defaultBlockState())
			|| !(grow.to() instanceof TransformBlockBeeEffect.To.SetProperties)) {
			helper.fail("GLOW_BERRY_GROW no longer sets berries on the cave-vine blocks");
			return;
		}
		if (!grow.to().apply(Blocks.CAVE_VINES.defaultBlockState()).getValue(BlockStateProperties.BERRIES)
			|| !grow.to().apply(Blocks.CAVE_VINES_PLANT.defaultBlockState()).getValue(BlockStateProperties.BERRIES)) {
			helper.fail("GLOW_BERRY_GROW's transform did not set berries on a bare cave vine");
			return;
		}

		helper.succeed();
	}
}
