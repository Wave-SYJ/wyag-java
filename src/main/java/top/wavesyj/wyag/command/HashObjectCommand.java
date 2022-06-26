package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

@CommandLine.Command(name = "hash-object", description = "Compute object ID and optionally creates a blob from a file.", mixinStandardHelpOptions = true)
public class HashObjectCommand implements Runnable {

    @CommandLine.Option(names = "-t", defaultValue = "blob", description = "Specify the type: blob, commit, tag, tree")
    GitObject.Type type;

    @CommandLine.Option(names = "-w", description = "Actually write the object into the database")
    boolean actuallyStore;

    @CommandLine.Parameters(index = "0", description = "Read object from <path>")
    String path;


    @Override
    public void run() {
        GitRepository repo = null;
        if (actuallyStore)
            repo = new GitRepository(".");

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(path))) {
            byte[] data = is.readAllBytes();
            String sha = switch (type) {
                case blob -> GitObject.writeObject(new GitBlob(repo, data), actuallyStore);
                case commit -> GitObject.writeObject(new GitCommit(repo, data), actuallyStore);
                case tag -> GitObject.writeObject(new GitTag(repo, data), actuallyStore);
                case tree -> GitObject.writeObject(new GitTree(repo, data), actuallyStore);
            };
            System.out.println(sha);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }
}
