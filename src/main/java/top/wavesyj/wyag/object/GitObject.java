package top.wavesyj.wyag.object;

import org.apache.commons.codec.digest.DigestUtils;
import top.wavesyj.wyag.util.RefUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                case "tag" -> new GitTag(repo, Arrays.copyOfRange(raw, nullPos + 1, raw.length));
                case "tree" -> new GitTree(repo, Arrays.copyOfRange(raw, nullPos + 1, raw.length));
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

    private static List<String> resolveObject(GitRepository repo, String name) {
        List<String> candidates = new ArrayList<>();
        if (name == null || "".equals(name.strip()))
            return null;

        if ("HEAD".equals(name))
            return Collections.singletonList(RefUtil.resolveRef(repo, "HEAD"));
        if (name.matches("^[\\dA-Fa-f]{4,40}$")) {
            name = name.toLowerCase();
            if (name.length() == 40)
                return Collections.singletonList(name);
            String prefix = name.substring(0, 2);
            String path = GitRepository.repoDirectory(repo, false, "objects", prefix);
            if (path != null) {
                String rem = name.substring(2);
                File[] files = new File(rem).listFiles();
                if (files == null)
                    return null;
                for (File f : files)
                    if (f.getName().startsWith(rem))
                        candidates.add(prefix + f.getName());
            }
        }
        return candidates;
    }

    public static String findObject(GitRepository repo, String name, String fmt, boolean follow) {
        List<String> shas = resolveObject(repo, name);
        if (shas == null)
            throw new RuntimeException("No such reference %s.".formatted(name));
        if (shas.size() > 1)
            throw new RuntimeException("Ambiguous reference %s: Candidates are:\n - %s".formatted(
                    name,
                    String.join("\n - ", shas)
            ));
        String sha = shas.get(0);
        if (fmt == null || "".equals(fmt))
            return sha;
        while (true) {
            GitObject obj = GitObject.readObject(repo, sha);
            if (fmt.equals(obj.getFmt()))
                return sha;
            if (!follow)
                return null;


            if (Type.tag.name().equals(obj.getFmt()))
                sha = new String(((GitTag) obj).getList().get("object").get(0));
            else if (Type.commit.name().equals(obj.getFmt()) && "tree".equals(fmt))
                sha = new String(((GitTag) obj).getList().get("tree").get(0));
            else
                return null;
        }
    }

    public static String findObject(GitRepository repo, String name) {
        return findObject(repo, name, null, true);
    }

}
