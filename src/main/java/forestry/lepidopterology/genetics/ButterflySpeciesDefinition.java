package forestry.lepidopterology.genetics;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import forestry.api.IForestryApi;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.alleles.Allele;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.core.genetics.GenomeCodecs;
import forestry.core.genetics.ISpeciesDefinition;
import forestry.core.genetics.ProductTypes;
import forestry.core.genetics.SpeciesCore;

/**
 * Pure-data, datapack-loadable genetics layer of a butterfly species (the entity/cocoon/item bindings stay
 * code-registered global singletons - see the Stage 5 design spec). Also the network sync payload.
 * {@link #codec()}/{@link #streamCodec()} are built lazily against the butterfly karyotype, which only exists once
 * the butterfly species type is registered - see {@link forestry.apiculture.genetics.BeeSpeciesDefinition} and
 * {@link forestry.arboriculture.genetics.TreeSpeciesDefinition} for the same pattern.
 *
 * @param genus               The scientific genus name (e.g. {@code "Danaus"}).
 * @param species             The scientific species name (e.g. {@code "plexippus"}).
 * @param dominant            Whether this species' species-allele is dominant.
 * @param glint               Whether this species' item/entity renders with an enchantment glint.
 * @param secret              Whether this species is hidden from the analyzer/database until discovered.
 * @param complexity          The breeding complexity used to gate research/analysis costs.
 * @param authority           The credited discoverer/author, shown in the database.
 * @param escritoireColor     The color used for this species' entry in the escritoire, or {@code -1} for none.
 * @param temperature         This species' ideal temperature.
 * @param humidity            This species' ideal humidity.
 * @param nocturnal           Whether this species is nocturnal (moths can work at night, butterflies during the day).
 * @param moth                Whether this species is a moth (vs. a butterfly).
 * @param rarity              The relative rarity used when picking this species for worldgen.
 * @param flightDistance      How far this species can fly from its spawn point.
 * @param serumColor          The color of this species' serum item.
 * @param spawnBiomes         The biome tag this species can spawn in, or empty for none.
 * @param products            The items a caught butterfly of this species can produce.
 * @param caterpillarProducts The items a caterpillar of this species can produce.
 * @param genome              Sparse genome overrides: chromosome id -&gt; inline allele, applied over the karyotype defaults.
 */
