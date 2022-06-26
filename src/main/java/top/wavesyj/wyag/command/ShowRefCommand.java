package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitRepository;

import java.io.File;

import static top.wavesyj.wyag.util.RefUtil.resolveRef;

@CommandLine.Command(
        name = "show-ref",
        mixinStandardHelpOptions = true,
        description = "List references."
)
public class ShowRefCommand implements Runnable {

    private void showRef(GitRepository repo, String path, String prefix) {
        if (path == null)
            path = GitRepository.repoDirectory(repo, "refs");

        File[] files = new File(path).listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory())
                showRef(repo, file.getAbsolutePath(), prefix + "/" + file.getName());
            else {
                String resolved = resolveRef(repo, file.getAbsolutePath());
                System.out.printf(
                        "%s %s/%s%n",
                        resolved,
                        prefix,
                        file.getName()
                );
            }
        }
    }

    @Override
    public void run() {
        GitRepository repo = GitRepository.findRepo(".", true);
        showRef(repo, null, "refs");
    }

}
