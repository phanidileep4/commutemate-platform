package com.commutemate.matching;

import java.util.Map;

public record MatchScore(double total, Map<String, Double> dimensions, String explanation) {}
