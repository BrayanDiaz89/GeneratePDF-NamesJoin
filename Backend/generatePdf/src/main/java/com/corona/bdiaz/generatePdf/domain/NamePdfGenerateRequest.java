package com.corona.bdiaz.generatePdf.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record NamePdfGenerateRequest(
        List<String> skus,
        MultipartFile pdfFile,
        Character typeSeparator,
        Integer numberOfNamesPerPdf
) {
}
