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
        String originalName = Optional.ofNullable(request.file().getOriginalFilename())
                .orElse("image.jpeg");
        String originalExt = Optional.ofNullable(getExt(originalName)).orElse("jpeg").toLowerCase();

        String baseName = originalName.replaceFirst("(?i)[.](jpe?g)$", "");
        int groupSize = Optional.ofNullable(request.numberOfNamesPerFile()).orElse(5);

        char separator = Optional.ofNullable(request.typeSeparator()).orElse('-');

        List<String> skus = Arrays.stream(request.skus().split("[,\\n;]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        final byte[] bytes;
        try {
            bytes = request.file().getBytes();
        } catch (IOException e) {
            throw new ValidationException("No se pudo leer el archivo subido.");
        }

        List<String> generatedFiles = new ArrayList<>();
        final Path OUTPUT_DIR = dirManager.ensure();

        for (int i = 0; i < skus.size(); i += groupSize) {
            List<String> batch = skus.subList(i, Math.min(i + groupSize, skus.size()));

            String prefixRaw = String.join(String.valueOf(separator), batch);
            String prefix = prefixRaw.replaceAll("[^a-zA-Z0-9\\-,.&]", "_");
            if (prefix.length() > 150) prefix = prefix.substring(0, 150);

            String filename = prefix + "-" + baseName + "." + originalExt;
            Path destination = OUTPUT_DIR.resolve(filename);

            if (destination.toString().length() > 255) {
                int exceso = destination.toString().length() - 255;
                if (exceso < prefix.length()) {
                    prefix = prefix.substring(0, prefix.length() - exceso);
                } else {
                    prefix = prefix.substring(0, Math.max(0, prefix.length() - 50));
                }
                filename = prefix + "-" + baseName + "." + originalExt;
                destination = OUTPUT_DIR.resolve(filename);
            }

            try {
                Files.write(destination, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                generatedFiles.add(destination.getFileName().toString());
            } catch (IOException e) {
                throw new CreatePathException("Error guardando el archivo: " + destination);
            }
        }

        return new ResponseFromProcessedFiles(
                generatedFiles.size(),
                OUTPUT_DIR.toString(),
                generatedFiles
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

