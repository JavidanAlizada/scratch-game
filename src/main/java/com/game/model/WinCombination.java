package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class WinCombination {
    @JsonProperty("reward_multiplier")
    private double rewardMultiplier;
    private String when;
    private int count;
    private String group;
    @JsonProperty("covered_areas")
    public List<List<String>> coveredAreas;
}
