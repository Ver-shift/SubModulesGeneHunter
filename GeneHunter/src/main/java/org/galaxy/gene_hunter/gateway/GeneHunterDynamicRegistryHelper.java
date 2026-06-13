package org.galaxy.gene_hunter.gateway;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GeneHunterDynamicRegistryHelper {

    private static final Field REGISTRY = findField(DynamicRegistry.class, "registry");
    private static final Field CODECS = findField(DynamicRegistry.class, "codecs");
    private static final Method BIND = findMethod(DynamicHolder.class, "bind");
    private static final Method UNBIND = findMethod(DynamicHolder.class, "unbind");

    private GeneHunterDynamicRegistryHelper() {
    }

    public static <T extends CodecProvider<? super T>> DynamicHolder<T> register(DynamicRegistry<T> registry, ResourceLocation id, T value) {
        try {
            BiMap<ResourceLocation, T> entries = HashBiMap.create(currentEntries(registry));
            entries.remove(id);
            entries.put(id, value);
            REGISTRY.set(registry, ImmutableBiMap.copyOf(entries));

            DynamicHolder<T> holder = registry.holder(id);
            UNBIND.invoke(holder);
            BIND.invoke(holder);
            return holder;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register dynamic registry entry: " + id, e);
        }
    }

    public static boolean contains(DynamicRegistry<?> registry, ResourceLocation id) {
        try {
            return rawEntries(registry).containsKey(id);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static <T extends CodecProvider<? super T>> String encode(DynamicRegistry<T> registry, T value) {
        try {
            CodecMap<T> codecs = codecs(registry);
            return codecs.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to encode dynamic registry entry: " + value, e);
        }
    }

    public static <T extends CodecProvider<? super T>> T decode(DynamicRegistry<T> registry, String json) {
        try {
            Pair<T, ?> decoded = codecs(registry).decode(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(json)).getOrThrow();
            return decoded.getFirst();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode dynamic registry entry.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CodecProvider<? super T>> BiMap<ResourceLocation, T> currentEntries(DynamicRegistry<T> registry) throws ReflectiveOperationException {
        return (BiMap<ResourceLocation, T>) REGISTRY.get(registry);
    }

    @SuppressWarnings("unchecked")
    private static BiMap<ResourceLocation, ?> rawEntries(DynamicRegistry<?> registry) throws ReflectiveOperationException {
        return (BiMap<ResourceLocation, ?>) REGISTRY.get(registry);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CodecProvider<? super T>> CodecMap<T> codecs(DynamicRegistry<T> registry) throws ReflectiveOperationException {
        return (CodecMap<T>) CODECS.get(registry);
    }

    private static Field findField(Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing field " + type.getName() + "#" + name, e);
        }
    }

    private static Method findMethod(Class<?> type, String name) {
        try {
            Method method = type.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing method " + type.getName() + "#" + name, e);
        }
    }
}
