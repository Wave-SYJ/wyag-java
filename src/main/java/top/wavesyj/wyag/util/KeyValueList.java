package top.wavesyj.wyag.util;

import java.util.*;

class Value {
    byte[] object;

    public Value() {
    }

    public Value(byte[] object) {
        this.object = object;
    }
}

class Pair<S, V> {
    S first;
    V second;

    public Pair() {
    }

    public Pair(S first, V second) {
        this.first = first;
        this.second = second;
    }
}

public class KeyValueList {

    private final Map<String, List<Value>> map = new HashMap<>();

    private final List<Pair<String, Value>> list = new ArrayList<>();

    private byte[] message = null;

    private void put(String key, byte[] value) {
        Value v = new Value(value);
        Pair<String, Value> pair = new Pair<>(key, v);
        list.add(pair);
        if (!map.containsKey(key))
            map.put(key, new ArrayList<>());
        map.get(key).add(v);
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    private static int findIndexOf(byte[] array, byte element, int start) {
        for (int i = start; i < array.length; i++)
            if (array[i] == element)
                return i;
        return -1;
    }

    public static KeyValueList parse(byte[] raw) {
        KeyValueList list = new KeyValueList();

        int start = 0;
        do {
            int space = findIndexOf(raw, (byte) ' ', start);

            String key = new String(Arrays.copyOfRange(raw, start, space));
            int end = space + 1;
            do {
                end = findIndexOf(raw, (byte) '\n', end + 1);
            } while (raw[end + 1] == (byte) ' ' || raw[end + 1] == (byte) '\n');

            boolean stop = false;
            if (raw[end] == (byte) '\n' && raw[end - 1] == (byte) '\n') {
                end--;
                stop = true;
            }

            int size = 0;
            for (int i = space + 1; i < end; i++) {
                size++;
                if (i < end - 1 && raw[i] == (byte) '\n' && raw[i + 1] == (byte) ' ')
                    i++;
            }
            byte[] value = new byte[size];
            for (int i = 0, j = space + 1; i < size && j < raw.length; i++, j++) {
                value[i] = raw[j];
                if (j < end - 1 && raw[j] == (byte) '\n' && raw[j + 1] == (byte) ' ')
                    j++;
            }

            list.put(key, value);
            start = end + 1;

            if (stop)
                break;
        } while (start < raw.length);

        start++;
        list.setMessage(Arrays.copyOfRange(raw, start, raw.length));

        return list;
    }
}
