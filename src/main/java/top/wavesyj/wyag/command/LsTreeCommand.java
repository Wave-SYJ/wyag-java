package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitObject;
import top.wavesyj.wyag.object.GitRepository;
import top.wavesyj.wyag.object.GitTree;

@CommandLine.Command(
        name = "ls-tree",
        mixinStandardHelpOptions = true,
        description = "Pretty-print a tree object."
)
public class LsTreeCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "The object to show.")
    String object;

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        GitObject obj = GitObject.readObject(repo, GitObject.findObject(repo, object));
        if (!GitObject.Type.tree.name().equalsIgnoreCase(obj.getFmt()))
            throw new RuntimeException("Wrong object type: " + object);

        GitTree tree = (GitTree) obj;
        for (GitTree.GitTreeLeaf leaf : tree.getList()) {
            System.out.printf(
                    "%s %s %s\t%s%n", leaf.getMode(),
                    GitObject.readObject(repo, leaf.getSha()).getFmt(),
                    leaf.getSha(),
                    leaf.getPath()
            );
        }
    }
}
