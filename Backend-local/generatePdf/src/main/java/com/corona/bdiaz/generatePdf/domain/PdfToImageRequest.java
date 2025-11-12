package com.corona.bdiaz.generatePdf.domain;

import org.springframework.web.multipart.MultipartFile;

public record PdfToImageRequest(
        MultipartFile file,
        Integer dpi
) {
}
