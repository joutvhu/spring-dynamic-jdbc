package org.springframework.data.jdbc.repository.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public abstract class DynamicOpenJdbcQueryLookupStrategy extends JdbcQueryLookupStrategy {
    public DynamicOpenJdbcQueryLookupStrategy(
            ApplicationEventPublisher publisher, EntityCallbacks callbacks, RelationalMappingContext context,
            JdbcConverter converter, Dialect dialect, QueryMappingConfiguration queryMappingConfiguration,
            NamedParameterJdbcOperations operations, BeanFactory beanfactory) {
        super(publisher, callbacks, context, converter, dialect, queryMappingConfiguration, operations, beanfactory);
    }

    @Override
    public NamedParameterJdbcOperations getOperations() {
        return super.getOperations();
    }

    @Override
    public JdbcConverter getConverter() {
        return super.getConverter();
    }

    @Override
    public RowMapper<Object> createMapper(Class<?> returnedObjectType) {
        return super.createMapper(returnedObjectType);
    }
}
