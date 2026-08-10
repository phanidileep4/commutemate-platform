package com.commutemate.matching;

public record MatchCandidate(
    String candidateId,
    double route,
    double schedule,
    double preference,
    double social,
    double history,
    double parking,
    double reliability) {}
