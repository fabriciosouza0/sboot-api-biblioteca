package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.PatronStatus;
import com.escola.biblioteca.domain.repository.PatronRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatronServiceTest {

    @Mock
    private PatronRepository patronRepository;

    @Mock
    private ProfileConfigRepository profileConfigRepository;

    @InjectMocks
    private PatronService patronService;

    private UUID institutionId;
    private UUID patronId;
    private Patron patron;

    @BeforeEach
    void setUp() {
        institutionId = UUID.randomUUID();
        patronId = UUID.randomUUID();

        patron = new Patron();
        patron.setId(patronId);
        patron.setInstitutionId(institutionId);
        patron.setExternalId("12345678901");
        patron.setName("Test User");
        patron.setProfile(PatronProfile.STUDENT);
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(0);
    }

    @Test
    void register_shouldSucceed_whenNoDuplicate() {
        when(patronRepository.findByInstitutionIdAndExternalId(institutionId, "12345678901"))
                .thenReturn(Optional.empty());
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patron result = patronService.register(institutionId, "12345678901", "Test User", "11999999999", PatronProfile.STUDENT);

        assertNotNull(result);
        assertEquals(institutionId, result.getInstitutionId());
        assertEquals("12345678901", result.getExternalId());
        assertEquals(PatronProfile.STUDENT, result.getProfile());
        assertEquals(PatronStatus.ACTIVE, result.getStatus());
        assertEquals(0, result.getFineBalance());
    }

    @Test
    void register_shouldFail_whenDuplicateExternalId() {
        when(patronRepository.findByInstitutionIdAndExternalId(institutionId, "12345678901"))
                .thenReturn(Optional.of(patron));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> patronService.register(institutionId, "12345678901", "Test User", "11999999999", PatronProfile.STUDENT));
        assertEquals("error.patron.duplicate.externalId", ex.getCode());
    }

    @Test
    void findById_shouldReturnPatron_whenExists() {
        when(patronRepository.findById(patronId)).thenReturn(Optional.of(patron));

        Patron result = patronService.findById(patronId);

        assertEquals(patron, result);
    }

    @Test
    void findById_shouldThrow_whenNotExists() {
        when(patronRepository.findById(patronId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patronService.findById(patronId));
    }

    @Test
    void updateProfile_shouldUpdateAndReturn() {
        when(patronRepository.findById(patronId)).thenReturn(Optional.of(patron));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patron result = patronService.updateProfile(patronId, PatronProfile.TEACHER);

        assertEquals(PatronProfile.TEACHER, result.getProfile());
    }

    @Test
    void block_shouldSetStatusBlocked() {
        when(patronRepository.findById(patronId)).thenReturn(Optional.of(patron));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        patronService.block(patronId);

        assertEquals(PatronStatus.BLOCKED, patron.getStatus());
    }

    @Test
    void unblock_shouldSetStatusActive() {
        patron.setStatus(PatronStatus.BLOCKED);
        when(patronRepository.findById(patronId)).thenReturn(Optional.of(patron));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        patronService.unblock(patronId);

        assertEquals(PatronStatus.ACTIVE, patron.getStatus());
    }

    @Test
    void isBlocked_shouldReturnTrue_whenStatusBlocked() {
        patron.setStatus(PatronStatus.BLOCKED);
        assertTrue(patronService.isBlocked(patron));
    }

    @Test
    void isBlocked_shouldReturnTrue_whenFineBalanceOverLimit() {
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(2500);
        assertTrue(patronService.isBlocked(patron));
    }

    @Test
    void isBlocked_shouldReturnFalse_whenActiveAndLowBalance() {
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(1000);
        assertFalse(patronService.isBlocked(patron));
    }
}