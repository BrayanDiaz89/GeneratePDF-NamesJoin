package com.corona.bdiaz.generatePdf.controller;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.service.SKUNameGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/generate")
public class SKUNameGeneratorController {

    private final SKUNameGeneratorService service;

    public SKUNameGeneratorController(SKUNameGeneratorService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity generateFiles(@ModelAttribute NameDocumentGenerateRequest request){
        service.generateFiles(request);
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }
}
