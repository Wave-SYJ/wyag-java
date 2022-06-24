package top.wavesyj.wyag;

import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class GitRepository {

    private final String worktree;
    private final File gitDir;

    private Wini conf = null;

    public GitRepository(String path) {
        this(path, false);
    }

    public GitRepository(String path, boolean force) {
        this.worktree = path;
        this.gitDir = Paths.get(path, ".git").toFile();

        if (!force && !gitDir.isDirectory())
            throw new RuntimeException(String.format("Not a Git repository %s", path));

        File configFile = new File(gitDir, "config");
        if (!force && !configFile.isFile())
            throw new RuntimeException("Configuration file missing");

        try {
            if (configFile.exists() && configFile.isFile())
                this.conf = new Wini(configFile);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }

        if (!force && this.conf != null) {
            Integer version = this.conf.get("core", "repositoryformatversion", Integer.class);
            if (version != 0)
                throw new RuntimeException(String.format("Unsupported repositoryformatversion %d", version));
        }
    }

    public static void create(String path) {
        GitRepository repo = new GitRepository(path, true);
        if (repo.gitDir.exists() && !repo.gitDir.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory", repo.gitDir.getAbsolutePath()));
        }
        if (!repo.gitDir.exists() && !repo.gitDir.mkdir())
            throw new RuntimeException(String.format("Cannot create directory %s", repo.gitDir.getAbsolutePath()));

        repoDirectory(repo, "branches");
        repoDirectory(repo, "objects");
        repoDirectory(repo, "refs");
        repoDirectory(repo, "refs", "tags");
        repoDirectory(repo, "refs", "heads");

        try (BufferedWriter fw = new BufferedWriter(new FileWriter(repoFile(repo, "description")))) {
            fw.write("Unnamed repository; edit this file 'description' to name the repository.");
            fw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }

        try (BufferedWriter fw = new BufferedWriter(new FileWriter(repoFile(repo, "HEAD")))) {
            fw.write("ref: refs/heads/master");
            fw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }

        try {
            repo.conf = new Wini(repoFile(repo, "config"));
            Profile.Section section = repo.conf.add("core");
            section.add("repositoryformatversion", "0");
            section.add("filemode", "false");
            section.add("bare", "false");
            repo.conf.store();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

    public static GitRepository findRepo(String path, boolean required) {
        File file = new File(path).getAbsoluteFile();
        File gitDir = new File(file, ".git");
        if (gitDir.isDirectory())
            return new GitRepository(gitDir.getPath());

        String parent = file.getParent();
        if (parent == null) {
            if (required)
                throw new RuntimeException("No git directory.");
            else
                return null;
        }

        return findRepo(parent, required);
    }

    private static void repoDirectory(GitRepository repo, String... path) {
        File file = new File(String.valueOf(Paths.get(repo.gitDir.getPath(), path)));
        if (file.exists()) {
            if (!file.isDirectory())
                throw new RuntimeException(String.format("%s is not a directory", file.getAbsolutePath()));
        } else if (!file.mkdirs())
            throw new RuntimeException(String.format("Cannot make directory %s", file.getAbsolutePath()));
    }

    private static File repoFile(GitRepository repo, String... path) {
        File file = new File(String.valueOf(Paths.get(repo.gitDir.getPath(), path)));
        if (file.exists()) {
            if (!file.isFile())
                throw new RuntimeException(String.format("%s is not a file", file.getAbsolutePath()));
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Cannot create file " + file.getAbsolutePath());
            }
        }
        return file;
    }

}
