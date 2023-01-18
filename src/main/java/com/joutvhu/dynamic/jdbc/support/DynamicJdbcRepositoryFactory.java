package com.joutvhu.dynamic.jdbc.support;

import com.joutvhu.dynamic.jdbc.query.DynamicJdbcQueryLookupStrategy;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.support.JdbcRepositoryFactory;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * JPA specific generic repository factory.
 *
 * @author Giao Ho
 * @since 2.x.1
 */
public class DynamicJdbcRepositoryFactory extends JdbcRepositoryFactory {
    private final RelationalMappingContext context;
    private final JdbcConverter converter;
    private final ApplicationEventPublisher publisher;
    private final NamedParameterJdbcOperations operations;
    private final Dialect dialect;
    @Nullable
    private BeanFactory beanFactory;

    private QueryMappingConfiguration queryMappingConfiguration = QueryMappingConfiguration.EMPTY;
    private EntityCallbacks entityCallbacks;

    /**
     * Creates a new {@link DynamicJdbcRepositoryFactory}.
     */
    public DynamicJdbcRepositoryFactory(DataAccessStrategy dataAccessStrategy, RelationalMappingContext context,
                                        JdbcConverter converter, Dialect dialect, ApplicationEventPublisher publisher,
                                        NamedParameterJdbcOperations operations) {
        super(dataAccessStrategy, context, converter, dialect, publisher, operations);

        this.publisher = publisher;
        this.context = context;
        this.converter = converter;
        this.dialect = dialect;
        this.operations = operations;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
            QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return super.getQueryLookupStrategy(key, evaluationContextProvider)
                .map(lookupStrategy -> DynamicJdbcQueryLookupStrategy.create(
                        publisher, entityCallbacks, context, converter, dialect, queryMappingConfiguration,
                        operations, beanFactory, evaluationContextProvider, lookupStrategy
                ));
    }

    @Override
    public void setEntityCallbacks(EntityCallbacks entityCallbacks) {
        super.setEntityCallbacks(entityCallbacks);
        this.entityCallbacks = entityCallbacks;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        this.beanFactory = beanFactory;
    }
}
