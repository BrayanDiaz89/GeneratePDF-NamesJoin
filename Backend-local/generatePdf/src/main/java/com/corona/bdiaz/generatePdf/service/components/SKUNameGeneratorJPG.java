package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component
public class SKUNameGeneratorJPG implements SKUNameGenerator {

    private final Path outputDir;

    public SKUNameGeneratorJPG(Path pdfOutputDir) {
        this.outputDir = pdfOutputDir;
    }

    @Override
    public Map<String, Object> generateFiles(NameDocumentGenerateRequest request) {
        String originalName = Optional.ofNullable(request.file().getOriginalFilename())
                .orElse("image.jpeg");
        String originalExt = Optional.ofNullable(getExt(originalName)).orElse("jpeg").toLowerCase();

        if (!(originalExt.equals("jpeg") || originalExt.equals("jpg"))) {
            throw new ValidationException("El generador JPEG solo acepta archivos .jpg o .jpeg. Recibido: " + originalExt);
        }

        String baseName = originalName.replaceFirst("(?i)[.](jpe?g)$", "");
        int groupSize = Optional.ofNullable(request.numberOfNamesPerPdf()).orElse(10);
        if (groupSize <= 0) groupSize = 10;

        char separator = Optional.ofNullable(request.typeSeparator()).orElse('-');

        List<String> skus = Optional.ofNullable(request.skus()).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (skus.isEmpty()) {
            throw new ValidationException("La lista de SKUs está vacía.");
        }

        final byte[] bytes;
        try {
            bytes = request.file().getBytes();
        } catch (IOException e) {
            throw new ValidationException("No se pudo leer el archivo subido.");
        }

        List<String> generatedFiles = new ArrayList<>();

        for (int i = 0; i < skus.size(); i += groupSize) {
            List<String> batch = skus.subList(i, Math.min(i + groupSize, skus.size()));

            String prefixRaw = String.join(String.valueOf(separator), batch);
            String prefix = prefixRaw.replaceAll("[^a-zA-Z0-9\\-]", "_");
            if (prefix.length() > 150) prefix = prefix.substring(0, 150);

            String filename = prefix + "-" + baseName + "." + originalExt;
            Path destination = outputDir.resolve(filename);

            if (destination.toString().length() > 255) {
                int exceso = destination.toString().length() - 255;
                if (exceso < prefix.length()) {
                    prefix = prefix.substring(0, prefix.length() - exceso);
                } else {
                    prefix = prefix.substring(0, Math.max(0, prefix.length() - 50));
                }
                filename = prefix + "-" + baseName + "." + originalExt;
                destination = outputDir.resolve(filename);
            }

            try {
                Files.write(destination, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                generatedFiles.add(destination.toAbsolutePath().toString());
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo: " + destination, e);
            }
        }

        return Map.of(
                "filesCreated", generatedFiles.size(),
                "outputDir", outputDir.toString(),
                "files", generatedFiles
        );
    }

    private String getExt(String name) {
        int idx = name.lastIndexOf('.');
        return (idx >= 0 && idx < name.length() - 1) ? name.substring(idx + 1) : null;
    }

    @Override
    public String getSupportedExtension() {
        return "jpg";
    }
}

