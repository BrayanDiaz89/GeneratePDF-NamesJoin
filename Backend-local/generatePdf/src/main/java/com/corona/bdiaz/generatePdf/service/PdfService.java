package com.corona.bdiaz.generatePdf.service;
import com.corona.bdiaz.generatePdf.domain.PdfToImageRequest;
import com.corona.bdiaz.generatePdf.domain.PdfToImageResponse;
import com.corona.bdiaz.generatePdf.service.components.OutputDirManager;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class PdfService {

    private final OutputDirManager dirManager;

    public PdfService(OutputDirManager dirManager) {
        this.dirManager = dirManager;
    }

    /** Registra plugins de ImageIO (incluye JPEG2000) una sola vez al iniciar la app. */
    @PostConstruct
    public void registerImageIOPlugins() {
        ImageIO.scanForPlugins();
        // En algunos entornos (contenedores) el auto-scan no detecta JP2; forzamos el registro:
        try {
            IIORegistry.getDefaultInstance().registerServiceProvider(new J2KImageReaderSpi());
        } catch (Throwable ignored) {
            // si ya está registrado o no está en classpath, seguimos (el scan suele bastar)
        }
    }

    /**
     * Renderiza la página 1 completa del PDF a JPG y la guarda en el directorio configurado.
     * Devuelve metadatos del archivo generado.
     */
    public PdfToImageResponse processedPdfToFirstImage(PdfToImageRequest request) {
        // 1) Validación básica
        MultipartFile file = Optional.ofNullable(request).map(PdfToImageRequest::file).orElse(null);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar un PDF.");
        }
        final int dpi = (request.dpi() == null || request.dpi() <= 0) ? 300 : request.dpi();

        // 2) Preparar nombre y directorio
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("document.pdf");
        String baseName = sanitizeBaseName(originalName.replaceFirst("(?i)[.]pdf$", ""));
        Path outDir = dirManager.createDirectory();
        Path destination = resolveNonClashingFilename(outDir, baseName, "jpg"); // cambia a "png" para lossless

        int width, height;

        // 3) Renderizar página completa (MediaBox) con todas las capas activadas y soporte JPEG2000
        //    (ImageIO plugins ya registrados en @PostConstruct)
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            if (doc.isEncrypted()) {
                throw new IllegalArgumentException("El PDF está cifrado/protegido.");
            }
            if (doc.getNumberOfPages() <= 0) {
                throw new IllegalArgumentException("El PDF no contiene páginas.");
            }

            // 3.a) Activar todas las capas OCG
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDOptionalContentProperties oc = catalog.getOCProperties();
            if (oc != null) {
                oc.setBaseState(PDOptionalContentProperties.BaseState.ON);
                for (PDOptionalContentGroup g : oc.getOptionalContentGroups()) {
                    oc.setGroupEnabled(g, true);
                }
            }

            // 3.b) Usar la MediaBox completa (no la CropBox) para evitar recortes
            PDPage page0 = doc.getPage(0);
            PDRectangle media = page0.getMediaBox();
            if (media != null) {
                page0.setCropBox(media);
            }

            // 3.c) Render de alta fidelidad (RGB) y DPI solicitado
            PDFRenderer renderer = new PDFRenderer(doc);
            renderer.setSubsamplingAllowed(false);
            BufferedImage img = renderer.renderImageWithDPI(0, dpi, ImageType.RGB);

            width = img.getWidth();
            height = img.getHeight();

            // 4) Guardar imagen con metadatos de DPI usando ImageIOUtil (pdfbox-tools)
            boolean ok = ImageIOUtil.writeImage(img, destination.toString(), dpi);
            if (!ok) throw new IOException("No fue posible escribir la imagen de salida.");
        } catch (IOException e) {
            throw new RuntimeException("Error procesando el PDF: " + e.getMessage(), e);
        }

        // 5) Metadatos de salida
        long size;
        try { size = Files.size(destination); } catch (IOException e) { size = -1L; }

        return new PdfToImageResponse(
                destination.toAbsolutePath().toString(),
                destination.getFileName().toString(),
                size,
                width,
                height,
                dpi
        );
    }

    // ---------- helpers ----------

    private String sanitizeBaseName(String name) {
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.isEmpty() ? "document" : cleaned;
    }

    /** Evita sobrescribir: agrega (1), (2) ... si ya existe. */
    private Path resolveNonClashingFilename(Path dir, String baseName, String ext) {
        String fn = baseName + "." + ext;
        Path dest = dir.resolve(fn);
        int i = 1;
        while (Files.exists(dest)) {
            fn = baseName + " (" + i + ")." + ext;
            dest = dir.resolve(fn);
            i++;
        }
        if (dest.toString().length() > 255) {
            String shortened = baseName;
            if (shortened.length() > 200) shortened = shortened.substring(0, 200);
            dest = dir.resolve(shortened + "." + ext);
        }
        return dest;
    }
}