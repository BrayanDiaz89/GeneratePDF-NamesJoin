package com.corona.bdiaz.generatePdf.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {
    @Bean
    public Path pdfOutputDir(StorageProperties props) throws IOException {
        Path path = props.getOutputDir();
        Files.createDirectories(path);
        return path;
    }
}
