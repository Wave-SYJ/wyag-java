package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "commit",
        mixinStandardHelpOptions = true,
        description = "Checkout a commit inside of a directory."
)
public class CheckoutCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "The commit or tree to checkout.")
    String commit;

    @CommandLine.Parameters(index = "1", description = "The EMPTY directory to cehckout on.")
    String path;

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        GitObject object = GitObject.readObject(repo, GitObject.findObject(repo, commit));
        if (
                !GitObject.Type.commit.name().equalsIgnoreCase(object.getFmt())
                        && !GitObject.Type.tree.name().equalsIgnoreCase(object.getFmt()))
            throw new RuntimeException("Wrong object type: " + commit);

        if (GitObject.Type.commit.name().equalsIgnoreCase(object.getFmt()))
            object = GitObject.readObject(repo, new String(((GitCommit) object).getList().get("tree").get(0)));

        GitTree tree = (GitTree) object;
        File directory = new File(path);
        if (!directory.exists() && !directory.mkdir())
            throw new RuntimeException("Cannot make directory: " + directory.getAbsolutePath());
        if (!directory.isDirectory())
            throw new RuntimeException("Not a directory: " + directory.getAbsolutePath());
        String[] list = directory.list();
        if (list != null && list.length > 0)
            throw new RuntimeException("Not empty directory: " + directory.getAbsolutePath());
        checkout(repo, tree, directory.getAbsolutePath());
    }

    private void checkout(GitRepository repo, GitTree tree, String path) {
        for (GitTree.GitTreeLeaf leaf : tree.getList()) {
            GitObject object = GitObject.readObject(repo, leaf.getSha());
            String dest = Path.of(path, leaf.getPath()).toString();

            if (GitObject.Type.tree.name().equalsIgnoreCase(object.getFmt())) {
                GitTree obj = (GitTree) object;
                if (!new File(dest).mkdir())
                    throw new RuntimeException("Cannot make directory: " + dest);
                checkout(repo, obj, dest);
            } else if (GitObject.Type.blob.name().equalsIgnoreCase(object.getFmt())) {
                GitBlob obj = (GitBlob) object;
                try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dest))) {
                    os.write(obj.serialize());
                } catch (IOException e) {
                    throw new RuntimeException("IO error: " + e.getMessage());
                }
            }
        }
    }
}
