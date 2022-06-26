package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitRepository;
import top.wavesyj.wyag.object.GitTag;
import top.wavesyj.wyag.util.RefUtil;

import java.io.File;

@CommandLine.Command(
        name = "tag",
        description = "List and create tags.",
        mixinStandardHelpOptions = true
)
public class TagCommand implements Runnable {

    @CommandLine.Option(names = "-a", description = "Whether to create a tag object.")
    boolean createObject;

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "The new tag's name.")
    String name;

    @CommandLine.Parameters(index = "1", arity = "0..1", description = "The object the new tag will point to.", defaultValue = "HEAD")
    String object;

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        if (name != null)
            GitTag.createTag(repo, name, object, createObject);
        else {
            assert repo != null;
            String path = GitRepository.repoDirectory(repo, "refs");
            File[] files = new File(path).listFiles();
            if (files == null)
                return;

            for (File file : files) {
                String resolved = RefUtil.resolveRef(repo, file.getAbsolutePath());
                System.out.printf("%s %s/%s", resolved, "refs/tag", file.getName());
            }
        }
    }
}
