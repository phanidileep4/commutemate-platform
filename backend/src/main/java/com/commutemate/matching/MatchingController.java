package com.commutemate.matching;

import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchingController {
  private final MatchingEngine engine;
  public MatchingController(MatchingEngine engine) { this.engine = engine; }

  public record RankRequest(MatchingPolicy policy, List<MatchCandidate> candidates) {}
  public record RankedCandidate(String candidateId, MatchScore score) {}

  @PostMapping("/rank")
  public List<RankedCandidate> rank(@RequestBody RankRequest request) {
    return request.candidates().stream()
        .map(c -> new RankedCandidate(c.candidateId(), engine.score(c, request.policy())))
        .sorted(Comparator.comparingDouble((RankedCandidate r) -> r.score().total()).reversed())
        .toList();
  }
}
