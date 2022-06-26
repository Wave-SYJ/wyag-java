package top.wavesyj.wyag.util;

import top.wavesyj.wyag.object.GitRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class RefUtil {

    public static String resolveRef(GitRepository repo, String ref) {
        File file = GitRepository.repoFile(repo, false, ref);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String data = br.readLine();
            if (data.startsWith("ref: "))
                return resolveRef(repo, data.substring(5));
            else
                return data;
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

}
