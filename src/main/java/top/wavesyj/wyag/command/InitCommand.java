package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitRepository;

import java.io.File;

@CommandLine.Command(name = "init", description = "Initialize a new, empty repository.", mixinStandardHelpOptions = true)
public class InitCommand implements Runnable {

    @CommandLine.Parameters(index = "0", defaultValue = ".", arity = "0..1", description = "Where to create the repository")
    File path;


    @Override
    public void run() {
        GitRepository.create(path.getPath());
    }
}
