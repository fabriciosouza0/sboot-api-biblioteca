package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.dto.response.WorkDependenciesResponse;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends CrudRepository<Item, UUID> {
    Optional<Item> findByBarcode(String barcode);
    List<Item> findByWorkId(UUID workId);
    List<Item> findByWorkIdAndStatus(UUID workId, ItemStatus status);
    Optional<Item> findByWorkIdAndLibraryIdAndStatus(UUID workId, UUID libraryId, ItemStatus status);

    @Query("SELECT COUNT(*) FROM item i JOIN library l ON i.library_id = l.id WHERE l.institution_id = :institutionId")
    long countByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM item i JOIN library l ON i.library_id = l.id WHERE l.institution_id = :institutionId AND i.status = :status")
    long countByInstitutionIdAndStatus(UUID institutionId, String status);

    @Query("SELECT COUNT(*) FROM item i JOIN library l ON i.library_id = l.id")
    long countAll();

    @Query("SELECT COUNT(*) FROM item i JOIN library l ON i.library_id = l.id WHERE i.status = :status")
    long countAllByStatus(String status);

    @Query("SELECT i.id, i.barcode, i.call_number, i.status, l.name " +
            "FROM item i JOIN library l ON i.library_id = l.id WHERE i.work_id = :workId")
    List<WorkDependenciesResponse.ItemSummary> findDependencyItems(UUID workId);
}
