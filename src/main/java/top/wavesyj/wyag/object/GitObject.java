package top.wavesyj.wyag.object;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public abstract class GitObject {

    public enum Type {
        blob, commit, tag, tree
    }

    protected final GitRepository repo;

    public GitObject(GitRepository repo) {
        this.repo = repo;
    }

    public GitObject(GitRepository repo, byte[] data) {
        this.repo = repo;
        if (data != null)
            deserialize(data);
    }

    public abstract byte[] serialize();

    public abstract void deserialize(byte[] data);

    public abstract String getFmt();

    public static GitObject readObject(GitRepository repo, String sha) {
        File file = GitRepository.repoFile(repo, false, "objects", sha.substring(0, 2), sha.substring(2));
        if (!file.exists() || !file.isFile())
            throw new RuntimeException("No such file " + file.getAbsolutePath());

        try (InflaterInputStream is = new InflaterInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            byte[] raw = is.readAllBytes();
            String fmt, sizeStr;
            int spacePos = 0, nullPos = 0;

            for (int i = 0; i < raw.length; i++)
                if (raw[i] == ' ') {
                    spacePos = i;
                    break;
                }
            fmt = new String(Arrays.copyOf(raw, spacePos), StandardCharsets.US_ASCII);

            for (int i = spacePos + 1; i < raw.length; i++)
                if (raw[i] == 0) {
                    nullPos = i;
                    break;
                }
            sizeStr = new String(Arrays.copyOfRange(raw, spacePos + 1, nullPos), StandardCharsets.US_ASCII);
            int size = Integer.parseInt(sizeStr);
            if (size != raw.length - nullPos - 1)
                throw new RuntimeException(String.format("Malformed object %s: bad length", sha));

            return switch (fmt) {
                case "blob" -> new GitBlob(repo, Arrays.copyOfRange(raw, nullPos + 1, raw.length));
                case "commit" -> new GitCommit(repo, Arrays.copyOfRange(raw, nullPos + 1, raw.length));
                default -> throw new RuntimeException("Unknown type %s for object %s".formatted(fmt, sha));
            };

        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }

    }

    public static String writeObject(GitObject object) {
        return writeObject(object, true);
    }

    public static String writeObject(GitObject object, boolean actuallyWrite) {
        byte[] fmt = object.getFmt().getBytes(StandardCharsets.US_ASCII);
        byte[] data = object.serialize();
        byte[] len = String.valueOf(data.length).getBytes(StandardCharsets.US_ASCII);

        byte[] result = new byte[fmt.length + 1 + len.length + 1 + data.length];
        System.arraycopy(fmt, 0, result, 0, fmt.length);
        result[fmt.length] = ' ';
        System.arraycopy(len, 0, result, fmt.length + 1, len.length);
        result[fmt.length + 1 + len.length] = '\0';
        System.arraycopy(data, 0, result, fmt.length + 1 + len.length + 1, data.length);

        String sha = DigestUtils.sha1Hex(result);

        if (actuallyWrite) {
            File file = GitRepository.repoFile(
                    object.repo,
                    true,
                    "objects",
                    sha.substring(0, 2),
                    sha.substring(2)
            );
            try (DeflaterOutputStream os = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                os.write(result);
                os.flush();
            } catch (IOException e) {
                throw new RuntimeException("IO error: " + e.getMessage());
            }
        }

        return sha;

    }

    public static String findObject(GitRepository repo, String name) {
        return name;
    }

}
