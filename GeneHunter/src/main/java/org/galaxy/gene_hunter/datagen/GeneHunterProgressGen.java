package org.galaxy.gene_hunter.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import org.galaxy.beyond.api.datagen.custom.RogueProgressProvider;
import org.galaxy.gene_hunter.progress.GeneHunterProgress;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeneHunterProgressGen extends RogueProgressProvider {

    public GeneHunterProgressGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void registerProgress(List<Entry> entries) {
        entries.add(entry("gene_hunter", new GeneHunterProgress().build()));
    }

    @Override
    public String getName() {
        return "GeneHunter Rogue Progress";
    }
}
