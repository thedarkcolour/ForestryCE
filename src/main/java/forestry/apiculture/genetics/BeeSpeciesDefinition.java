package forestry.apiculture.genetics;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import forestry.api.IForestryApi;
import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.alleles.Allele;
import forestry.api.genetics.alleles.IKaryotype;
import forestry.core.genetics.GenomeCodecs;
import forestry.core.genetics.ProductTypes;
import forestry.core.genetics.ISpeciesDefinition;
import forestry.core.genetics.SpeciesCore;

/**
 * The pure-data, datapack-loadable shape of a bee species: everything a {@code BeeSpeciesBuilder} would otherwise
 * set in code, plus a sparse map of genome overrides (chromosome id -&gt; inline allele). This is also the network
 * sync payload, so the client can render species it has never seen registered in code.
 * <p>
 * The {@link #codec()} and {@link #streamCodec()} are built lazily on first use: they are keyed against the bee
 * karyotype ({@link IForestryApi#getGeneticManager()}), which does not exist at class-load time - only once the bee
 * species type has been registered. See {@code forestry.core.genetics.mutations.MutationRecipe.Serializer} for the
 * same pattern applied to mutation recipes.
 *
 * @param genus          The scientific genus name (e.g. {@code "Apis"}).
 * @param species        The scientific species name (e.g. {@code "mellifera"}).
 * @param dominant       Whether this species' species-allele is dominant.
 * @param glint          Whether this species' item/entity renders with an enchantment glint.
 * @param secret         Whether this species is hidden from the analyzer/database until discovered.
 * @param complexity     The breeding complexity used to gate research/analysis costs.
 * @param authority      The credited discoverer/author, shown in the database.
 * @param escritoireColor The color used for this species' entry in the escritoire, or {@code -1} for none.
 * @param temperature    This species' ideal temperature.
 * @param humidity       This species' ideal humidity.
 * @param body           The body color of this species' bee sprite.
 * @param stripes        The stripe color of this species' bee sprite.
 * @param outline        The outline color of this species' bee sprite, or {@code -1} for none.
 * @param products       The items a worker of this species can produce.
 * @param specialties    The items a worker of this species can rarely produce.
 * @param jubilance      The id of the {@link forestry.api.apiculture.IBeeJubilance} that determines when this species is jubilant.
 * @param genome         Sparse genome overrides: chromosome id -&gt; inline allele, applied over the karyotype defaults.
 */