public record ButterflySpeciesDefinition(
	String genus,
	String species,
	boolean dominant,
	boolean glint,
	boolean secret,
	int complexity,
	String authority,
	int escritoireColor,
	TemperatureType temperature,
	HumidityType humidity,
	boolean nocturnal,
	boolean moth,
	float rarity,
	float flightDistance,
	int serumColor,
	Optional<TagKey<Biome>> spawnBiomes,
	List<IProduct> products,
	List<IProduct> caterpillarProducts,
	Map<ResourceLocation, Allele<?>> genome
) implements ISpeciesDefinition {
	@Nullable
	private static Codec<ButterflySpeciesDefinition> codec;
	@Nullable
	private static StreamCodec<RegistryFriendlyByteBuf, ButterflySpeciesDefinition> streamCodec;

	/**
	 * @return The lazily-built JSON/NBT codec for this record, keyed against the butterfly karyotype.
	 */
	public static Codec<ButterflySpeciesDefinition> codec() {
		Codec<ButterflySpeciesDefinition> codec = ButterflySpeciesDefinition.codec;
		if (codec == null) {
			codec = buildCodec();
			ButterflySpeciesDefinition.codec = codec;
		}
		return codec;
	}

	/**
	 * @return The lazily-built network stream codec for this record, keyed against the butterfly karyotype.
	 */
	public static StreamCodec<RegistryFriendlyByteBuf, ButterflySpeciesDefinition> streamCodec() {
		StreamCodec<RegistryFriendlyByteBuf, ButterflySpeciesDefinition> streamCodec = ButterflySpeciesDefinition.streamCodec;
		if (streamCodec == null) {
			streamCodec = buildStreamCodec();
			ButterflySpeciesDefinition.streamCodec = streamCodec;
		}
		return streamCodec;
	}

	private static IKaryotype karyotype() {
		return IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BUTTERFLY).getKaryotype();
	}

	private static final Codec<List<IProduct>> PRODUCTS_CODEC = ProductTypes.LIST_CODEC;
	private static final StreamCodec<RegistryFriendlyByteBuf, List<IProduct>> PRODUCTS_STREAM_CODEC = ProductTypes.LIST_STREAM_CODEC;

	private static Codec<ButterflySpeciesDefinition> buildCodec() {
		Codec<Map<ResourceLocation, Allele<?>>> genomeCodec = GenomeCodecs.alleleMapCodec(karyotype());
		return RecordCodecBuilder.create(instance -> instance.group(
			SpeciesCore.MAP_CODEC.forGetter(ButterflySpeciesDefinition::core),
			Codec.BOOL.optionalFieldOf("nocturnal", false).forGetter(ButterflySpeciesDefinition::nocturnal),
			Codec.BOOL.optionalFieldOf("moth", false).forGetter(ButterflySpeciesDefinition::moth),
			Codec.FLOAT.optionalFieldOf("rarity", 0.0f).forGetter(ButterflySpeciesDefinition::rarity),
			Codec.FLOAT.optionalFieldOf("flight_distance", 5.0f).forGetter(ButterflySpeciesDefinition::flightDistance),
			Codec.INT.optionalFieldOf("serum_color", 0).forGetter(ButterflySpeciesDefinition::serumColor),
			TagKey.codec(Registries.BIOME).optionalFieldOf("spawn_biomes").forGetter(ButterflySpeciesDefinition::spawnBiomes),
			PRODUCTS_CODEC.optionalFieldOf("products", List.of()).forGetter(ButterflySpeciesDefinition::products),
			PRODUCTS_CODEC.optionalFieldOf("caterpillar_products", List.of()).forGetter(ButterflySpeciesDefinition::caterpillarProducts),
			genomeCodec.optionalFieldOf("genome", Map.of()).forGetter(ButterflySpeciesDefinition::genome)
		).apply(instance, (core, nocturnal, moth, rarity, flightDistance, serumColor, spawnBiomes, products, caterpillarProducts, genome) ->
			new ButterflySpeciesDefinition(core.genus(), core.species(), core.dominant(), core.glint(), core.secret(),
				core.complexity(), core.authority(), core.escritoireColor(), core.temperature(), core.humidity(),
				nocturnal, moth, rarity, flightDistance, serumColor, spawnBiomes, products, caterpillarProducts, genome)));
	}

	private static StreamCodec<RegistryFriendlyByteBuf, ButterflySpeciesDefinition> buildStreamCodec() {
		StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, Allele<?>>> genomeStreamCodec = GenomeCodecs.alleleMapStreamCodec(karyotype());
		StreamCodec<ByteBuf, Optional<TagKey<Biome>>> spawnBiomesStreamCodec = ByteBufCodecs.optional(
			ResourceLocation.STREAM_CODEC.map(location -> TagKey.create(Registries.BIOME, location), TagKey::location));
		return StreamCodec.of(
			(buf, def) -> {
				SpeciesCore.STREAM_CODEC.encode(buf, def.core());
				buf.writeBoolean(def.nocturnal);
				buf.writeBoolean(def.moth);
				buf.writeFloat(def.rarity);
				buf.writeFloat(def.flightDistance);
				buf.writeInt(def.serumColor);
				spawnBiomesStreamCodec.encode(buf, def.spawnBiomes);
				PRODUCTS_STREAM_CODEC.encode(buf, def.products);
				PRODUCTS_STREAM_CODEC.encode(buf, def.caterpillarProducts);
				genomeStreamCodec.encode(buf, def.genome);
			},
			buf -> {
				SpeciesCore core = SpeciesCore.STREAM_CODEC.decode(buf);
				boolean nocturnal = buf.readBoolean();
				boolean moth = buf.readBoolean();
				float rarity = buf.readFloat();
				float flightDistance = buf.readFloat();
				int serumColor = buf.readInt();
				Optional<TagKey<Biome>> spawnBiomes = spawnBiomesStreamCodec.decode(buf);
				List<IProduct> products = PRODUCTS_STREAM_CODEC.decode(buf);
				List<IProduct> caterpillarProducts = PRODUCTS_STREAM_CODEC.decode(buf);
				Map<ResourceLocation, Allele<?>> genome = genomeStreamCodec.decode(buf);
				return new ButterflySpeciesDefinition(core.genus(), core.species(), core.dominant(), core.glint(), core.secret(),
					core.complexity(), core.authority(), core.escritoireColor(), core.temperature(), core.humidity(),
					nocturnal, moth, rarity, flightDistance, serumColor, spawnBiomes, products, caterpillarProducts, genome);
			}
		);
	}
}
