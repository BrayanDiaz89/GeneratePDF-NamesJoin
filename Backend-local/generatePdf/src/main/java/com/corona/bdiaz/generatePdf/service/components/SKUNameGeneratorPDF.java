package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.domain.ResponseFromProcessedFiles;
import com.corona.bdiaz.generatePdf.infra.errors.ValidationException;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;


@Component
public class SKUNameGeneratorPDF implements SKUNameGenerator {

    private final OutputDirManager dirManager;

    public SKUNameGeneratorPDF(OutputDirManager dirManager) {
        this.dirManager = dirManager;
    }

    @Override
    public ResponseFromProcessedFiles generateFiles(NameDocumentGenerateRequest request) {

        String originalName = Optional.ofNullable(request.file().getOriginalFilename())
                .orElse("document.pdf");
        String baseName = originalName.replaceFirst("(?i)[.]pdf$", "");

        int groupSize = Optional.ofNullable(request.numberOfNamesPerFile()).orElse(5);

        char separator = Optional.ofNullable(request.typeSeparator()).orElse('-');

        List<String> skus = Arrays.stream(request.skus().split("[,\\n;]+"))
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
        final Path OUTPUT_DIR = dirManager.ensure();

        for (int i = 0; i < skus.size(); i += groupSize) {

            List<String> batch = skus.subList(i, Math.min(i + groupSize, skus.size()));

            String prefixRaw = String.join(String.valueOf(separator), batch);
            String prefix = prefixRaw.replaceAll("[^a-zA-Z0-9\\-,.&]", "_");
            if (prefix.length() > 150) prefix = prefix.substring(0, 150);

            String filename = prefix + "-" + baseName + ".pdf";
            Path destination = OUTPUT_DIR.resolve(filename);

            if (destination.toString().length() > 255) {
                prefix = prefix.substring(0, Math.max(0, prefix.length() - 50));
                filename = prefix + "-" + baseName + ".pdf";
                destination = OUTPUT_DIR.resolve(filename);
            }

            try {
                Files.write(destination, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                generatedFiles.add(destination.getFileName().toString());
            } catch (IOException e) {
                throw new RuntimeException("Error guardando archivo.");
            }
        }

        return new ResponseFromProcessedFiles(
                generatedFiles.size(),
                OUTPUT_DIR.toString(),
                generatedFiles
        );
    }

    @Override
    public String getSupportedExtension() {
        return "pdf";
    }
}
