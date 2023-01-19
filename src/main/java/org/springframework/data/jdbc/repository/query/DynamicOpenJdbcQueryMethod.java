package org.springframework.data.jdbc.repository.query;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;

public abstract class DynamicOpenJdbcQueryMethod extends JdbcQueryMethod {
    public DynamicOpenJdbcQueryMethod(
            Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries,
            MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> mappingContext) {
        super(method, metadata, factory, namedQueries, mappingContext);
    }

    @Override
    public Class<? extends RowMapper> getRowMapperClass() {
        return super.getRowMapperClass();
    }

    @Override
    public Class<? extends ResultSetExtractor> getResultSetExtractorClass() {
        return super.getResultSetExtractorClass();
    }
}
