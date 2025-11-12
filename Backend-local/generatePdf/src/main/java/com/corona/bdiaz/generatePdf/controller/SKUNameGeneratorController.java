package com.corona.bdiaz.generatePdf.controller;

import com.corona.bdiaz.generatePdf.domain.NameDocumentGenerateRequest;
import com.corona.bdiaz.generatePdf.domain.PdfToImageRequest;
import com.corona.bdiaz.generatePdf.domain.PdfToImageResponse;
import com.corona.bdiaz.generatePdf.domain.ResponseFromProcessedFiles;
import com.corona.bdiaz.generatePdf.service.PdfService;
import com.corona.bdiaz.generatePdf.service.SKUNameGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping
public class SKUNameGeneratorController {

    private final SKUNameGeneratorService service;
    private final PdfService pdfService;

    public SKUNameGeneratorController(SKUNameGeneratorService service, PdfService pdfService) {
        this.service = service;
        this.pdfService = pdfService;
    }

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseFromProcessedFiles> generate(@ModelAttribute @Valid NameDocumentGenerateRequest request) {
        return ResponseEntity.ok(service.generateFiles(request));
    }

    @PostMapping(value = "/first-image-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PdfToImageResponse> generate(@ModelAttribute @Valid PdfToImageRequest request) {
        return ResponseEntity.ok(pdfService.processedPdfToFirstImage(request));
    }
}

