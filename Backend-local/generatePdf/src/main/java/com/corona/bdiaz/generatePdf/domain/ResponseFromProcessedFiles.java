package com.corona.bdiaz.generatePdf.domain;

import java.util.List;

public record ResponseFromProcessedFiles(
    Integer filesCreated,
    String outputDir,
    List<String> files
) {
}
