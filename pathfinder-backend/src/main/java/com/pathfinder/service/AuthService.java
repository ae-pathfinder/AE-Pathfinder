package com.pathfinder.service;

import com.pathfinder.dto.LoginRequestDTO;
import com.pathfinder.dto.LoginResponseDTO;
import com.pathfinder.exception.EmailNotFoundException;
import com.pathfinder.exception.WrongPasswordException;
import com.pathfinder.model.Nachwuchskraft;
import com.pathfinder.repository.NachwuchskraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NachwuchskraftRepository repo;

    public LoginResponseDTO login(LoginRequestDTO req) {

        Nachwuchskraft nwk = repo.findByEmail(req.email())
                .orElseThrow(() -> new EmailNotFoundException(req.email()));

        // Passwort check (Demo)
        if (!req.password().equals("pass123")) {
            throw new WrongPasswordException();
        }

        return new LoginResponseDTO(
                nwk.getId(),
                nwk.getVorname(),
                nwk.getNachname(),
                nwk.getEmail(),
                nwk.getJahrgang()
        );
    }
}
