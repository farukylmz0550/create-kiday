package com.farukylmz.createkiday.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Persistent rules for a tag filter. IDs remain namespaced strings so datapack tags can reload dynamically. */
public record TagFilterData(List<String> tags, MatchMode matchMode, FilterMode filterMode) {
    public static final int MAX_TAGS = 128;
    public static final int MAX_TAG_ID_LENGTH = 256;
    public static final TagFilterData EMPTY = new TagFilterData(List.of(), MatchMode.ANY, FilterMode.WHITELIST);

    public static final Codec<TagFilterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(TagFilterData::tags),
            MatchMode.CODEC.optionalFieldOf("match_mode", MatchMode.ANY).forGetter(TagFilterData::matchMode),
            FilterMode.CODEC.optionalFieldOf("filter_mode", FilterMode.WHITELIST).forGetter(TagFilterData::filterMode)
    ).apply(instance, TagFilterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagFilterData> STREAM_CODEC = new StreamCodec<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, List<String>> tagList = ByteBufCodecs.collection(
                ArrayList::new, ByteBufCodecs.stringUtf8(MAX_TAG_ID_LENGTH), MAX_TAGS);

        @Override
        public TagFilterData decode(RegistryFriendlyByteBuf buffer) {
            return new TagFilterData(tagList.decode(buffer), MatchMode.STREAM_CODEC.decode(buffer),
                    FilterMode.STREAM_CODEC.decode(buffer));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, TagFilterData data) {
            tagList.encode(buffer, data.tags);
            MatchMode.STREAM_CODEC.encode(buffer, data.matchMode);
            FilterMode.STREAM_CODEC.encode(buffer, data.filterMode);
        }
    };

    public TagFilterData {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (tags != null) {
            for (String raw : tags) {
                if (normalized.size() >= MAX_TAGS) break;
                if (raw == null || raw.length() > MAX_TAG_ID_LENGTH) continue;
                ResourceLocation id = ResourceLocation.tryParse(raw.trim());
                if (id != null) normalized.add(id.toString());
            }
        }
        tags = List.copyOf(normalized);
        matchMode = matchMode == null ? MatchMode.ANY : matchMode;
        filterMode = filterMode == null ? FilterMode.WHITELIST : filterMode;
    }

    public enum MatchMode {
        ANY("any"), ALL("all");

        private final String id;
        MatchMode(String id) { this.id = id; }

        private static final Codec<MatchMode> CODEC = Codec.STRING.xmap(MatchMode::fromId, mode -> mode.id);
        private static final StreamCodec<RegistryFriendlyByteBuf, MatchMode> STREAM_CODEC = new StreamCodec<>() {
            @Override public MatchMode decode(RegistryFriendlyByteBuf buffer) {
                return values()[Math.floorMod(buffer.readVarInt(), values().length)];
            }
            @Override public void encode(RegistryFriendlyByteBuf buffer, MatchMode mode) {
                buffer.writeVarInt(mode.ordinal());
            }
        };

        private static MatchMode fromId(String id) {
            return "all".equals(id) ? ALL : ANY;
        }
    }

    public enum FilterMode {
        WHITELIST("whitelist"), BLACKLIST("blacklist");

        private final String id;
        FilterMode(String id) { this.id = id; }

        private static final Codec<FilterMode> CODEC = Codec.STRING.xmap(FilterMode::fromId, mode -> mode.id);
        private static final StreamCodec<RegistryFriendlyByteBuf, FilterMode> STREAM_CODEC = new StreamCodec<>() {
            @Override public FilterMode decode(RegistryFriendlyByteBuf buffer) {
                return values()[Math.floorMod(buffer.readVarInt(), values().length)];
            }
            @Override public void encode(RegistryFriendlyByteBuf buffer, FilterMode mode) {
                buffer.writeVarInt(mode.ordinal());
            }
        };

        private static FilterMode fromId(String id) {
            return "blacklist".equals(id) ? BLACKLIST : WHITELIST;
        }
    }
}
