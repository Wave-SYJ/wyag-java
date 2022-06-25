package top.wavesyj.wyag.object;

public class GitBlob extends GitObject {


    public GitBlob(GitRepository repo) {
        super(repo);
    }

    public GitBlob(GitRepository repo, byte[] data) {
        super(repo, data);
    }

    private byte[] blobData;

    @Override
    public byte[] serialize() {
        return blobData;
    }

    @Override
    public void deserialize(byte[] data) {
        this.blobData = data;
    }

    @Override
    public String getFmt() {
        return Type.blob.name();
    }
}
