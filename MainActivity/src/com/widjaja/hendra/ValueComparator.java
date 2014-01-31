package com.widjaja.hendra;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/* 
 *  This is a class from MainActivity - Augmented Reality
 *  This class has been modified and adjusted to my project
 *  All rights are reserved. Copyright(c) 2013 Hendra Widjaja
 */

public class ValueComparator  {
    // Main TAG 
    private static final String APPTAG = "ValueComparator";
    
    // SortOnValues
    public static <K, V extends Comparable<V>> Map<K, V> sortOnValues(Map<K, V> unsortedMap) {
        Map<K, V> sortedMap = new TreeMap<K, V>(new MapValueComparator<K, V>(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }
 
    // SortOnKey
    public static <K, V extends Comparable<V>> Map<K, V> sortOnKeys(Map<K, V> unsortedMap) {
        Map<K, V> sortedMap = new TreeMap<K, V>();
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }
}
 
class MapValueComparator<K, V extends Comparable<V>> implements Comparator<K> {
    Map<K, V> map;
 
    MapValueComparator(Map<K, V> map) {
        this.map = map;
    }
 
    public int compare(K key1, K key2) {
        V value1 = map.get(key1);
        V value2 = map.get(key2);
        return value1.compareTo(value2);
    }
}