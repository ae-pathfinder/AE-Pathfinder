package com.pathfinder.service;

import com.pathfinder.exception.InvalidFileTypeException;
import com.pathfinder.exception.NachwuchskraftNotFoundException;
import com.pathfinder.model.NachwuchskraftAnhang;
import com.pathfinder.repository.NachwuchskraftAnhangRepository;
import com.pathfinder.repository.NachwuchskraftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NachwuchskraftAnhangServiceTest {

    private NachwuchskraftAnhangRepository anhangRepository;
    private NachwuchskraftRepository nwkRepository;
    private NachwuchskraftAnhangService service;

    @BeforeEach
    void setup() {
        anhangRepository = mock(NachwuchskraftAnhangRepository.class);
        nwkRepository = mock(NachwuchskraftRepository.class);

        // Dummy-Upload-Ordner für Tests
        service = new NachwuchskraftAnhangService(
                anhangRepository,
                nwkRepository,
                "target/test-uploads"
        );
    }

    @Test
    void getByNachwuchskraft_shouldReturnList() {
        NachwuchskraftAnhang a1 = new NachwuchskraftAnhang();
        NachwuchskraftAnhang a2 = new NachwuchskraftAnhang();

        when(anhangRepository.findByNachwuchskraftId(5L))
                .thenReturn(List.of(a1, a2));

        List<NachwuchskraftAnhang> result = service.getByNachwuchskraft(5L);

        assertEquals(2, result.size());
        verify(anhangRepository).findByNachwuchskraftId(5L);
    }

    @Test
    void storeFile_shouldThrow_whenInvalidFileType() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.exe");
        when(file.getSize()).thenReturn(1000L);

        // Nachwuchskraft existiert
        when(nwkRepository.findById(1L))
                .thenReturn(Optional.of(mock(com.pathfinder.model.Nachwuchskraft.class)));

        assertThrows(
                InvalidFileTypeException.class,
                () -> service.storeFile(
                        file,
                        1L,
                        NachwuchskraftAnhang.DokumentTyp.LEBENSLAUF
                )
        );
    }

    @Test
    void storeFile_shouldThrow_whenNwkNotFound() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getSize()).thenReturn(1000L);

        when(nwkRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NachwuchskraftNotFoundException.class,
                () -> service.storeFile(
                        file,
                        99L,
                        NachwuchskraftAnhang.DokumentTyp.ZEUGNIS
                )
        );
    }
}
