package top.wavesyj.wyag.command;

import picocli.CommandLine;
import top.wavesyj.wyag.object.GitObject;
import top.wavesyj.wyag.object.GitRepository;

@CommandLine.Command(
        name = "rev-parse",
        description = "Parse revision (or other objects) identifiers",
        mixinStandardHelpOptions = true
)
public class RevParseCommand implements Runnable {

    @CommandLine.Option(
            names = "--wyag-type",
            arity = "0..1",
            description = "Specify the expected type (blob, commit, tag, tree)"
    )
    GitObject.Type type;

    @CommandLine.Parameters(index = "0", description = "The name to parse")
    String name;

    @Override
    public void run() {
        System.out.println(
                GitObject.findObject(
                        GitRepository.findRepo(".", true),
                        name,
                        type.name(),
                        true
                )
        );
    }
}
