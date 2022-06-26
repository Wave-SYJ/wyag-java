package top.wavesyj.wyag.object;

import top.wavesyj.wyag.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitTree extends GitObject {

    public static class GitTreeLeaf {
        byte[] mode;
        byte[] path;
        byte[] sha;

        public GitTreeLeaf(byte[] mode, byte[] path, byte[] sha) {
            this.mode = mode;
            this.path = path;
            this.sha = sha;
        }

        public String getMode() {
            return "%6d".formatted(Integer.parseInt(new String(mode)));
        }

        public String getPath() {
            return new String(path);
        }

        public String getSha() {
            StringBuilder builder = new StringBuilder();
            for (byte b : sha) {
                builder.append(Integer.toHexString(b / 16));
                builder.append(Integer.toHexString(b % 16));
            }
            return builder.toString();
        }
    }

    public GitTree(GitRepository repo) {
        super(repo);
    }

    public GitTree(GitRepository repo, byte[] data) {
        super(repo, data);
    }

    private List<GitTreeLeaf> list;

    public List<GitTreeLeaf> getList() {
        return list;
    }

    @Override
    public byte[] serialize() {
        List<Byte> res = new ArrayList<>();
        for (GitTreeLeaf leaf : list) {
            for (byte b : leaf.mode)
                res.add(b);
            res.add((byte) ' ');
            for (byte b : leaf.path)
                res.add(b);
            res.add((byte) '\n');
            for (byte b : leaf.sha)
                res.add(b);
        }
        byte[] bytes = new byte[res.size()];
        for (int i = 0; i < res.size(); i++)
            bytes[i] = res.get(i);
        return bytes;
    }

    @Override
    public void deserialize(byte[] raw) {
        int start = 0;
        list = new ArrayList<>();

        do {
            int space = ArrayUtils.findIndexOf(raw, (byte) ' ', start);
            byte[] mod = Arrays.copyOfRange(raw, start, space);

            int nullCh = ArrayUtils.findIndexOf(raw, (byte) '\0', space);
            byte[] path = Arrays.copyOfRange(raw, space + 1, nullCh);

            start = nullCh + 21;
            list.add(new GitTreeLeaf(mod, path, Arrays.copyOfRange(raw, nullCh + 1, nullCh + 21)));
        } while (start >= raw.length);
    }

    @Override
    public String getFmt() {
        return Type.tree.name();
    }
}
