package com.corona.bdiaz.generatePdf.service;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.infra.errors.ValidationException;
import com.corona.bdiaz.generatePdf.service.components.SKUNameGenerator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SKUNameGeneratorService {

    private final Map<String, SKUNameGenerator> generatorsMap;

    public SKUNameGeneratorService(List<SKUNameGenerator> generators) {
        this.generatorsMap = generators.stream()
                .collect(Collectors.toMap(
                        g -> g.getSupportedExtension().toLowerCase(),
                        g -> g
                ));
    }

    public Map<String, Object> generateFiles(NameDocumentGenerateRequest request) {
        String extension = Optional.ofNullable(
                        FilenameUtils.getExtension(request.file().getOriginalFilename()))
                .orElse("")
                .toLowerCase();

        // alias: trata "jpg" como "jpeg"
        if ("jpeg".equals(extension)) extension = "jpg";

        SKUNameGenerator generator = generatorsMap.get(extension);
        if (generator == null) {
            throw new ValidationException("Extensión no soportada. Soportadas: " + generatorsMap.keySet());
        }
        return generator.generateFiles(request);
    }
}
