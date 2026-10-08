package forestry.apiculture.bees.genetics;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// not a HolderSet, datagen cannot encode a set built from BuiltInRegistries
public sealed interface BlockMatcher {
	Codec<List<Block>> BLOCK_LIST_CODEC = Codec.either(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec().listOf())
		.xmap(either -> either.map(List::of, Function.identity()),
			list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));
	Codec<BlockMatcher> CODEC = Codec.either(TagKey.hashedCodec(Registries.BLOCK), BLOCK_LIST_CODEC)
		.xmap(either -> either.map(Tag::new, Direct::new),
			matcher -> matcher instanceof Tag tag ? Either.left(tag.tag()) : Either.right(((Direct) matcher).blocks()));

	boolean matches(BlockState state);

	record Tag(TagKey<Block> tag) implements BlockMatcher {
		@Override
		public boolean matches(BlockState state) {
			return state.is(this.tag);
		}
	}

	record Direct(List<Block> blocks) implements BlockMatcher {
		@Override
		public boolean matches(BlockState state) {
			for (Block block : this.blocks) {
				if (state.is(block)) {
					return true;
				}
			}
			return false;
		}
	}
}
