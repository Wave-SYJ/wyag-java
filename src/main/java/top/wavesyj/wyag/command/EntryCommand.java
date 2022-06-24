package top.wavesyj.wyag.command;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true,
        subcommands = {
                InitCommand.class
        })
public class EntryCommand {
}
