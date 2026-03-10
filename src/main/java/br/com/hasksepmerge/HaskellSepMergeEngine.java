package br.com.hasksepmerge;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HaskellSepMergeEngine {

    /**
     * Analisa a saída do diff3 principal e aplica o processamento apenas nos blocos de conflito.
     */
    public static List<String> resolveConflicts(List<String> diff3Output) throws Exception {
        List<String> finalOutput = new ArrayList<>();

        List<String> leftBlock = new ArrayList<>();
        List<String> baseBlock = new ArrayList<>();
        List<String> rightBlock = new ArrayList<>();

        // Máquina de estados: 0 = normal, 1 = lendo left, 2 = lendo base, 3 = lendo right
        int state = 0;

        for (String line : diff3Output) {
            if (line.startsWith("<<<<<<<")) {
                state = 1;
                leftBlock.clear();
                baseBlock.clear();
                rightBlock.clear();
            } else if (line.startsWith("|||||||")) {
                state = 2;
            } else if (line.startsWith("=======")) {
                state = 3;
            } else if (line.startsWith(">>>>>>>")) {
                state = 0; // Fim do conflito

                // 1. Convertendo as listas em strings preservando as quebras de linha (com EOF garantido!)
                String leftStr = String.join("\n", leftBlock) + "\n";
                String baseStr = String.join("\n", baseBlock) + "\n";
                String rightStr = String.join("\n", rightBlock) + "\n";

                // 2. Pré-processamento (Onde isolamos os separadores do Haskell)
                leftStr = HaskellSeparatorProcessor.preprocessCodeBlock(leftStr);
                baseStr = HaskellSeparatorProcessor.preprocessCodeBlock(baseStr);
                rightStr = HaskellSeparatorProcessor.preprocessCodeBlock(rightStr);

                // 3. Executamos o diff3 interno apenas neste pequeno bloco [cite: 297]
                String resolvedConflict = runInnerDiff3(leftStr, baseStr, rightStr);

                // 4. Pós-processamento (A sua Estratégia 1: Colagem Plana sem usar .trim())
                String finalConflict = HaskellSeparatorProcessor.postprocessCodeBlock(resolvedConflict);

                // 5. Substituímos o conflito original pelo resultado refinado [cite: 298]
                finalOutput.add(finalConflict);

            } else {
                // Preenchendo os blocos ou adicionando ao arquivo final dependendo do estado
                switch (state) {
                    case 0: finalOutput.add(line); break; // Linha sem conflito, passa direto
                    case 1: leftBlock.add(line); break;
                    case 2: baseBlock.add(line); break;
                    case 3: rightBlock.add(line); break;
                }
            }
        }

        return finalOutput;
    }

    /**
     * Método auxiliar para rodar o diff3 nos blocos temporários criados em memória.
     */
    private static String runInnerDiff3(String left, String base, String right) throws Exception {
        // Criando os arquivos temporários exigidos pelo artigo do SepMerge [cite: 296]
        File tmpLeft = File.createTempFile("left_tmp", ".hs");
        File tmpBase = File.createTempFile("base_tmp", ".hs");
        File tmpRight = File.createTempFile("right_tmp", ".hs");

        Files.writeString(tmpLeft.toPath(), left);
        Files.writeString(tmpBase.toPath(), base);
        Files.writeString(tmpRight.toPath(), right);

        // Invoca a classe Diff3Runner (aquela do passo anterior) para rodar o comando
        List<String> innerOutput = Diff3Runner.runDiff3(
                tmpLeft.getAbsolutePath(),
                tmpBase.getAbsolutePath(),
                tmpRight.getAbsolutePath()
        );

        // Limpeza dos temporários
        tmpLeft.delete();
        tmpBase.delete();
        tmpRight.delete();

        return String.join("\n", innerOutput);
    }
}