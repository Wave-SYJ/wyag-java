package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitObject;
import top.wavesyj.wyag.object.GitRepository;

@CommandLine.Command(
        name = "cat-file",
        description = "Provide content of repository objects.",
        mixinStandardHelpOptions = true
)
public class CatFileCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Specify the type: blob, commit, tag, tree")
    GitObject.Type type;

    @CommandLine.Parameters(index = "1", description = "The object to display")
    String object;

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        GitObject object = GitObject.readObject(repo, GitObject.findObject(repo, this.object));
        System.out.println(new String(object.serialize()));
    }

}
