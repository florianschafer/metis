/*
 * Copyright Florian Schaefer 2019.
 * SPDX-License-Identifier: Apache-2.0
 */

package net.adeptropolis.frogspawn.graphs.algorithms;

import net.adeptropolis.frogspawn.ClusteringSettings;
import net.adeptropolis.frogspawn.graphs.GraphTestBase;
import net.adeptropolis.frogspawn.graphs.algorithms.power_iteration.PowerIteration;
import net.adeptropolis.frogspawn.graphs.algorithms.power_iteration.PowerIterationException;
import net.adeptropolis.frogspawn.graphs.algorithms.power_iteration.RandomInitialVectorsSource;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThrows;

public class SpectralBisectorTest extends GraphTestBase {

  private static final RandomInitialVectorsSource IV_SOURCE = new RandomInitialVectorsSource(1337421337L);

  private static final ClusteringSettings settings = ClusteringSettings.builder()
          .minClusterSize(0)
          .minAffiliation(0)
          .trailSize(100)
          .convergenceThreshold(0.999)
          .build();

  @Test
  public void completeBipartiteGraphs() throws PowerIterationException {
    SpectralBisector bisector = new SpectralBisector(settings, IV_SOURCE);
    SubgraphCollectingConsumer c = new SubgraphCollectingConsumer();
    bisector.bisect(completeBipartiteWithWeakLink(), c);
    List<List<Integer>> partitions = c.vertices();
    assertThat(partitions.get(0), containsInAnyOrder(0, 1, 2, 3, 4));
    assertThat(partitions.get(1), containsInAnyOrder(5, 6, 7, 8));
  }

  @Test
  public void iterationExcessYieldsException() {
    SpectralBisector bisector = new SpectralBisector(settings, IV_SOURCE);
    SubgraphCollectingConsumer c = new SubgraphCollectingConsumer();
    assertThrows(PowerIteration.MaxIterationsExceededException.class, () -> bisector.bisect(largeCircle(), c));
  }

}