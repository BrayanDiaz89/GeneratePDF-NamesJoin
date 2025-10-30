package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.infra.errors.ValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


@Component
public class SKUNameGeneratorPDF implements SKUNameGenerator {

    private final Path outputDir;

    public SKUNameGeneratorPDF(Path pdfOutputDir) {
        this.outputDir = pdfOutputDir;
    }

    @Override
    public Map<String, Object> generateFiles(NameDocumentGenerateRequest request) {

        String originalName = Optional.ofNullable(request.file().getOriginalFilename())
                .orElse("document.pdf");
        String baseName = originalName.replaceFirst("(?i)[.]pdf$", "");

        int groupSize = Optional.ofNullable(request.numberOfNamesPerPdf()).orElse(10);
        if (groupSize <= 0) groupSize = 10;

        char separator = Optional.ofNullable(request.typeSeparator()).orElse('-');

        List<String> skus = Optional.ofNullable(request.skus()).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (skus.isEmpty())
            throw new ValidationException("La lista de SKUs está vacía.");

        byte[] fileBytes;
        try {
            fileBytes = request.file().getBytes();
        } catch (IOException e) {
            throw new ValidationException("No se pudo leer el archivo.");
        }

        List<String> generatedFiles = new ArrayList<>();

        for (int i = 0; i < skus.size(); i += groupSize) {
            List<String> batch = skus.subList(i, Math.min(i + groupSize, skus.size()));

            String prefixRaw = String.join(String.valueOf(separator), batch);
            String prefix = prefixRaw.replaceAll("[^a-zA-Z0-9\\-]", "_");
            if (prefix.length() > 150) prefix = prefix.substring(0, 150);

            String filename = prefix + "-" + baseName + ".pdf";
            Path destination = outputDir.resolve(filename);

            if (destination.toString().length() > 255) {
                prefix = prefix.substring(0, Math.max(0, prefix.length() - 50));
                filename = prefix + "-" + baseName + ".pdf";
                destination = outputDir.resolve(filename);
            }

            try (PDDocument doc = PDDocument.load(fileBytes)) {
                doc.save(destination.toString());
                generatedFiles.add(destination.toAbsolutePath().toString());
            } catch (IOException e) {
                throw new RuntimeException("Error guardando archivo.");
            }
        }

        return Map.of(
                "filesCreated", generatedFiles.size(),
                "outputDir", outputDir.toString(),
                "files", generatedFiles
        );
    }

    @Override
    public String getSupportedExtension() {
        return "pdf";
    }
}
