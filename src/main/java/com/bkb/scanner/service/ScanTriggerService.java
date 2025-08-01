package com.bkb.scanner.service;

import com.bkb.scanner.entity.Document;
import com.bkb.scanner.repository.CaseRepository;
import com.bkb.scanner.repository.DocumentRepository;
import com.bkb.scanner.repository.PartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Service
public class ScanTriggerService {

    @Autowired private DocumentRepository documentRepository;
    @Autowired private CaseRepository caseRepository;
    @Autowired private PartyRepository partyRepository;

    @Value("${naps2.path.windows}")
    private String naps2PathWindows;

    @Value("${naps2.path.mac}")
    private String naps2PathMac;

    @Transactional
    public Document triggerNaps2Scan(String profileName, String ownerType, String ownerId,
                                     String documentType, String format) throws IOException, InterruptedException {

        // 1. Verify the owner (Case or Party) exists
        if ("CASE".equalsIgnoreCase(ownerType) && !caseRepository.existsById(ownerId)) {
            throw new RuntimeException("Case not found with id: " + ownerId);
        } else if ("PARTY".equalsIgnoreCase(ownerType) && !partyRepository.existsById(ownerId)) {
            throw new RuntimeException("Party not found with id: " + ownerId);
        }

        // 2. Prepare the output file path in a temporary directory
        String tempDir = System.getProperty("java.io.tmpdir");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Use format parameter to determine file extension
        String fileExtension = (format != null && format.equalsIgnoreCase("png")) ? ".png" : ".pdf";
        String fileName = "scan_" + ownerId + "_" + timestamp + fileExtension;
        Path outputFilePath = Paths.get(tempDir, fileName);

        // 3. OS-Agnostic Command Building Logic
        List<String> command = new ArrayList<>();
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            command.add(naps2PathWindows);
        } else {
            command.add(naps2PathMac);
            command.add("console");
        }

        // Add the shared NAPS2 arguments
        command.add("-p");
        command.add(profileName);
        command.add("-o");
        command.add(outputFilePath.toString());
        command.add("--force");

        // Add format-specific arguments if PNG
        if (format != null && format.equalsIgnoreCase("png")) {
            command.add("--outputtype");
            command.add("png");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        System.out.println("Executing command: " + String.join(" ", processBuilder.command()));
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        // 4. Check for errors
        if (exitCode != 0) {
            InputStream errorStream = process.getErrorStream();
            String errorDetails;
            try (Scanner s = new Scanner(errorStream).useDelimiter("\\A")) {
                errorDetails = s.hasNext() ? s.next() : "No details available.";
            }
            throw new IOException("NAPS2 process failed with exit code " + exitCode + ". Details: " + errorDetails);
        }

        // 5. Read the scanned file's content
        byte[] fileContent = Files.readAllBytes(outputFilePath);

        // 6. Create and save the Document entity
        Document doc = new Document();
        doc.setName(documentType); // Set the name field
        doc.setOwnerType(ownerType.toUpperCase());
        doc.setOwnerId(ownerId);
        doc.setDocumentType(documentType);
        doc.setOriginalFilename(fileName);
        doc.setMimeType(format != null && format.equalsIgnoreCase("png") ? "image/png" : "application/pdf");
        doc.setSizeInBytes(fileContent.length);
        doc.setContent(fileContent);
        doc.setStatus("Submitted");
        doc.setVersion(1);
        doc.setUploadedBy(getCurrentUsername()); // You'll need to implement this

        if ("CASE".equalsIgnoreCase(ownerType)) {
            doc.setOwnerCase(caseRepository.getReferenceById(ownerId));
        } else {
            doc.setOwnerParty(partyRepository.getReferenceById(ownerId));
        }

        Document savedDoc = documentRepository.save(doc);

        // 7. Clean up the temporary file
        Files.delete(outputFilePath);

        return savedDoc;
    }

    private String getCurrentUsername() {
        // Get from Spring Security context
        // For now, return a default
        return "Scanner";
    }
}