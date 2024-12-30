package org.poo.ExchangeRate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Pair<K, V> {
    private K key;
    private V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }
}
