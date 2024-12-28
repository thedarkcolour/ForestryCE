package forestry.core.genetics.alleles;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IAlleleManager;
import forestry.api.genetics.alleles.IAlleleNaming;
import forestry.api.genetics.alleles.IBooleanAllele;
import forestry.api.genetics.alleles.IBooleanChromosome;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IFloatAllele;
import forestry.api.genetics.alleles.IFloatChromosome;
import forestry.api.genetics.alleles.IIntegerAllele;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.genetics.alleles.IRegistryAllele;
import forestry.api.genetics.alleles.IRegistryAlleleValue;
import forestry.api.genetics.alleles.IRegistryChromosome;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.genetics.alleles.IValueChromosome;

import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class AlleleManager implements IAlleleManager {
	// Default registration state. Both alleles and chromosomes can be registered.
	public static final int REGISTRATION_OPEN = 0;
	// State after species types are registered and karyotypes are built. Chromosomes can no longer be registered.
	public static final int REGISTRATION_CHROMOSOMES_COMPLETE = 1;
	// State after all species are registered. Alleles can no longer be registered.
	public static final int REGISTRATION_ALLELES_COMPLETE = 2;

	// primitive values to primitive allele
	private final Float2ObjectOpenHashMap<IFloatAllele> dominantFloatAlleles = new Float2ObjectOpenHashMap<>();
	private final Float2ObjectOpenHashMap<IFloatAllele> floatAlleles = new Float2ObjectOpenHashMap<>();
	private final Int2ObjectOpenHashMap<IIntegerAllele> dominantIntAlleles = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectOpenHashMap<IIntegerAllele> intAlleles = new Int2ObjectOpenHashMap<>();
	private final IBooleanAllele[] booleanAlleles = new BooleanAllele[4];
	// value to allele
	private final HashMap<?, IValueAllele<?>> dominantValueAlleles = new HashMap<>();
	private final HashMap<?, IValueAllele<?>> valueAlleles = new HashMap<>();
	// id to allele
	private final LinkedHashMap<ResourceLocation, IAllele> allelesByName = new LinkedHashMap<>();
	private final HashMap<ResourceLocation, IChromosome<?>> chromosomes = new HashMap<>();
	private final Codec<IAllele> alleleCodec;
	private final Codec<IChromosome<?>> chromosomeCodec;

	private int registrationState = REGISTRATION_OPEN;

	public AlleleManager() {
		// the flat* codec methods allow for error handling
		this.alleleCodec = ResourceLocation.CODEC.flatXmap(id -> {
			IAllele allele = getAllele(id);
			if (allele != null) {
				return DataResult.success(allele);
			} else {
				return DataResult.error(() -> "Unknown allele: " + id);
			}
		}, allele -> DataResult.success(allele.alleleId()));

		this.chromosomeCodec = ResourceLocation.CODEC.flatXmap(id -> {
			IChromosome<?> chromosome = getChromosome(id);
			if (chromosome != null) {
				return DataResult.success(chromosome);
			} else {
				return DataResult.error(() -> "Unknown chromosome: " + id);
			}
		}, chromosome -> DataResult.success(chromosome.id()));
	}

	private void checkAlleleRegistration() {
		if (this.registrationState == REGISTRATION_ALLELES_COMPLETE) {
			throw new IllegalStateException("Registration of alleles has already finished");
		}
	}

	@Override
	public IBooleanAllele booleanAllele(boolean value, boolean dominant) {
		checkAlleleRegistration();
		// 0 = F, 1 = Fd, 2 = T, 3 = Td
		int index = (value ? (dominant ? 3 : 2) : (dominant ? 1 : 0));
		IBooleanAllele allele = booleanAlleles[index];
		if (allele == null) {
			allele = new BooleanAllele(value, dominant);
			this.booleanAlleles[index] = allele;
			this.allelesByName.put(allele.alleleId(), allele);
		}
		return allele;
	}

	@Override
	public IIntegerAllele intAllele(int value, boolean dominant) {
		checkAlleleRegistration();
		return (dominant ? this.dominantIntAlleles : this.intAlleles).computeIfAbsent(value, v -> {
			IntegerAllele allele = new IntegerAllele(v, dominant);
			if (this.allelesByName.put(allele.alleleId(), allele) != null) {
				throw new IllegalStateException("An allele was already registered with ID " + allele.alleleId());
			}
			return allele;
		});
	}

	@Override
	public IFloatAllele floatAllele(float value, boolean dominant) {
		checkAlleleRegistration();
		return (dominant ? this.dominantFloatAlleles : this.floatAlleles).computeIfAbsent(value, v -> {
			FloatAllele allele = new FloatAllele(v, dominant);
			if (this.allelesByName.put(allele.alleleId(), allele) != null) {
				throw new IllegalStateException("An allele was already registered with ID " + allele.alleleId());
			}
			return allele;
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends IRegistryAlleleValue> IRegistryAllele<V> registryAllele(ResourceLocation id, IRegistryChromosome<V> chromosome) {
		checkAlleleRegistration();
		return (IRegistryAllele<V>) this.allelesByName.computeIfAbsent(id, key -> new RegistryAllele<>(key, chromosome));
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <V> IValueAllele<V> valueAllele(V value, boolean dominant, IAlleleNaming<V> naming) {
		Preconditions.checkNotNull(value, "Allele value must not be null");
		checkAlleleRegistration();

		// Try to get existing by value first
		HashMap<?, IValueAllele<?>> valueToAlleleMap = (dominant ? this.dominantValueAlleles : this.valueAlleles);
		IValueAllele<?> valueExisting = valueToAlleleMap.get(value);

		if (valueExisting != null) {
			return (IValueAllele<V>) valueExisting;
		} else {
			// Then try to get existing by name
			ResourceLocation name = naming.getName(value, dominant);
			IAllele byName = this.allelesByName.get(name);

			if (byName != null) {
				if (byName instanceof IValueAllele<?> allele) {
					if (allele.value() != value) {
						throw new IllegalStateException("Tried to register two values with the same value allele ID: " + allele.value() + " and " + value + " under ID " + name);
					} else {
						return (IValueAllele<V>) allele;
					}
				} else {
					throw new IllegalStateException("Tried to register a value allele with ID " + name + " but an allele was already registered with type " + byName.getClass());
				}
			} else {
				// Create new allele
				IValueAllele<V> allele = new ValueAllele<>(name, value, dominant);
				((HashMap) valueToAlleleMap).put(value, allele);
				this.allelesByName.put(name, allele);

				return allele;
			}
		}
	}

	@Override
	public Codec<IAllele> alleleCodec() {
		// should we offer a compressed codec as well? it would only be used in JsonOps.COMPRESSED
		return this.alleleCodec;
	}

	@Override
	public Codec<IChromosome<?>> chromosomeCodec() {
		return this.chromosomeCodec;
	}

	@Override
	@Nullable
	public IAllele getAllele(ResourceLocation id) {
		return this.allelesByName.get(id);
	}

	@Override
	@Nullable
	public IChromosome<?> getChromosome(ResourceLocation id) {
		return this.chromosomes.get(id);
	}

	private void checkChromosomeRegistration() {
		if (this.registrationState == REGISTRATION_CHROMOSOMES_COMPLETE) {
			throw new IllegalStateException("Registration of chromosomes has already finished");
		}
	}

	@Override
	public IFloatChromosome floatChromosome(ResourceLocation id) {
		checkChromosomeRegistration();
		return (IFloatChromosome) this.chromosomes.computeIfAbsent(id, FloatChromosome::new);
	}

	@Override
	public IIntegerChromosome intChromosome(ResourceLocation id) {
		checkChromosomeRegistration();
		return (IIntegerChromosome) this.chromosomes.computeIfAbsent(id, IntegerChromosome::new);
	}

	@Override
	public IBooleanChromosome booleanChromosome(ResourceLocation id) {
		checkChromosomeRegistration();
		return (IBooleanChromosome) this.chromosomes.computeIfAbsent(id, BooleanChromosome::create);
	}

	@Override
	public <V> IValueChromosome<V> valueChromosome(ResourceLocation id, Class<V> valueClass) {
		checkChromosomeRegistration();
		return registerValueChromosome(id, valueClass, ValueChromosome::new);
	}

	@Override
	public <V extends IRegistryAlleleValue> IRegistryChromosome<V> registryChromosome(ResourceLocation id, Class<V> valueClass) {
		checkChromosomeRegistration();
		return registerValueChromosome(id, valueClass, RegistryChromosome::new);
	}

	private <V, C extends IValueChromosome<V>> C registerValueChromosome(ResourceLocation id, Class<V> valueClass, BiFunction<ResourceLocation, Class<V>, C> factory) {
		@SuppressWarnings("unchecked")
		C existing = (C) this.chromosomes.get(id);

		if (existing == null) {
			// Create new chromosome
			C chromosome = factory.apply(id, valueClass);
			this.chromosomes.put(id, chromosome);
			return chromosome;
		} else if (existing.valueClass().equals(valueClass)) {
			// Return existing equivalent chromosome
			return existing;
		} else {
			// Crash in case of conflicting chromosome.
			throw new IllegalStateException("A chromosome is already registered with ID " + id + " with a different value type: " + existing.valueClass() + " was registered, but tried register again with valueClass: " + valueClass);
		}
	}

	public void setRegistrationState(int state) {
		Preconditions.checkArgument(state == REGISTRATION_CHROMOSOMES_COMPLETE || state == REGISTRATION_ALLELES_COMPLETE, "Invalid registry state");
		this.registrationState++;

		if (this.registrationState != state) {
			throw new IllegalStateException("That registration state has already finished: " + state);
		}
		if (state == REGISTRATION_ALLELES_COMPLETE) {
			this.dominantFloatAlleles.trim();
			this.floatAlleles.trim();
			this.dominantIntAlleles.trim();
			this.intAlleles.trim();

			boolean hasErrors = false;

			for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
				for (IChromosome<?> chromosome : type.getKaryotype().getChromosomes()) {
					if (chromosome instanceof RegistryChromosome<?> registry) {
						boolean missingValues = false;

						for (IRegistryAllele<?> allele : registry.alleles()) {
							if (allele.value() == null) {
								if (!missingValues) {
									Forestry.LOGGER.error("Registry chromosome {} is missing values for the following alleles: ", chromosome.id());
									hasErrors = true;
									missingValues = true;
								}
								Forestry.LOGGER.error("  > {}", allele.alleleId());
							}
						}
					}
				}
			}

			if (hasErrors) {
				throw new IllegalStateException("Missing values for certain IRegistryAllele - check log for details");
			}
		}
	}
}
