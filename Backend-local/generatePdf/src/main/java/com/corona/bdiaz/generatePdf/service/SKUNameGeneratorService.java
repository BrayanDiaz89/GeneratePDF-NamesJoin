package com.corona.bdiaz.generatePdf.service;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SKUNameGeneratorService {
    private final Map<String, SKUNameGenerator> generatorsMap;

    public SKUNameGeneratorService(List<SKUNameGenerator> generators) {
        this.generatorsMap = generators.stream()
                .collect(Collectors.toMap(
                        SKUNameGenerator::getSupportedExtension,
                        generator -> generator
                ));
    }

    public void generateFiles(NameDocumentGenerateRequest request){
        String extension = FilenameUtils.getExtension(request.file().getOriginalFilename());
        SKUNameGenerator generator = generatorsMap.get(extension);
        if(generator == null) throw new IllegalArgumentException("Extensión no soportada.");
        generator.generateFiles(request);
    }
}
