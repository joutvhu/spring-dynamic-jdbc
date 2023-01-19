package com.joutvhu.dynamic.jdbc.query;

import com.joutvhu.dynamic.jdbc.DynamicQuery;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.query.StringBasedJdbcQuery;
import org.springframework.data.jdbc.repository.support.DynamicOpenJdbcQueryLookupStrategy;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * {@link QueryLookupStrategy} that tries to detect a dynamic query declared via {@link DynamicQuery} annotation.
 *
 * @author Giao Ho
 */
public class DynamicJdbcQueryLookupStrategy extends DynamicOpenJdbcQueryLookupStrategy {
    private RelationalMappingContext context;
    private QueryLookupStrategy jdbcQueryLookupStrategy;

    public DynamicJdbcQueryLookupStrategy(
            ApplicationEventPublisher publisher, @Nullable EntityCallbacks callbacks,
            RelationalMappingContext context, JdbcConverter converter, Dialect dialect,
            QueryMappingConfiguration queryMappingConfiguration, NamedParameterJdbcOperations operations,
            QueryLookupStrategy queryLookupStrategy
    ) {
        super(publisher, callbacks, context, converter, dialect, queryMappingConfiguration, operations);
        this.context = context;
        this.jdbcQueryLookupStrategy = queryLookupStrategy;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        if (isDynamicQueryMethod(method)) {
            DynamicJdbcQueryMethod queryMethod = new DynamicJdbcQueryMethod(method, metadata, factory, namedQueries, context);
            RowMapper<?> mapper = queryMethod.isModifyingQuery() ? null : createMapper(queryMethod);
            return new DynamicJdbcRepositoryQuery(queryMethod, getOperations(), mapper, getConverter());
        } else return jdbcQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
    }

    private boolean isDynamicQueryMethod(Method method) {
        DynamicQuery annotation = method.getAnnotation(DynamicQuery.class);
        return annotation != null;
    }

    public static QueryLookupStrategy create(
            ApplicationEventPublisher publisher, @Nullable EntityCallbacks callbacks,
            RelationalMappingContext context, JdbcConverter converter, Dialect dialect,
            QueryMappingConfiguration queryMappingConfiguration, NamedParameterJdbcOperations operations,
            QueryLookupStrategy queryLookupStrategy) {
        Assert.notNull(publisher, "ApplicationEventPublisher must not be null");
        Assert.notNull(context, "RelationalMappingContextPublisher must not be null");
        Assert.notNull(converter, "JdbcConverter must not be null");
        Assert.notNull(dialect, "Dialect must not be null");
        Assert.notNull(queryMappingConfiguration, "QueryMappingConfiguration must not be null");
        Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");

        return new DynamicJdbcQueryLookupStrategy(publisher, callbacks, context, converter, dialect,
                queryMappingConfiguration, operations, queryLookupStrategy);
    }
}
