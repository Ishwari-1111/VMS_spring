package vms.controller;

import vms.model.Certificate;
import vms.dto.CertificateResponse;
import vms.service.CertificateService;
import vms.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    
    @Autowired
    private CertificateService certificateService;

    @Autowired
    private EventService eventService;

    /**
     * Get all certificates
     * GET /api/certificates
     */
    @GetMapping
    public ResponseEntity<?> getAllCertificates() {
        try {
            eventService.publishCertificatesForPastEvents();
            List<Certificate> certificates = certificateService.getAllCertificates();
            List<CertificateResponse> responses = certificates.stream()
                .map(CertificateResponse::fromCertificate)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("certificateCount", responses.size());
            response.put("certificates", responses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("success", false, "error", "Internal server error: " + e.getMessage())
            );
        }
    }
    
    /**
     * AUTOMATED: Generate certificates for all volunteers in an event
     * This triggers when an event is marked as completed
     * POST /api/certificates/generate/event/{eventId}
     */
    @PostMapping(value = "/generate/event/{eventId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> generateCertificatesForEvent(@PathVariable String eventId) {
        try {
            List<Certificate> certificates = certificateService.generateCertificatesForEvent(eventId);
            List<CertificateResponse> responses = certificates.stream()
                .map(CertificateResponse::fromCertificate)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificates generated successfully");
            response.put("certificatesGenerated", responses.size());
            response.put("certificates", responses);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("success", false, "error", "Internal server error: " + e.getMessage())
            );
        }
    }
    
    /**
     * Generate certificate for a specific volunteer for a specific event
     * POST /api/certificates/generate/{volunteerId}/{eventId}
     */
    @PostMapping(value = "/generate/{volunteerId}/{eventId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> generateCertificateForVolunteer(
            @PathVariable String volunteerId,
            @PathVariable String eventId) {
        try {
            Certificate certificate = certificateService.generateCertificateForVolunteer(volunteerId, eventId);
            CertificateResponse response = CertificateResponse.fromCertificate(certificate);
            
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "Certificate generated successfully");
            body.put("certificate", response);
            
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("success", false, "error", "Internal server error: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get all certificates for a volunteer
     * GET /api/certificates/volunteer/{volunteerId}
     */
    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<?> getCertificatesByVolunteer(@PathVariable String volunteerId) {
        try {
            eventService.publishCertificatesForPastEvents();
            List<Certificate> certificates = certificateService.getCertificatesByVolunteer(volunteerId);
            List<CertificateResponse> responses = certificates.stream()
                .map(CertificateResponse::fromCertificate)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("volunteerId", volunteerId);
            response.put("certificateCount", responses.size());
            response.put("certificates", responses);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    /**
     * Get all certificates generated for an event
     * GET /api/certificates/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getCertificatesByEvent(@PathVariable String eventId) {
        try {
            eventService.publishCertificatesForPastEvents();
            List<Certificate> certificates = certificateService.getCertificatesByEvent(eventId);
            List<CertificateResponse> responses = certificates.stream()
                .map(CertificateResponse::fromCertificate)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventId", eventId);
            response.put("certificateCount", responses.size());
            response.put("certificates", responses);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    /**
     * Get certificate count for an event
     * GET /api/certificates/event/{eventId}/count
     */
    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<?> getCertificateCountForEvent(@PathVariable String eventId) {
        try {
            long count = certificateService.getCertificateCountForEvent(eventId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventId", eventId);
            response.put("certificateCount", count);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    /**
     * Get specific certificate for a volunteer-event pair
     * GET /api/certificates/for-volunteer/{volunteerId}/event/{eventId}
     */
    @GetMapping("/for-volunteer/{volunteerId}/event/{eventId}")
    public ResponseEntity<?> getCertificateForVolunteerEvent(
            @PathVariable String volunteerId,
            @PathVariable String eventId) {
        try {
            Certificate certificate = certificateService.getCertificateForVolunteerEvent(volunteerId, eventId);
            CertificateResponse response = CertificateResponse.fromCertificate(certificate);
            
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("certificate", response);
            
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    /**
     * Get certificate by ID
     * GET /api/certificates/id/{certificateId}
     */
    @GetMapping("/id/{certificateId}")
    public ResponseEntity<?> getCertificateById(@PathVariable String certificateId) {
        try {
            Certificate certificate = certificateService.getCertificateById(certificateId);
            CertificateResponse response = CertificateResponse.fromCertificate(certificate);
            
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("certificate", response);
            
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    /**
     * Verify certificate by code (for authenticity check)
     * GET /api/certificates/verify/{certificateCode}
     */
    @GetMapping("/verify/{certificateCode}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String certificateCode) {
        try {
            boolean isValid = certificateService.verifyCertificate(certificateCode);
            
            if (isValid) {
                Certificate certificate = certificateService.getCertificateByCode(certificateCode);
                CertificateResponse certResponse = CertificateResponse.fromCertificate(certificate);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("isValid", true);
                response.put("message", "Certificate is authentic");
                response.put("certificate", certResponse);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "isValid", false, "message", "Invalid certificate code")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}
