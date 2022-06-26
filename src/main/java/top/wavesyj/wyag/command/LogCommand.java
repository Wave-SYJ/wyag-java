package top.wavesyj.wyag.command;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import picocli.CommandLine;
import top.wavesyj.wyag.object.GitCommit;
import top.wavesyj.wyag.object.GitObject;
import top.wavesyj.wyag.object.GitRepository;
import top.wavesyj.wyag.util.KeyValueList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "log",
        description = "Display history of a given commit.",
        mixinStandardHelpOptions = true
)
public class LogCommand implements Runnable {

    @CommandLine.Parameters(index = "0", defaultValue = "HEAD", arity = "0..1", description = "Commit to start at.")
    String commit;

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        SimpleTreeNode node = createGraph(repo, GitObject.findObject(repo, commit), new HashMap<>());
        new TraditionalTreePrinter().print(node);
    }

    private SimpleTreeNode createGraph(GitRepository repo, String sha, Map<String, SimpleTreeNode> map) {
        if (map.containsKey(sha))
            return map.get(sha);

        SimpleTreeNode node = new SimpleTreeNode(sha);
        GitObject object = GitObject.readObject(repo, sha);
        if (!GitObject.Type.commit.name().equalsIgnoreCase(object.getFmt()))
            throw new RuntimeException("Wrong file type: " + sha);

        KeyValueList list = ((GitCommit) object).getList();
        if (!list.containsKey("parent"))
            return node;
        List<byte[]> parents = list.get("parent");
        for (byte[] parent : parents)
            node.addChild(createGraph(repo, new String(parent), map));
        return node;
    }


}
