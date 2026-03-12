package br.com.hasksepmerge;
import java.util.ArrayList;
import java.util.List;

public class HaskellSeparatorProcessor {

    // Regex rigoroso para o pré-processamento (a ordem importa para pegar => antes de =)
    private static final String HASKELL_SEPARATORS_REGEX = "(::|->|=>|<-|@|=|\\||,|\\(|\\))";

    // Lista exata de separadores para a reversão literal no pós-processamento
    private static final String[] SEPARATORS_LIST = {
            "::", "->", "=>", "<-", "@", "=", "|", ",", "(", ")"
    };

    public static String preprocessCodeBlock(String codeBlock) {
        return codeBlock.replaceAll(HASKELL_SEPARATORS_REGEX, "\n$1\n");
    }

    public static String postprocessCodeBlock(String mergedCodeBlock) {
        String result = mergedCodeBlock.replace("\r", "");

        // O método replace() do Java trabalha com texto literal (não usa Regex).
        // Ele vai varrer o arquivo e desfazer EXATAMENTE a injeção que fizemos no pré-processamento,
        // devolvendo a formatação e os espaçamentos estruturais idênticos aos do código original.
        for (String sep : SEPARATORS_LIST) {
            result = result.replace("\n" + sep + "\n", sep);
        }

        return result;
    }

    // Helper para identificar se a linha é uma marcação pura do diff3
    private static boolean isConflictMarker(String line) {
        return line.startsWith("<<<<<<<") || line.startsWith("|||||||") ||
                line.startsWith("=======") || line.startsWith(">>>>>>>");
    }
}