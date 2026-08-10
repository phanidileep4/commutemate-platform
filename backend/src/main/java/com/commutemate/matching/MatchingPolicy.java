package com.commutemate.matching;

public record MatchingPolicy(
    double route,
    double schedule,
    double preference,
    double social,
    double history,
    double parking,
    double reliability) {

  public MatchingPolicy {
    double sum = route + schedule + preference + social + history + parking + reliability;
    if (Math.abs(sum - 1.0) > 0.0001) throw new IllegalArgumentException("Weights must sum to 1.0");
  }
}
