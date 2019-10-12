package net.adeptropolis.nephila.graph.backend;

// TODO: Move into subgraph class?

public class SubgraphVertexIterator implements VertexIterator {

  private int localId;
  private int globalId;

  private int[] vertexBuf;
  private int size;

  public SubgraphVertexIterator() {
  }

  public SubgraphVertexIterator reset(int[] vertexBuf, int size) {
    this.vertexBuf = vertexBuf;
    this.size = size;
    this.localId = 0;
    this.globalId = -1;
    return this;
  }

  /**
   * Note: Must proceed before first use => while(...)
   * @return
   */

  @Override
  public boolean proceed() {
    if (localId == size) {
      return false;
    }
    globalId = vertexBuf[localId++];
    return true;
  }

  /**
   * Note: -1 because right after <code>globalId</code> is assigned, <code>localId</code> is being incremented
   * @return
   */

  @Override
  public int localId() {
    return localId - 1;
  }

  @Override
  public int globalId() {
    return globalId;
  }

}
