package net.adeptropolis.nephila.graph.implementations;

import net.adeptropolis.nephila.graph.implementations.primitives.arrays.ArrayDoubles;
import net.adeptropolis.nephila.graph.implementations.primitives.arrays.ArrayInts;
import net.adeptropolis.nephila.graph.implementations.primitives.Doubles;
import net.adeptropolis.nephila.graph.implementations.primitives.IntBuffer;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CSRSubmatrixTest {

  private static final Product INNER_PROD = new CSRSubmatrix.DefaultProduct(null);

  @Test
  public void fullRowScalarProduct() {
    CSRStorageBuilder b = new CSRStorageBuilder();
    IntBuffer indices = new ArrayInts(10000);
    Doubles vec = new ArrayDoubles(10000);
    for (int i = 0; i < 10000; i++) {
      b.add(1, i, i + 1);
      indices.set(i, i);
      vec.set(i, i + 3);
    }
    CSRStorage storage = b.build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(1, vec, INNER_PROD);
    assertThat(p, is(333483345000d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void emptyIndexScalarProduct() {
    IntBuffer indices = new ArrayInts(0);
    Doubles vec = new ArrayDoubles(0);
    CSRStorage storage = new CSRStorageBuilder().add(0, 0, 17).build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(0d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void emptyRowScalarProduct() {
    IntBuffer indices = new ArrayInts(3);
    indices.set(0, 0);
    indices.set(1, 2);
    indices.set(2, 3);
    Doubles vec = new ArrayDoubles(3);
    vec.set(0, 9);
    vec.set(1, 11);
    vec.set(2, 13);
    CSRStorage storage = new CSRStorageBuilder().add(1, 0, 17).build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(0d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void scalarProductWithEntryOverhang() {
    IntBuffer indices = new ArrayInts(3);
    indices.set(0, 0);
    indices.set(1, 2);
    indices.set(2, 3);
    Doubles vec = new ArrayDoubles(3);
    vec.set(0, 9);
    vec.set(1, 11);
    vec.set(2, 13);
    CSRStorage storage = new CSRStorageBuilder()
            .add(0, 0, 17)
            .add(0, 2, 19)
            .add(0, 4, 23)
            .add(0, 5, 29)
            .build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(9d * 17d + 11d * 19d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void scalarProductWithIndexOverhang() {
    IntBuffer indices = new ArrayInts(4);
    indices.set(0, 0);
    indices.set(1, 2);
    indices.set(2, 3);
    indices.set(3, 4);
    Doubles vec = new ArrayDoubles(4);
    vec.set(0, 7);
    vec.set(1, 11);
    vec.set(2, 13);
    vec.set(3, 19);
    CSRStorage storage = new CSRStorageBuilder()
            .add(0, 2, 23)
            .add(0, 4, 29)
            .build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(11d * 23d + 19d * 29d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void singleEntryScalarProductWithIndexOverhang() {
    IntBuffer indices = new ArrayInts(2);
    indices.set(0, 0);
    indices.set(1, 2);
    Doubles vec = new ArrayDoubles(2);
    vec.set(0, 7);
    vec.set(1, 11);
    CSRStorage storage = new CSRStorageBuilder().add(0, 2, 23).build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(11d * 23d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }

  @Test
  public void singleIndexyScalarProductWithEntryOverhang() {
    IntBuffer indices = new ArrayInts(1);
    indices.set(0, 0);
    Doubles vec = new ArrayDoubles(4);
    vec.set(0, 7);
    CSRStorage storage = new CSRStorageBuilder()
            .add(0, 0, 11)
            .add(0, 2, 23)
            .build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);
    double p = mat.rowScalarProduct(0, vec, INNER_PROD);
    assertThat(p, is(7d * 11d));
    vec.free();
    indices.free();
    mat.free();
    storage.free();
  }


  @Test
  public void multiplication() {

    IntBuffer indices = new ArrayInts(4);

    indices.set(0, 0);
    indices.set(1, 2);
    indices.set(2, 3);
    indices.set(3, 4);

    Doubles vec = new ArrayDoubles(4);
    vec.set(0, 43);
    vec.set(1, 47);
    vec.set(2, 53);
    vec.set(3, 59);

    CSRStorage storage = new CSRStorageBuilder()
            .add(0, 0, 2)
            .add(0, 1, 3)
            .add(1, 0, 5)
            .add(1, 1, 7)
            .add(1, 2, 11)
            .add(2, 1, 13)
            .add(2, 2, 17)
            .add(2, 3, 19)
            .add(2, 4, 23)
            .add(4, 3, 37)
            .add(4, 4, 41)
            .build();
    CSRSubmatrix mat = new CSRSubmatrix(storage, indices);

    Doubles res = new ArrayDoubles(indices.size());
    mat.multiply(vec, res);
    assertThat(res.get(0), is(2d * 43d));
    assertThat(res.get(1), is(17d * 47d + 19d * 53 + 23d * 59));
    assertThat(res.get(2), is(0d));
    assertThat(res.get(3), is(37d * 53d + 41d * 59d));

    res.free();
    mat.free();
    indices.free();
    vec.free();
    storage.free();



  }

}