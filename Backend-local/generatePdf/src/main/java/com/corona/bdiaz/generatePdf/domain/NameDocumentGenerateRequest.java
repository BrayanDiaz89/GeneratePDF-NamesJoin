package com.corona.bdiaz.generatePdf.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record NameDocumentGenerateRequest(
        List<String> skus,
        MultipartFile file,
        Character typeSeparator,
        Integer numberOfNamesPerPdf
) {
}
