package com.eriapro.gestionDesAvis.service.impl;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.eriapro.gestionDesAvis.StatusAvis;
import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.entite.Avis;
import com.eriapro.gestionDesAvis.entite.User;
import com.eriapro.gestionDesAvis.exception.UpdateAvisRefuseException;
import com.eriapro.gestionDesAvis.mapper.AvisMapper;
import com.eriapro.gestionDesAvis.repository.AvisRepository;
import com.eriapro.gestionDesAvis.service.AvisServise;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvisServiceImpl implements AvisServise {

    private final AvisRepository avisRepository;

    // whitelist des champs triables — empêche une PropertyReferenceException
    // si le client envoie un sortBy invalide ou malveillant
    private static final Set<String> CHAMPS_TRIABLES = Set.of("id", "createdAt", "status");
    private static final String TRI_PAR_DEFAUT = "createdAt";
    private static final int TAILLE_MAX_PAGE = 50; 

    @Override
    public Page<Avis> mesAvis(int page, int size, String sortBy, String direction) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Pageable pageable = construirePageable(page, size, sortBy, direction);
        return avisRepository.findByUserEmail(user.getEmail(), pageable);
    }

    @Override
    public Page<Avis> tousLesAvis(int page, int size, String sortBy, String direction) {
        Pageable pageable = construirePageable(page, size, sortBy, direction);
        return avisRepository.findAll(pageable);
    }

    // méthode privée partagée — évite de dupliquer la logique de validation
    // dans mesAvis() et tousLesAvis()
    private Pageable construirePageable(int page, int size, String sortBy, String direction) {
        String champTri = CHAMPS_TRIABLES.contains(sortBy) ? sortBy : TRI_PAR_DEFAUT;
        Sort.Direction dir = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        int taillePage = Math.min(size, TAILLE_MAX_PAGE);
        int numeroPage = Math.max(page, 0);

        return PageRequest.of(numeroPage, taillePage, Sort.by(dir, champTri));
    }

    @Override
    public void supprimer(Long id) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis introuvable"));
        if (!avis.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez pas supprimer cet avis");
        }
        avisRepository.delete(avis);
    }

    @Override
    public Avis modifier(Long id, AvisRequestDTO dto) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Avis avis = avisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis introuvable"));
        if (!avis.getUser().getId().equals(user.getId())) {
            throw new UpdateAvisRefuseException("Vous ne pouvez pas modifier cet avis");
        }
        avis.setMessage(dto.getMessage());
        avis.setStatus(StatusAvis.EN_ATTENTE);
        return avisRepository.save(avis);
    }

    @Override
    public Avis creer(AvisRequestDTO dto) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Avis avis = AvisMapper.toEntity(dto);
        avis.setUser(user);
        avis.setStatus(StatusAvis.EN_ATTENTE);
        avis.setCreatedAt(Instant.now());
        return avisRepository.save(avis);
    }
}