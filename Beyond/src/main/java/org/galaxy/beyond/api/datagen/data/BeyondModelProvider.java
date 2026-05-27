package org.galaxy.beyond.api.datagen.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.util.concurrent.CompletableFuture;

public class BeyondModelProvider implements DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Beyond Models";
    }
}
