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
        // 1. Limpeza do fantasma do Windows (\r)
        String result = mergedCodeBlock.replace("\r", "");

        // 2. A Mágica da Reversão Literal (INTOCÁVEL - Garante a Sintaxe do Haskell)
        for (String sep : SEPARATORS_LIST) {
            result = result.replace("\n" + sep + "\n", sep);
        }

        // 3. PASSO COSMÉTICO: Garantir a formatação dos marcadores para as IDEs (VS Code)
        // a) Se o marcador colou à direita de um código (ex: =<<<<<<<), empurra o marcador para a linha de baixo
        result = result.replaceAll("([^\\n])(<<<<<<<|\\|\\|\\|\\|\\|\\|\\||=======|>>>>>>>)", "$1\n$2");

        // b) Se o código colou à direita de um marcador, empurra o código para a linha de baixo.
        // O ======= é fixo e fácil de tratar:
        result = result.replaceAll("(?m)^(=======)([^\\n])", "$1\n$2");
        // Os outros terminam obrigatoriamente com o nome do nosso arquivo temporário (.hs):
        result = result.replaceAll("(?m)^(<<<<<<<.*?\\.hs|\\|\\|\\|\\|\\|\\|\\|.*?\\.hs|>>>>>>>.*?\\.hs)([^\\n])", "$1\n$2");

        return result;
    }

    // Helper para identificar se a linha é uma marcação pura do diff3
    private static boolean isConflictMarker(String line) {
        return line.startsWith("<<<<<<<") || line.startsWith("|||||||") ||
                line.startsWith("=======") || line.startsWith(">>>>>>>");
    }
}