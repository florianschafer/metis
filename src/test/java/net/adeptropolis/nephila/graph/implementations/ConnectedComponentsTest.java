package net.adeptropolis.nephila.graph.implementations;

import com.google.common.collect.Lists;
import net.adeptropolis.nephila.graph.backend.CSRStorage;
import net.adeptropolis.nephila.graph.backend.UndirectedCSRStorageBuilder;
import net.adeptropolis.nephila.graph.backend.View;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConnectedComponentsTest {

  @Test
  public void fullyConnectedGraph() {
    withButterfly(new int[]{0, 1, 2, 3, 4, 5, 6}, new int[]{0, 1, 2, 3, 4, 5, 6});
  }

  private void withButterfly(int[] viewIndices, int[]... expected) {
    CSRStorage butterfly = new UndirectedCSRStorageBuilder()
            .add(0, 1, 1)
            .add(0, 2, 1)
            .add(1, 2, 1)
            .add(2, 3, 1)
            .add(0, 4, 1)
            .add(0, 5, 1)
            .add(4, 5, 1)
            .add(5, 6, 1)
            .build();
    List<View> components = Lists.newArrayList();
    new ConnectedComponents(butterfly.view(viewIndices)).find(components::add);
    assertThat("Number of components should agree", components.size(), is(expected.length));
    components.sort(Comparator.comparingInt(comp -> comp.getVertex(0)));
    for (int i = 0; i < components.size(); i++) {
      View component = components.get(i);
      assertThat("Component size should agree", component.size(), is(expected[i].length));
      for (int j = 0; j < component.size(); j++) {
        assertThat("Component has member", component.getVertex(j), is(expected[i][j]));
      }
    }
  }

  @Test
  public void removingNonBridgePreservesComponent() {
    withButterfly(new int[]{0, 2, 3, 4, 5, 6}, new int[]{0, 2, 3, 4, 5, 6});
  }

  @Test
  public void splitAtMiddleHub() {
    withButterfly(new int[]{1, 2, 3, 4, 5, 6}, new int[]{1, 2, 3}, new int[]{4, 5, 6});
  }

  @Test
  public void splitMultiple() {
    withButterfly(new int[]{1, 3, 4, 5, 6}, new int[]{1}, new int[]{3}, new int[]{4, 5, 6});
  }


}