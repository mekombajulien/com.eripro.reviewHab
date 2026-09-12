package com.eriapro.gestionDesAvis.contoller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisResponseDTO;
import com.eriapro.gestionDesAvis.entite.Avis;
import com.eriapro.gestionDesAvis.mapper.AvisMapper;
import com.eriapro.gestionDesAvis.service.impl.AvisServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/avis")
@RequiredArgsConstructor
public class AvisController {

    private final AvisServiceImpl avisService;

    @PostMapping
    public ResponseEntity<AvisResponseDTO> creer(@Valid @RequestBody AvisRequestDTO dto) {
        Avis avis = avisService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AvisMapper.toResponseDTO(avis));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvisResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody AvisRequestDTO dto
    ) {
        Avis avis = avisService.modifier(id, dto);
        return ResponseEntity.ok(AvisMapper.toResponseDTO(avis));
    }

    @GetMapping("/mes")
    public ResponseEntity<Page<AvisResponseDTO>> mesAvis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<AvisResponseDTO> resultat = avisService
                .mesAvis(page, size, sortBy, direction)
                .map(AvisMapper::toResponseDTO);
        return ResponseEntity.ok(resultat);
    }

    @GetMapping
    public ResponseEntity<Page<AvisResponseDTO>> tousLesAvis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<AvisResponseDTO> resultat = avisService
                .tousLesAvis(page, size, sortBy, direction)
                .map(AvisMapper::toResponseDTO);
        return ResponseEntity.ok(resultat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        avisService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}