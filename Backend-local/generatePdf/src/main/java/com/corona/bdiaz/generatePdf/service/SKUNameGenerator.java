package com.corona.bdiaz.generatePdf.service;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;

public interface SKUNameGenerator {
    void generateFiles(NameDocumentGenerateRequest request);
    String getSupportedExtension();
}
