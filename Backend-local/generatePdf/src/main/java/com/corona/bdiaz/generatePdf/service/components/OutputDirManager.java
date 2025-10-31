package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.infra.errors.CreatePathException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class OutputDirManager {
    private final Path outputDir;

    public OutputDirManager(Path fileOutputDir) {
        this.outputDir = fileOutputDir;
    }

    public Path ensure() {
        try {
            if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
                throw new IOException("La ruta existe pero no es un directorio: " + outputDir);
            }
            Files.createDirectories(outputDir);
            return outputDir;
        } catch (IOException e) {
            throw new CreatePathException("No se pudo preparar el directorio: " + outputDir);
        }
    }
}

