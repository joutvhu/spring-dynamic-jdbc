package org.springframework.data.jdbc.repository.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.convert.EntityRowMapper;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.query.JdbcQueryMethod;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public abstract class DynamicOpenJdbcQueryLookupStrategy extends JdbcQueryLookupStrategy {
    private final RelationalMappingContext context;
    private final JdbcConverter converter;
    private final QueryMappingConfiguration queryMappingConfiguration;
    private final NamedParameterJdbcOperations operations;
    private final BeanFactory beanfactory;

    public DynamicOpenJdbcQueryLookupStrategy(
            ApplicationEventPublisher publisher, EntityCallbacks callbacks, RelationalMappingContext context,
            JdbcConverter converter, Dialect dialect, QueryMappingConfiguration queryMappingConfiguration,
            NamedParameterJdbcOperations operations, BeanFactory beanfactory) {
        super(publisher, callbacks, context, converter, dialect, queryMappingConfiguration, operations, beanfactory);

        this.context = context;
        this.converter = converter;
        this.queryMappingConfiguration = queryMappingConfiguration;
        this.operations = operations;
        this.beanfactory = beanfactory;
    }

    public NamedParameterJdbcOperations getOperations() {
        return operations;
    }

    public JdbcConverter getConverter() {
        return converter;
    }

    public BeanFactory getBeanFactory() {
        return beanfactory;
    }

    @SuppressWarnings("unchecked")
    public RowMapper<Object> createMapper(JdbcQueryMethod queryMethod) {
        Class<?> returnedObjectType = queryMethod.getReturnedObjectType();

        RelationalPersistentEntity<?> persistentEntity = context.getPersistentEntity(returnedObjectType);

        if (persistentEntity == null) {
            return (RowMapper<Object>) SingleColumnRowMapper.newInstance(returnedObjectType, converter.getConversionService());
        }

        return (RowMapper<Object>) determineDefaultMapper(queryMethod);
    }

    private RowMapper<?> determineDefaultMapper(JdbcQueryMethod queryMethod) {
        Class<?> domainType = queryMethod.getReturnedObjectType();
        RowMapper<?> configuredQueryMapper = queryMappingConfiguration.getRowMapper(domainType);

        if (configuredQueryMapper != null)
            return configuredQueryMapper;

        EntityRowMapper<?> defaultEntityRowMapper = new EntityRowMapper<>(
                context.getRequiredPersistentEntity(domainType),
                converter);

        return new PostProcessingRowMapper<>(defaultEntityRowMapper);
    }
}
