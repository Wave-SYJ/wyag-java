package top.wavesyj.wyag.util;

public class ArrayUtils {

    public static int findIndexOf(byte[] array, byte element, int start) {
        for (int i = start; i < array.length; i++)
            if (array[i] == element)
                return i;
        return -1;
    }

}
