package com.joutvhu.dynamic.jdbc.query;

import com.joutvhu.dynamic.commons.DynamicQueryTemplate;
import com.joutvhu.dynamic.jdbc.DynamicQuery;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.query.DynamicJdbcParameterAccessor;
import org.springframework.data.jdbc.repository.query.DynamicStringBasedJdbcQuery;
import org.springframework.data.jdbc.repository.query.StringBasedJdbcQuery;
import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.Map;

/**
 * {@link RepositoryQuery} implementation that inspects a {@link DynamicJdbcQueryMethod}
 * for the existence of an {@link DynamicQuery} annotation and creates a JDBC {@link DynamicQuery} from it.
 *
 * @author Giao Ho
 */
public class DynamicJdbcRepositoryQuery extends DynamicStringBasedJdbcQuery {
    private final DynamicJdbcQueryMethod method;

    /**
     * Creates a new {@link DynamicJdbcRepositoryQuery} from the given {@link StringBasedJdbcQuery}.
     */
    public DynamicJdbcRepositoryQuery(
            DynamicJdbcQueryMethod queryMethod, NamedParameterJdbcOperations operations,
            RowMapperFactory rowMapperFactory, JdbcConverter converter) {
        super(queryMethod, operations, rowMapperFactory, converter);
        this.method = queryMethod;
    }

    @Override
    protected String createQuery(RelationalParameterAccessor accessor) {
        return buildQuery(method.getQueryTemplate(), accessor);
    }

    protected String buildQuery(DynamicQueryTemplate template, RelationalParameterAccessor accessor) {
        try {
            if (template != null) {
                Map<String, Object> model = DynamicJdbcParameterAccessor.of(method, accessor).getParamModel();
                String queryString = template.process(model)
                        .replaceAll("\n", " ")
                        .replaceAll("\t", " ")
                        .replaceAll(" +", " ")
                        .trim();
                return queryString.isEmpty() ? null : queryString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
