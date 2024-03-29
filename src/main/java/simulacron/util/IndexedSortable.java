package simulacron.util;

import java.util.Arrays;

/**
 * A way to obtain, given an array of comparable objects, the permutation of its indexes that gives
 * its element in ascending or descending order.
 *
 * @author Marco Biazzini
 *
 */
public class IndexedSortable implements Comparable<IndexedSortable> {
Object[] data;


IndexedSortable(@SuppressWarnings("rawtypes") Comparable data, Integer index) {
  this.data = new Comparable[2];
  this.data[0] = data;
  this.data[1] = index;
}

@SuppressWarnings({ "unchecked", "rawtypes" })
@Override
public int compareTo(IndexedSortable is) {
  return ((Comparable)(data[0])).compareTo((Comparable)(is.data[0]));
}


static public int[] sortedPermutation(@SuppressWarnings("rawtypes") Comparable[] original, boolean descending) {
  int[] res = new int[original.length];
  IndexedSortable[] data = new IndexedSortable[original.length];

  for (int i = 0; i < original.length; i++) {
    data[i] = new IndexedSortable(original[i], i);
  }
  Arrays.sort(data);
  for (int i = 0; i < data.length; i++) {
    res[descending ? (data.length - 1 - i) : i] = (Integer)(data[i].data[1]);
  }
  return res;
}

}
