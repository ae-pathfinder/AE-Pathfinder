package com.pathfinder.service;

import com.pathfinder.model.*;
import com.pathfinder.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StellenmerklisteService {

    private final StellenmerklisteRepository repository;
    private final NachwuchskraftRepository nwkRepository;
    private final StelleRepository stelleRepository;

    public StellenmerklisteService(StellenmerklisteRepository repository,
                                   NachwuchskraftRepository nwkRepository,
                                   StelleRepository stelleRepository) {
        this.repository = repository;
        this.nwkRepository = nwkRepository;
        this.stelleRepository = stelleRepository;
    }

    public List<Stellenmerkliste> getByNachwuchskraft(Long id) {
        if (id == null) throw new IllegalArgumentException("Nachwuchskraft-ID fehlt");
        return repository.findByNachwuchskraftId(id);
    }

    // Validiertes Löschen
    public boolean deleteByNachwuchskraftAndStelle(Long nwkId, Long stelleId) {
        if (nwkId == null || stelleId == null)
            throw new IllegalArgumentException("IDs fehlen beim Löschen");

        StellenmerklisteId id = new StellenmerklisteId(nwkId, stelleId);
        Stellenmerkliste eintrag = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Eintrag nicht gefunden"));

        // Zugriff nur wenn NWK-Eigentümer
        if (!eintrag.getNachwuchskraft().getId().equals(nwkId)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    public Stellenmerkliste save(Stellenmerkliste eintrag) {
        return repository.save(eintrag);
    }

    public Stellenmerkliste addMerkliste(Long nwkId, Long stellenId) {
        if (nwkId == null || stellenId == null) {
            throw new IllegalArgumentException("Nachwuchskraft-ID oder Stellen-ID fehlt");
        }

        // Prüfen, ob Eintrag schon existiert
        StellenmerklisteId id = new StellenmerklisteId(nwkId, stellenId);
        if(repository.existsById(id)) {
            throw new IllegalArgumentException("Stelle ist bereits gemerkt");
        }

        // Nachwuchskraft abrufen
        Nachwuchskraft nwk = nwkRepository.findById(nwkId)
                .orElseThrow(() -> new IllegalArgumentException("Nachwuchskraft mit ID " + nwkId + " nicht gefunden"));

        // Stelle abrufen
        Stelle stelle = stelleRepository.findById(stellenId)
                .orElseThrow(() -> new IllegalArgumentException("Stelle mit ID " + stellenId + " nicht gefunden"));

        // Neuen Merkliste-Eintrag erstellen
        Stellenmerkliste eintrag = new Stellenmerkliste();
        eintrag.setId(id); // Wichtig für EmbeddedId
        eintrag.setNachwuchskraft(nwk);
        eintrag.setStelle(stelle);
        eintrag.setMerkenStatus(true); // Optional, default ist true
        eintrag.setErstelltAm(LocalDateTime.now());

        return repository.save(eintrag);
    }
}
