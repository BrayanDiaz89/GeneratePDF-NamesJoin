package com.corona.bdiaz.generatePdf.domain;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record NameDocumentGenerateRequest(
        @NotEmpty(message = "Debes ingresar los SKU, que requieres enlazar.")
        String skus,
        @NotNull(message = "Debes importar el documento que deseas procesar.")
        MultipartFile file,
        @NotNull(message = "Debes indicar un tipo de separador.")
        Character typeSeparator,
        @Max(value = 5, message = "El número de separación de skus, debe ser menor o igual a 5.")
        @Min(value = 2, message = "Almenos debes separar por 2 SKU cada documento.")
        @NotNull(message = "Debes ingresar un número para separar los SKU por documento.")
        Integer numberOfNamesPerFile
) {
}
