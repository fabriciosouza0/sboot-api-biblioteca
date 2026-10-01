package com.escola.biblioteca.domain.patron.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.ProfileConfig;
import com.escola.biblioteca.domain.patron.repository.ProfileConfigRepository;
import com.escola.biblioteca.dto.request.ProfileConfigRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileConfigServiceTest {

    @Mock
    private ProfileConfigRepository profileConfigRepository;

    @Mock
    private DashboardPublisher dashboardPublisher;

    @InjectMocks
    private ProfileConfigService profileConfigService;

    private UUID configId;
    private UUID institutionId;
    private ProfileConfig config;

    @BeforeEach
    void setUp() {
        configId = UUID.randomUUID();
        institutionId = UUID.randomUUID();

        config = new ProfileConfig();
        config.setId(configId);
        config.setInstitutionId(institutionId);
        config.setProfile(PatronProfile.STUDENT);
        config.setMaxLoans(3);
        config.setLoanDays(14);
        config.setMaxRenewals(2);
        config.setHoldLimit(2);
        config.setFineRateCents(100);
        config.setFineCapCents(1000);
    }

    @Test
    void findByInstitutionId_filtersMatchingInstitution() {
        ProfileConfig otherInstConfig = new ProfileConfig();
        otherInstConfig.setId(UUID.randomUUID());
        otherInstConfig.setInstitutionId(UUID.randomUUID());

        when(profileConfigRepository.findAll()).thenReturn(List.of(config, otherInstConfig));

        List<ProfileConfig> result = profileConfigService.findByInstitutionId(institutionId);

        assertEquals(1, result.size());
        assertEquals(configId, result.get(0).getId());
    }

    @Test
    void findById_found_returnsConfig() {
        when(profileConfigRepository.findById(configId)).thenReturn(Optional.of(config));

        ProfileConfig found = profileConfigService.findById(configId);

        assertNotNull(found);
        assertEquals(configId, found.getId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(profileConfigRepository.findById(configId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileConfigService.findById(configId));
    }

    @Test
    void create_savesConfigAndNotifiesDashboard() {
        ProfileConfigRequest request = new ProfileConfigRequest("TEACHER", 5, 30, 3, 5, 50, 2000);
        when(profileConfigRepository.save(any(ProfileConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileConfig created = profileConfigService.create(institutionId, request);

        assertNotNull(created);
        assertEquals(PatronProfile.TEACHER, created.getProfile());
        assertEquals(institutionId, created.getInstitutionId());
        assertEquals(5, created.getMaxLoans());
        verify(profileConfigRepository).save(any(ProfileConfig.class));
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void update_modifiesConfigAndNotifiesDashboard() {
        when(profileConfigRepository.findById(configId)).thenReturn(Optional.of(config));
        when(profileConfigRepository.save(any(ProfileConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileConfigRequest request = new ProfileConfigRequest("STUDENT", 4, 15, 2, 3, 150, 1500);
        ProfileConfig updated = profileConfigService.update(configId, request);

        assertEquals(4, updated.getMaxLoans());
        assertEquals(150, updated.getFineRateCents());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void delete_deletesConfigAndNotifiesDashboard() {
        profileConfigService.delete(configId);

        verify(profileConfigRepository).deleteById(configId);
        verify(dashboardPublisher).dadosAlterados();
    }
}
