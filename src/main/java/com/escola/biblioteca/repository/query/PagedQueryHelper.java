package com.escola.biblioteca.repository.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PagedQueryHelper {

    private final JdbcClient jdbcClient;

    public PagedQueryHelper(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public <T> Page<T> paginate(
            String selectClause,
            String fromAndWhereClause,
            Map<String, Object> params,
            String defaultOrderBy,
            Pageable pageable,
            Class<T> responseType) {

        long totalElements = count(fromAndWhereClause, params);
        Pageable effectivePageable = (pageable != null) ? pageable : Pageable.unpaged();

        if (totalElements == 0) {
            return new PageImpl<>(Collections.emptyList(), effectivePageable, 0);
        }

        String dataSql = buildDataSql(selectClause, fromAndWhereClause, defaultOrderBy, effectivePageable);
        var dataSpec = jdbcClient.sql(dataSql);
        params.forEach(dataSpec::param);
        bindPagination(dataSpec, effectivePageable);

        List<T> items = dataSpec.query(responseType).list();
        return new PageImpl<>(items, effectivePageable, totalElements);
    }

    public <T> Page<T> paginate(
            String selectClause,
            String fromAndWhereClause,
            Map<String, Object> params,
            String defaultOrderBy,
            Pageable pageable,
            RowMapper<T> rowMapper) {

        long totalElements = count(fromAndWhereClause, params);
        Pageable effectivePageable = (pageable != null) ? pageable : Pageable.unpaged();

        if (totalElements == 0) {
            return new PageImpl<>(Collections.emptyList(), effectivePageable, 0);
        }

        String dataSql = buildDataSql(selectClause, fromAndWhereClause, defaultOrderBy, effectivePageable);
        var dataSpec = jdbcClient.sql(dataSql);
        params.forEach(dataSpec::param);
        bindPagination(dataSpec, effectivePageable);

        List<T> items = dataSpec.query(rowMapper).list();
        return new PageImpl<>(items, effectivePageable, totalElements);
    }

    private long count(String fromAndWhereClause, Map<String, Object> params) {
        var countSpec = jdbcClient.sql("SELECT COUNT(*) " + fromAndWhereClause);
        params.forEach(countSpec::param);
        Long total = countSpec.query(Long.class).single();
        return (total != null) ? total : 0L;
    }

    private String buildDataSql(String selectClause, String fromAndWhereClause, String defaultOrderBy, Pageable pageable) {
        StringBuilder sb = new StringBuilder(selectClause.strip())
                .append(" ")
                .append(fromAndWhereClause.strip())
                .append(" ORDER BY ")
                .append(defaultOrderBy.strip());

        if (pageable.isPaged()) {
            sb.append(" LIMIT :limit OFFSET :offset");
        }
        return sb.toString();
    }

    private void bindPagination(JdbcClient.StatementSpec spec, Pageable pageable) {
        if (pageable.isPaged()) {
            spec.param("limit", pageable.getPageSize());
            spec.param("offset", pageable.getOffset());
        }
    }
}
