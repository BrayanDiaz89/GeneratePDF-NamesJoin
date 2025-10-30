package com.corona.bdiaz.generatePdf.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record NameDocumentGenerateRequest(
        @NotNull(message = "Debes ingresar los SKU, que requieres enlazar.")
        List<String> skus,
        @NotNull(message = "Debes importar el documento que deseas procesar.")
        MultipartFile file,
        @NotNull(message = "Debes indicar un tipo de separador.")
        Character typeSeparator,
        @Max(value = 10, message = "El número de separación de skus, no debe ser menor a 10.")
        @Min(value = 2, message = "Almenos debes separar por 2 SKU cada documento.")
        @NotNull(message = "Debes ingresar un número para separar los SKU por documento.")
        Integer numberOfNamesPerPdf
) {
}
