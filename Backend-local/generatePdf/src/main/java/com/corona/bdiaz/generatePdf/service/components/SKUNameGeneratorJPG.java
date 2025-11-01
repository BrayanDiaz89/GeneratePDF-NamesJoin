package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.domain.ResponseFromProcessedFiles;
import com.corona.bdiaz.generatePdf.infra.errors.CreatePathException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Component
public class SKUNameGeneratorJPG implements SKUNameGenerator {

    private final OutputDirManager dirManager;

    public SKUNameGeneratorJPG(OutputDirManager dirManager) {
        this.dirManager = dirManager;
    }

    @Override
    public ResponseFromProcessedFiles generateFiles(NameDocumentGenerateRequest request) {

        var file = request.file();
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Debe adjuntar un archivo.");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .orElse("image.jpeg");

        String originalExt = originalName.toLowerCase().matches(".*\\.jpe?g$")
                ? originalName.substring(originalName.lastIndexOf('.') + 1)
                : "jpeg";

        String baseName = originalName.replaceFirst("(?i)[.](jpe?g)$", "");

        int groupSize = Optional.ofNullable(request.numberOfNamesPerFile()).orElse(5);
        if (groupSize <= 0) groupSize = 5;

        char separator = Optional.ofNullable(request.typeSeparator()).orElse('-');

        List<String> skus = Arrays.stream(
                        Optional.ofNullable(request.skus()).orElse("")
                                .split("[,\\n;]+")
                )
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (skus.isEmpty()) {
            throw new ValidationException("La lista de SKUs está vacía.");
        }

        final byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ValidationException("No se pudo leer el archivo subido.");
        }

        Path outputDir = dirManager.createDirectory();

        List<String> generatedFiles = new ArrayList<>();

        for (int i = 0; i < skus.size(); i += groupSize) {

            List<String> batch = skus.subList(i, Math.min(i + groupSize, skus.size()));

            String prefix = String.join(String.valueOf(separator), batch)
                    .replaceAll("[^a-zA-Z0-9\\-,.&]", "_");

            if (prefix.length() > 150) prefix = prefix.substring(0, 150);

            String filename = prefix + "-" + baseName + "." + originalExt;
            Path destination = resolveValidFilename(outputDir, filename, prefix, baseName, originalExt);

            try {
                Files.write(destination, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                generatedFiles.add(destination.getFileName().toString());
            } catch (IOException e) {
                throw new CreatePathException("Error guardando el archivo: " + destination);
            }
        }

        return new ResponseFromProcessedFiles(
                generatedFiles.size(),
                outputDir.toString(),
                generatedFiles
        );
    }

    private Path resolveValidFilename(Path dir, String filename, String prefix, String base, String ext) {
        Path dest = dir.resolve(filename);

        if (dest.toString().length() > 255) {
            int exceso = dest.toString().length() - 255;

            prefix = prefix.length() > exceso
                    ? prefix.substring(0, prefix.length() - exceso)
                    : prefix.substring(0, Math.max(0, prefix.length() - 50));

            filename = prefix + "-" + base + "." + ext;
            dest = dir.resolve(filename);
        }

        return dest;
    }

    @Override
    public String getSupportedExtension() {
        return "jpg";
    }
}