public record BeeSpeciesDefinition(
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
	int body,
	int stripes,
	int outline,
	List<IProduct> products,
	List<IProduct> specialties,
	ResourceLocation jubilance,
	Map<ResourceLocation, Allele<?>> genome
) implements ISpeciesDefinition {
	public static final ResourceLocation DEFAULT_JUBILANCE = ForestryBeeJubilances.DEFAULT;

	@Nullable
	private static Codec<BeeSpeciesDefinition> codec;
	@Nullable
	private static StreamCodec<RegistryFriendlyByteBuf, BeeSpeciesDefinition> streamCodec;

	/**
	 * @return The lazily-built JSON/NBT codec for this record, keyed against the bee karyotype.
	 */
	public static Codec<BeeSpeciesDefinition> codec() {
		Codec<BeeSpeciesDefinition> codec = BeeSpeciesDefinition.codec;
		if (codec == null) {
			codec = buildCodec();
			BeeSpeciesDefinition.codec = codec;
		}
		return codec;
	}

	/**
	 * @return The lazily-built network stream codec for this record, keyed against the bee karyotype.
	 */
	public static StreamCodec<RegistryFriendlyByteBuf, BeeSpeciesDefinition> streamCodec() {
		StreamCodec<RegistryFriendlyByteBuf, BeeSpeciesDefinition> streamCodec = BeeSpeciesDefinition.streamCodec;
		if (streamCodec == null) {
			streamCodec = buildStreamCodec();
			BeeSpeciesDefinition.streamCodec = streamCodec;
		}
		return streamCodec;
	}

	private static IKaryotype karyotype() {
		return IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.BEE).getKaryotype();
	}

	/**
	 * The sprite palette fields (body/stripes/outline), grouped only so {@link #buildCodec()} stays under
	 * {@link RecordCodecBuilder}'s 16-field {@code group()} limit; they still serialize as plain top-level JSON keys.
	 */
	private record SpritePalette(int body, int stripes, int outline) {
		static final MapCodec<SpritePalette> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("body", 0xffdc16).forGetter(SpritePalette::body),
			Codec.INT.optionalFieldOf("stripes", 0).forGetter(SpritePalette::stripes),
			Codec.INT.optionalFieldOf("outline", -1).forGetter(SpritePalette::outline)
		).apply(instance, SpritePalette::new));
	}

	private static Codec<BeeSpeciesDefinition> buildCodec() {
		Codec<Map<ResourceLocation, Allele<?>>> genomeCodec = GenomeCodecs.alleleMapCodec(karyotype());
		return RecordCodecBuilder.create(instance -> instance.group(
			SpeciesCore.MAP_CODEC.forGetter(BeeSpeciesDefinition::core),
			SpritePalette.CODEC.forGetter(def -> new SpritePalette(def.body(), def.stripes(), def.outline())),
			ProductTypes.LIST_CODEC.optionalFieldOf("products", List.of()).forGetter(BeeSpeciesDefinition::products),
			ProductTypes.LIST_CODEC.optionalFieldOf("specialties", List.of()).forGetter(BeeSpeciesDefinition::specialties),
			ResourceLocation.CODEC.optionalFieldOf("jubilance", DEFAULT_JUBILANCE).forGetter(BeeSpeciesDefinition::jubilance),
			genomeCodec.optionalFieldOf("genome", Map.of()).forGetter(BeeSpeciesDefinition::genome)
		).apply(instance, (core, palette, products, specialties, jubilance, genome) ->
			new BeeSpeciesDefinition(core.genus(), core.species(), core.dominant(), core.glint(), core.secret(),
				core.complexity(), core.authority(), core.escritoireColor(), core.temperature(), core.humidity(),
				palette.body(), palette.stripes(), palette.outline(), products, specialties, jubilance, genome)));
	}

	private static StreamCodec<RegistryFriendlyByteBuf, BeeSpeciesDefinition> buildStreamCodec() {
		StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, Allele<?>>> genomeStreamCodec = GenomeCodecs.alleleMapStreamCodec(karyotype());
		StreamCodec<RegistryFriendlyByteBuf, List<IProduct>> productListStreamCodec = ProductTypes.LIST_STREAM_CODEC;
		return StreamCodec.of(
			(buf, def) -> {
				SpeciesCore.STREAM_CODEC.encode(buf, def.core());
				buf.writeInt(def.body);
				buf.writeInt(def.stripes);
				buf.writeInt(def.outline);
				productListStreamCodec.encode(buf, def.products);
				productListStreamCodec.encode(buf, def.specialties);
				ResourceLocation.STREAM_CODEC.encode(buf, def.jubilance);
				genomeStreamCodec.encode(buf, def.genome);
			},
			buf -> {
				SpeciesCore core = SpeciesCore.STREAM_CODEC.decode(buf);
				int body = buf.readInt();
				int stripes = buf.readInt();
				int outline = buf.readInt();
				List<IProduct> products = productListStreamCodec.decode(buf);
				List<IProduct> specialties = productListStreamCodec.decode(buf);
				ResourceLocation jubilance = ResourceLocation.STREAM_CODEC.decode(buf);
				Map<ResourceLocation, Allele<?>> genome = genomeStreamCodec.decode(buf);
				return new BeeSpeciesDefinition(core.genus(), core.species(), core.dominant(), core.glint(), core.secret(),
					core.complexity(), core.authority(), core.escritoireColor(), core.temperature(), core.humidity(),
					body, stripes, outline, products, specialties, jubilance, genome);
			}
		);
	}
}
