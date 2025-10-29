package com.corona.bdiaz.generatePdf.service;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import org.springframework.stereotype.Component;

@Component
public class SKUNameGeneratorJPEG implements SKUNameGenerator{
    @Override
    public void generateFiles(NameDocumentGenerateRequest request){
        System.out.println("Realizando jpeg.");
    }
    @Override
    public String getSupportedExtension(){
        return "jpeg";
    }
}

