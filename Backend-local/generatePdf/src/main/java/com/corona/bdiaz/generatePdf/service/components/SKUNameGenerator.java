package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import java.util.Map;

public interface SKUNameGenerator {
    Map<String, Object> generateFiles(NameDocumentGenerateRequest request);
    String getSupportedExtension();
}
