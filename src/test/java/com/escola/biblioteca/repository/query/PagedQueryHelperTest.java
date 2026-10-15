package com.escola.biblioteca.repository.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagedQueryHelperTest {

    @Mock
    private JdbcClient jdbcClient;

    @Mock
    private JdbcClient.StatementSpec countSpec;

    @Mock
    private JdbcClient.MappedQuerySpec<Long> countQuerySpec;

    @Mock
    private JdbcClient.StatementSpec dataSpec;

    @Mock
    private JdbcClient.MappedQuerySpec<TestRecord> dataQuerySpec;

    private PagedQueryHelper pagedQueryHelper;

    record TestRecord(String id, String name) {}

    @BeforeEach
    void setUp() {
        pagedQueryHelper = new PagedQueryHelper(jdbcClient);
    }

    @Test
    void paginate_whenCountIsZero_returnsEmptyPageWithoutRunningDataQuery() {
        when(jdbcClient.sql(contains("COUNT(*)"))).thenReturn(countSpec);
        when(countSpec.query(Long.class)).thenReturn(countQuerySpec);
        when(countQuerySpec.single()).thenReturn(0L);

        Page<TestRecord> result = pagedQueryHelper.paginate(
                "SELECT id, name",
                "FROM test_table WHERE 1=1",
                Map.of(),
                "id ASC",
                PageRequest.of(0, 10),
                TestRecord.class
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(jdbcClient, never()).sql(startsWith("SELECT id, name"));
    }

    @Test
    void paginate_whenCountGreaterThanZero_executesDataQueryAndReturnsPage() {
        when(jdbcClient.sql(contains("COUNT(*)"))).thenReturn(countSpec);
        when(countSpec.query(Long.class)).thenReturn(countQuerySpec);
        when(countQuerySpec.single()).thenReturn(42L);

        when(jdbcClient.sql(argThat(sql -> sql.contains("LIMIT :limit OFFSET :offset")))).thenReturn(dataSpec);
        when(dataSpec.query(TestRecord.class)).thenReturn(dataQuerySpec);
        when(dataQuerySpec.list()).thenReturn(List.of(new TestRecord("1", "Alpha")));

        Page<TestRecord> result = pagedQueryHelper.paginate(
                "SELECT id, name",
                "FROM test_table WHERE status = :status",
                Map.of("status", "ACTIVE"),
                "id ASC",
                PageRequest.of(0, 10),
                TestRecord.class
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(42L, result.getTotalElements());
        assertEquals(5, result.getTotalPages());
        verify(dataSpec).param("limit", 10);
        verify(dataSpec).param("offset", 0L);
    }
}
