package br.com.utfpr.gerenciamento.server.repository.impl;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: 15/05/2020 Necessário finalizar

@Repository
public class EmprestimoFilterRepositoryImpl implements EmprestimoFilterRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Emprestimo> filter(EmprestimoFilter emprestimoFilter) {
        String sql = "SELECT E.* \n" +
                "FROM \n" +
                "EMPRESTIMO E \n" +
                "LEFT JOIN USUARIO UE\n" +
                "ON UE.ID = E.USUARIO_EMPRESTIMO_ID\n" +
                "LEFT JOIN USUARIO UR \n" +
                "ON UR.ID = E.USUARIO_RESPONSAVEL_ID";

        StringBuilder where = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (emprestimoFilter != null) {

            if (emprestimoFilter.getUsuarioEmprestimo() != null) {
                where.append("UE.ID = :USUARIO_EMPRESTIMO_ID ");
                params.addValue("USUARIO_EMPRESTIMO_ID", emprestimoFilter.getUsuarioEmprestimo().getId());
            }

            if (emprestimoFilter.getUsuarioResponsalvel() != null) {
                putAnd(where, params);
                where.append("UR.ID = :USUARIO_REPONSAVEL_ID ");
                params.addValue("USUARIO_REPONSAVEL_ID", emprestimoFilter.getUsuarioResponsalvel().getId());
            }

            if (emprestimoFilter.getDtIniEmp() != null) {
                putAnd(where, params);
                where.append("E.DATA_EMPRESTIMO >= :DTINI::DATE ");
                params.addValue("DTINI", emprestimoFilter.getDtIniEmp());
            }

            if (emprestimoFilter.getDtFimEmp() != null) {
                putAnd(where, params);
                where.append("E.DATA_EMPRESTIMO <= :DTFIM::DATE ");
                params.addValue("DTFIM", emprestimoFilter.getDtFimEmp());
            }

            if (emprestimoFilter.getStatus() != null) {
                if (!emprestimoFilter.getStatus().equals("T")) putAnd(where, params);

                // p - em andamento/ a - atraso/ f - finalizado
                switch (emprestimoFilter.getStatus()) {
                    case ("A"): {
                        where.append("(DATA_DEVOLUCAO IS NULL AND PRAZO_DEVOLUCAO < CURRENT_DATE) ");
                        break;
                    }
                    case ("P"): {
                        where.append("(DATA_DEVOLUCAO IS NULL AND PRAZO_DEVOLUCAO >= CURRENT_DATE) ");
                        break;
                    }
                    case ("F"): {
                        where.append("DATA_DEVOLUCAO IS NOT NULL ");
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        if (where.toString().length() > 0) {
            sql = sql + " WHERE " + where.toString();
        }
        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Emprestimo.class));

    }

    private void putAnd(StringBuilder where, MapSqlParameterSource params) {
        if (params.getValues().size() > 0) {
            where.append(" AND ");
        }
    }

}
