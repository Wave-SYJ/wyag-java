package top.wavesyj.wyag.object;

import top.wavesyj.wyag.util.KeyValueList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class GitTag extends GitObject {

    private KeyValueList list;


    public GitTag(GitRepository repo) {
        super(repo);
    }

    public GitTag(GitRepository repo, byte[] data) {
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
        return Type.tag.name();
    }

    public static void createTag(GitRepository repo, String name, String ref, boolean createObject) {
        String sha = GitObject.findObject(repo, ref);

        if (createObject) {
            GitTag tag = new GitTag(repo);
            tag.list = new KeyValueList();
            tag.list.put("object", sha.getBytes());
            tag.list.put("type", "commit".getBytes());
            tag.list.put("tag", name.getBytes());
            tag.list.put("tagger", "tagger <tagger@example.com>".getBytes());
            tag.list.setMessage("This is the commit message that should have come from the user\n".getBytes());
            sha = GitObject.writeObject(tag, true);
        }

        try (Writer w = new BufferedWriter(new FileWriter(GitRepository.repoFile(repo, true, "refs", "tags", name)))) {
            w.write(sha + "\n");
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

    public KeyValueList getList() {
        return list;
    }
}
