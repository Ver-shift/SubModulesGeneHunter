package org.galaxy.gene_hunter.api.system.choice;

import java.util.List;

public record ChoiceRewardScheme(List<ChoiceStage> stages) {

    public ChoiceRewardScheme {
        stages = stages == null ? List.of() : List.copyOf(stages);
    }

    public boolean isEmpty() {
        return stages.isEmpty();
    }
}
