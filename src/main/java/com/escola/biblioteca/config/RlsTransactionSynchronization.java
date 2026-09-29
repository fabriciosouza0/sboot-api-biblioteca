package com.escola.biblioteca.config;

import com.escola.biblioteca.security.AdminAuthenticationToken;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RlsTransactionSynchronization {

    private static final Logger log = LoggerFactory.getLogger(RlsTransactionSynchronization.class);

    private final DataSource dataSource;

    public RlsTransactionSynchronization(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void registerIfNeeded() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    // No-op
                }

                @Override
                public void afterCommit() {
                    // No-op
                }

                @Override
                public void beforeCompletion() {
                    setRlsSessionVariables();
                }
            });
        }
    }

    private void setRlsSessionVariables() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AdminAuthenticationToken adminAuth)) {
            return;
        }

        Connection conn = DataSourceUtils.getConnection(dataSource);
        if (conn == null) {
            return;
        }

        try {
            if (adminAuth.isGlobalAdmin()) {
                execute(conn, "SET LOCAL app.is_global_admin = 'true'");
                execute(conn, "SET LOCAL app.current_institution_id = ''");
            } else {
                UUID institutionId = adminAuth.getInstitutionId();
                if (institutionId != null) {
                    execute(conn, "SET LOCAL app.is_global_admin = 'false'");
                    execute(conn, "SET LOCAL app.current_institution_id = '" + institutionId + "'");
                    execute(conn, "SET LOCAL app.current_adm_codigo = '" + adminAuth.getName() + "'");
                }
            }
        } catch (SQLException e) {
            log.warn("Falha ao definir variáveis RLS: {}", e.getMessage());
        }
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}