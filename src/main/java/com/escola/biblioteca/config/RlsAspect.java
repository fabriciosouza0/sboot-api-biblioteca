package com.escola.biblioteca.config;

import com.escola.biblioteca.security.AdminAuthenticationToken;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@Aspect
@Component
@Order(100)
public class RlsAspect {

    private final DataSource dataSource;

    public RlsAspect(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethods() {}

    @Before("transactionalMethods()")
    public void setRlsVariables() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AdminAuthenticationToken adminAuth)) {
            return;
        }

        Connection conn = DataSourceUtils.getConnection(dataSource);
        if (conn == null) {
            return;
        }

        try {
            UUID institutionId = adminAuth.getCurrentInstitutionId();
            if (institutionId != null) {
                execute(conn, "SET LOCAL app.is_global_admin = 'false'");
                execute(conn, "SET LOCAL app.current_institution_id = '" + institutionId + "'");
                execute(conn, "SET LOCAL app.current_adm_codigo = '" + adminAuth.getName() + "'");
            } else if (adminAuth.isGlobalAdmin()) {
                execute(conn, "SET LOCAL app.is_global_admin = 'true'");
                execute(conn, "SET LOCAL app.current_institution_id = ''");
                execute(conn, "SET LOCAL app.current_adm_codigo = '" + adminAuth.getName() + "'");
            }
        } catch (SQLException e) {
            // Log but don't fail the transaction
            System.err.println("RLS aspect error: " + e.getMessage());
        }
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
