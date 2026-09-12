package com.eriapro.gestionDesAvis.service;

import org.springframework.data.domain.Page;
import com.eriapro.gestionDesAvis.DTO.avisDTO.AvisRequestDTO;
import com.eriapro.gestionDesAvis.entite.Avis;

public interface AvisServise {
    Page<Avis> mesAvis(int page, int size, String sortBy, String direction);
    Page<Avis> tousLesAvis(int page, int size, String sortBy, String direction);
    void supprimer(Long id);
    Avis modifier(Long id, AvisRequestDTO dto);
    Avis creer(AvisRequestDTO dto);
}
