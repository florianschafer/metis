/*
 * Copyright (c) Florian Schaefer 2019.
 *
 * This file is subject to the terms and conditions defined in the
 * file 'LICENSE.txt', which is part of this source code package.
 */

package net.adeptropolis.nephila;

import net.adeptropolis.nephila.clustering.Cluster;
import net.adeptropolis.nephila.clustering.RelativeWeightConsistencyMetric;
import net.adeptropolis.nephila.clustering.labeling.Labeling;
import net.adeptropolis.nephila.clustering.labeling.TopWeightsAggregateLabeling;
import net.adeptropolis.nephila.clustering.sinks.LeafTextSink;
import net.adeptropolis.nephila.clustering.sinks.Sink;
import net.adeptropolis.nephila.clustering.sinks.TextSink;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraph;
import net.adeptropolis.nephila.graphs.implementations.CompressedSparseGraphBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Ignore
public class FooingOuterEdgeSourceTest {

  // Found bug: freq must not be float

  Logger LOG = LoggerFactory.getLogger(FooingOuterEdgeSourceTest.class);

  @Test
  public void newShit() throws FileNotFoundException {

//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.getVertex("/home/florian/Datasets/Essentials/Workbench/fb_names.tsv"));
//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.get("/home/florian/Datasets/Essentials/Workbench/fb_names.5M.tsv"));
//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.get("/home/florian/Datasets/Essentials/Workbench/wiki_en.listjson.lemmas.pairs.1M"));
//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.get("/home/florian/Datasets/Essentials/Workbench/wiki_en.listjson.lemmas.pairs.250k"));
//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.get("/home/florian/Datasets/Essentials/Workbench/wiki_en.listjson.lemmas.pairs.2.5M"));
    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.get("/home/florian/Datasets/Essentials/Workbench/wiki_en.listjson.lemmas.pairs"));
//    LabeledTSVGraphSource g = new LabeledTSVGraphSource(Paths.getVertex("/home/florian/Datasets/Essentials/Workbench/fb_names.30M.tsv"));

    ClusteringSettings settings = new ClusteringSettings(50, 0.2, 0.75, 1E-5, true, 10000);    CompressedSparseGraphBuilder builder = new CompressedSparseGraphBuilder();
    g.edges().sequential().forEach(e -> builder.add(e.u, e.v, e.weight));
    CompressedSparseGraph graph = builder.build();
    RelativeWeightConsistencyMetric metric = new RelativeWeightConsistencyMetric();
    Cluster root = Clustering.run(graph, metric, settings);

    AtomicInteger lost = new AtomicInteger();
    root.traverse(c -> {
      if (!c.getChildren().isEmpty()) {
        lost.addAndGet(c.getRemainder().size());
      }
    });
    LOG.debug("Lost: {} / {}", lost.get(), graph.size());


    Labeling labeling = new TopWeightsAggregateLabeling(100, graph);
//    Labeling labeling = new TopWeightsRemainderLabeling(10, graph);
//    TextSink textSink = new TextSink(Paths.get("/home/florian/tmp/clusters.txt"), labeling, g.inverseLabels());
    Sink textSink = new LeafTextSink(Paths.get("/home/florian/tmp/clusters.txt"), labeling, g.inverseLabels());
    textSink.consume(root);
  }

  interface LabeledEdgeSource<T> {

    Stream<LabeledEdge<T>> edges();

  }

  class TSVLabeledEdgeSource implements LabeledEdgeSource<String> {

    private final Pattern TSVParsePattern = Pattern.compile("\\s*(?<weight>[0-9]+)\\s+(?<u>.+?)\\s+(?<v>.+?)\\s*");
    private final Path path;

    TSVLabeledEdgeSource(Path path) {
      this.path = path;
    }

    @Override
    public Stream<LabeledEdge<String>> edges() {
      ThreadLocal<LabeledEdge<String>> localEdges = ThreadLocal.withInitial(() -> new LabeledEdge<>("", "", 0));
      try {
        return Files.lines(path).parallel().map(line -> {
          Matcher matcher = TSVParsePattern.matcher(line);
          if (matcher.find()) {
            String weight = matcher.group("weight");
            String u = matcher.group("u");
            String v = matcher.group("v");
            if (weight != null && u != null && v != null) {
              double parsedWeight = Double.parseDouble(weight);
              if (parsedWeight > 0 && u.length() > 0 && v.length() > 0) {
                LabeledEdge<String> edge = localEdges.get();
                edge.u = u;
                edge.v = v;
                edge.weight = parsedWeight;
                return edge;
              } else {
                return null;
              }
            } else {
              return null;
            }
          } else {
            return null;
          }
        }).filter(Objects::nonNull);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }
  }

}