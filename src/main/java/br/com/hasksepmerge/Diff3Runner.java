package br.com.hasksepmerge;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Diff3Runner {

    // Método que chama o diff3 nativo com a flag --show-all
    public static List<String> runDiff3(String leftPath, String basePath, String rightPath) throws Exception {
        // Monta o comando: diff3 -m --show-all left.hs base.hs right.hs
        ProcessBuilder pb = new ProcessBuilder("diff3", "-m", "--show-all", leftPath, basePath, rightPath);
        Process process = pb.start();

        List<String> diffOutput = new ArrayList<>();

        // Lê a saída do comando linha por linha
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                diffOutput.add(line);
            }
        }

        process.waitFor();
        return diffOutput; // Retorna o arquivo com os marcadores de conflito
    }
}