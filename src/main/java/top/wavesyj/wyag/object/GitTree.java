package top.wavesyj.wyag.object;

public class GitTree extends GitObject {
    public GitTree(GitRepository repo) {
        super(repo);
    }

    public GitTree(GitRepository repo, byte[] data) {
        super(repo, data);
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void deserialize(byte[] data) {

    }

    @Override
    public String getFmt() {
        return null;
    }
}
