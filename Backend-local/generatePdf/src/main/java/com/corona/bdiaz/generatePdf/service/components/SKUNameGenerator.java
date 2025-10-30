package com.corona.bdiaz.generatePdf.service.components;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.domain.ResponseFromProcessedFiles;

import java.util.Map;

public interface SKUNameGenerator {
    ResponseFromProcessedFiles generateFiles(NameDocumentGenerateRequest request);
    String getSupportedExtension();
}
