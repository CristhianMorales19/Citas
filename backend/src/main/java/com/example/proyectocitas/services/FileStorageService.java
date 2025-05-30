package com.example.proyectocitas.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir, "profile-photos")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("FileStorageService initialized. Upload directory: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio para almacenar los archivos subidos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalizar nombre del archivo
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Verificar caracteres inválidos en el nombre del archivo
        if (originalFileName.contains("..")) {
            throw new RuntimeException("El nombre del archivo contiene caracteres inválidos " + originalFileName);
        }
        
        // Generar un nombre único para el archivo para evitar sobreescrituras
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Guardar el archivo en el sistema de archivos
        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Devolver el path relativo del archivo
            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + originalFileName, ex);
        }
    }
    
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
}
