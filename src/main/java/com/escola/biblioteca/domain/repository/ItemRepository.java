package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends CrudRepository<Item, UUID> {
    Optional<Item> findByBarcode(String barcode);
    List<Item> findByWorkId(UUID workId);
    List<Item> findByWorkIdAndStatus(UUID workId, ItemStatus status);
    Optional<Item> findByWorkIdAndLibraryIdAndStatus(UUID workId, UUID libraryId, ItemStatus status);
}