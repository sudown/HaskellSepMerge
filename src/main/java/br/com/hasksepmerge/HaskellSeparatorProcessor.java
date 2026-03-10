package br.com.hasksepmerge;
import java.util.ArrayList;
import java.util.List;

public class HaskellSeparatorProcessor {

    // Regex rigoroso para os separadores do Haskell
    private static final String HASKELL_SEPARATORS = "(::|->|=>|<-|@|=|\\||,|\\(|\\))";

    public static String preprocessCodeBlock(String codeBlock) {
        // Envolve cada separador com uma quebra de linha limpa
        return codeBlock.replaceAll(HASKELL_SEPARATORS, "\n$1\n");
    }

    public static String postprocessCodeBlock(String mergedCodeBlock) {
        // Limpeza rigorosa do fantasma do Windows (\r)
        mergedCodeBlock = mergedCodeBlock.replace("\r", "");
        String[] lines = mergedCodeBlock.split("\n");

        // Usar uma Lista garante que nunca vamos errar a quebra de linha
        List<String> reconstructed = new ArrayList<>();
        boolean expectsGlue = false;

        for (String line : lines) {
            if (line.isEmpty()) continue;

            // Proteção aos marcadores de conflito do diff3
            if (isConflictMarker(line)) {
                reconstructed.add(line);
                expectsGlue = false;
                continue;
            }

            if (line.matches(HASKELL_SEPARATORS)) {
                if (reconstructed.isEmpty()) {
                    reconstructed.add(line);
                } else {
                    // É um separador: pegamos a última linha e colamos ele no final dela
                    int lastIdx = reconstructed.size() - 1;
                    reconstructed.set(lastIdx, reconstructed.get(lastIdx) + line);
                }
                expectsGlue = true;
            } else {
                if (expectsGlue && !reconstructed.isEmpty()) {
                    // A linha anterior era um separador: colamos o código atual direto nela
                    int lastIdx = reconstructed.size() - 1;
                    reconstructed.set(lastIdx, reconstructed.get(lastIdx) + line);
                } else {
                    // Linha de código normal: adicionamos como uma nova linha intacta
                    reconstructed.add(line);
                }
                expectsGlue = false;
            }
        }

        return String.join("\n", reconstructed);
    }

    // --- AQUI ESTÁ O MÉTODO QUE O COMPILADOR ESTAVA SENTINDO FALTA! ---
    // Helper para identificar se a linha é uma marcação pura do diff3
    private static boolean isConflictMarker(String line) {
        return line.startsWith("<<<<<<<") || line.startsWith("|||||||") ||
                line.startsWith("=======") || line.startsWith(">>>>>>>");
    }
}