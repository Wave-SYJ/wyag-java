package top.wavesyj.wyag.object;

import top.wavesyj.wyag.util.KeyValueList;

public class GitCommit extends GitObject {

    private KeyValueList list;

    public GitCommit(GitRepository repo) {
        super(repo);
    }

    public GitCommit(GitRepository repo, byte[] data) {
        super(repo, data);
    }

    @Override
    public byte[] serialize() {
        return KeyValueList.serialize(list);
    }

    @Override
    public void deserialize(byte[] data) {
        list = KeyValueList.parse(data);
    }

    @Override
    public String getFmt() {
        return "commit";
    }

    public KeyValueList getList() {
        return list;
    }
}
