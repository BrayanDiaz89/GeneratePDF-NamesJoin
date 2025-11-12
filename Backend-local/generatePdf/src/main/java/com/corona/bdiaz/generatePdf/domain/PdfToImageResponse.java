package com.corona.bdiaz.generatePdf.domain;

public record PdfToImageResponse(
        String outputPath,      // ruta absoluta del jpg
        String outputFileName,  // nombre del archivo
        long outputSizeBytes,   // tamaño final
        int width,              // pixeles
        int height,             // pixeles
        int dpiUsed             // dpi usados para render
) {}
