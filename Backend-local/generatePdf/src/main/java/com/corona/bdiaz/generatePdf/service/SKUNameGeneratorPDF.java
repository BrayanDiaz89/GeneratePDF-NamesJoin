package com.corona.bdiaz.generatePdf.service;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import org.springframework.stereotype.Component;

@Component
public class SKUNameGeneratorPDF implements SKUNameGenerator{
    @Override
    public void generateFiles(NameDocumentGenerateRequest request){
        System.out.println("Realizando pdf");
    }

    @Override
    public String getSupportedExtension() {
        return "pdf";
    }
}