package com.bkb.scanner.service;

import com.bkb.scanner.entity.Document;
import com.bkb.scanner.entity.User;
import com.bkb.scanner.repository.CaseRepository;
import com.bkb.scanner.repository.DocumentRepository;
import com.bkb.scanner.repository.PartyRepository;
import com.bkb.scanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired private UserRepository userRepository;

    @Value("${naps2.path.windows}")
    private String naps2PathWindows;

    @Value("${naps2.path.mac}")
    private String naps2PathMac;

    private int getNextVersionNumber(String ownerType, String ownerId, String documentType) {
        List<Document> existingDocs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                ownerType, ownerId, documentType
        );

        if (existingDocs.isEmpty()) {
            return 1;
        }

        int maxVersion = existingDocs.stream()
                .mapToInt(Document::getVersion)
                .max()
                .orElse(0);

        System.out.println("Found " + existingDocs.size() + " existing versions, max version: " + maxVersion);
        return maxVersion + 1;
    }

    @Transactional
    public Document triggerNaps2Scan(String profileName, String ownerType, String ownerId,
                                     String documentType, String format) throws IOException, InterruptedException {

        if ("CASE".equalsIgnoreCase(ownerType) && !caseRepository.existsById(ownerId)) {
            throw new RuntimeException("Case not found with id: " + ownerId);
        } else if ("PARTY".equalsIgnoreCase(ownerType) && !partyRepository.existsById(ownerId)) {
            throw new RuntimeException("Party not found with id: " + ownerId);
        }

        // Get current user
        User currentUser = getCurrentUser();

        String tempDir = System.getProperty("java.io.tmpdir");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String fileExtension = (format != null && format.equalsIgnoreCase("png")) ? ".png" : ".pdf";
        String fileName = "scan_" + ownerId + "_" + timestamp + fileExtension;
        Path outputFilePath = Paths.get(tempDir, fileName);

        List<String> command = new ArrayList<>();
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            command.add(naps2PathWindows);
        } else {
            command.add(naps2PathMac);
            command.add("console");
        }

        command.add("-p");
        command.add(profileName);
        command.add("-o");
        command.add(outputFilePath.toString());
        command.add("--force");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println("Executing command: " + String.join(" ", processBuilder.command()));
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            InputStream errorStream = process.getErrorStream();
            String errorDetails;
            try (Scanner s = new Scanner(errorStream).useDelimiter("\\A")) {
                errorDetails = s.hasNext() ? s.next() : "No details available.";
            }
            throw new IOException("NAPS2 process failed with exit code " + exitCode + ". Details: " + errorDetails);
        }

        byte[] fileContent = Files.readAllBytes(outputFilePath);

        Document doc = new Document();
        doc.setName(documentType);
        doc.setOwnerType(ownerType.toUpperCase());
        doc.setOwnerId(ownerId);
        doc.setDocumentType(documentType);
        doc.setOriginalFilename(fileName);
        doc.setMimeType(format != null && format.equalsIgnoreCase("png") ? "image/png" : "application/pdf");
        doc.setSizeInBytes(fileContent.length);
        doc.setContent(fileContent);
        doc.setStatus("Submitted");
        doc.setUploadedByUser(currentUser);  // Changed from setUploadedBy(String)

        int nextVersion = getNextVersionNumber(ownerType.toUpperCase(), ownerId, documentType);
        doc.setVersion(nextVersion);

        doc.setIsCurrentForCase(true);

        if ("CASE".equalsIgnoreCase(ownerType)) {
            doc.setOwnerCase(caseRepository.getReferenceById(ownerId));
        } else {
            doc.setOwnerParty(partyRepository.getReferenceById(ownerId));
        }

        Document savedDoc = documentRepository.save(doc);

        Files.delete(outputFilePath);

        return savedDoc;
    }

    /**
     * Get current user from Spring Security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }
        throw new RuntimeException("No authenticated user found");
    }
}