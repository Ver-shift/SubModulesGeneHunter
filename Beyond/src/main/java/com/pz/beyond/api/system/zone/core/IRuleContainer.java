package com.pz.beyond.api.system.zone.core;

import com.pz.beyond.api.system.rule.RuleData;

import java.util.List;

public interface IRuleContainer {

    List<RuleData> getListeners();
    void addListener(RuleData listener);
    void removeListener(RuleData listener);
    void clearListeners();


}
