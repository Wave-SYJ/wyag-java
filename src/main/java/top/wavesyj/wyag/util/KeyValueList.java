package top.wavesyj.wyag.util;

import java.util.*;

import static top.wavesyj.wyag.util.ArrayUtils.findIndexOf;

public class KeyValueList {

    private static class Value {
        byte[] object;

        public Value() {
        }

        public Value(byte[] object) {
            this.object = object;
        }
    }

    private static class Pair<S, V> {
        S first;
        V second;

        public Pair() {
        }

        public Pair(S first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    private final Map<String, List<Value>> map = new HashMap<>();

    private final List<Pair<String, Value>> list = new ArrayList<>();

    private byte[] message = null;

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public List<byte[]> get(String key) {
        return map.get(key).stream().map(value -> value.object).toList();
    }

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

    public byte[] getMessage() {
        return message;
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

    public static byte[] serialize(KeyValueList kvl) {
        List<Byte> res = new ArrayList<>();
        for (Pair<String, Value> pair : kvl.list) {
            for (byte b : pair.first.getBytes())
                res.add(b);
            res.add((byte) ' ');
            byte[] object = pair.second.object;
            for (int i = 0; i < object.length; i++) {
                res.add(object[i]);
                if (object[i] == (byte) '\n' && (i == object.length - 1 || object[i + 1] != (byte) '\n'))
                    res.add((byte) ' ');
            }
            res.add((byte) '\n');
        }
        res.add((byte) '\n');
        for (byte b : kvl.getMessage())
            res.add(b);

        byte[] bytes = new byte[res.size()];
        for (int i = 0; i < res.size(); i++)
            bytes[i] = res.get(i);
        return bytes;
    }
}
