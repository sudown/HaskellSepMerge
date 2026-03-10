package br.com.hasksepmerge;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Validação de argumentos, exatamente como o script bash original
        if (args.length != 3) {
            System.err.println("Uso: java -jar haskell-sepmerge.jar <BASE> <LEFT> <RIGHT>");
            System.exit(1);
        }

        String basePath = args[0];
        String leftPath = args[1];
        String rightPath = args[2];

        try {
            // Verifica se todos os arquivos existem antes de tentar o merge
            validateFileExists(basePath);
            validateFileExists(leftPath);
            validateFileExists(rightPath);

            // 1. O Motor invoca o diff3 original na íntegra
            List<String> diff3RawOutput = Diff3Runner.runDiff3(leftPath, basePath, rightPath);

            // 2. A Engine processa focalizadamente apenas os blocos de conflito
            List<String> finalOutput = HaskellSepMergeEngine.resolveConflicts(diff3RawOutput);

            // 3. Imprime o resultado final (o script Python irá redirecionar isso para um arquivo via '>')
            for (String line : finalOutput) {
                System.out.println(line);
            }

        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] Falha ao processar o merge: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void validateFileExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("Arquivo não encontrado: " + filePath);
        }
    }
}