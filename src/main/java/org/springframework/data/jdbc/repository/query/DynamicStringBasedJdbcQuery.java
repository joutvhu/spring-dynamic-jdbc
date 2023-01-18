package org.springframework.data.jdbc.repository.query;

import com.joutvhu.dynamic.jdbc.query.DynamicJdbcQueryMethod;
import org.springframework.data.jdbc.core.convert.JdbcColumnTypes;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.jdbc.support.JdbcUtil;
import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.relational.repository.query.RelationalParametersParameterAccessor;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.ObjectUtils;

import java.sql.SQLType;

public abstract class DynamicStringBasedJdbcQuery extends StringBasedJdbcQuery {
    private static final String PARAMETER_NEEDS_TO_BE_NAMED = "For queries with named parameters you need to provide names for method parameters. Use @Param for query method parameters, or when on Java 8+ use the javac flag -parameters.";

    private DynamicJdbcQueryMethod queryMethod;
    private JdbcConverter converter;
    private RowMapperFactory rowMapperFactory;

    public DynamicStringBasedJdbcQuery(DynamicJdbcQueryMethod queryMethod, NamedParameterJdbcOperations operations, RowMapperFactory rowMapperFactory, JdbcConverter converter) {
        super(queryMethod, operations, rowMapperFactory, converter);

        this.queryMethod = queryMethod;
        this.converter = converter;
        this.rowMapperFactory = rowMapperFactory;
    }

    @Override
    public Object execute(Object[] objects) {
        RelationalParameterAccessor accessor = new RelationalParametersParameterAccessor(getQueryMethod(), objects);
        ResultProcessor processor = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);
        JdbcQueryExecution.ResultProcessingConverter converter = new JdbcQueryExecution.ResultProcessingConverter(processor, this.converter.getMappingContext(),
                this.converter.getEntityInstantiators());

        RowMapper<Object> rowMapper = determineRowMapper(rowMapperFactory.create(resolveTypeToRead(processor)), converter,
                accessor.findDynamicProjection() != null);

        JdbcQueryExecution<?> queryExecution = getQueryExecution(//
                queryMethod, //
                determineResultSetExtractor(rowMapper), //
                rowMapper);

        MapSqlParameterSource parameterMap = this.bindParameters(accessor);

        String query = createQuery(accessor);

        if (ObjectUtils.isEmpty(query)) {
            throw new IllegalStateException(String.format("No query specified on %s", queryMethod.getName()));
        }

        return queryExecution.execute(query, parameterMap);
    }

    protected abstract String createQuery(RelationalParameterAccessor accessor);

    private MapSqlParameterSource bindParameters(RelationalParameterAccessor accessor) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        Parameters<?, ?> bindableParameters = accessor.getBindableParameters();

        for (Parameter bindableParameter : bindableParameters) {
            convertAndAddParameter(parameters, bindableParameter, accessor.getBindableValue(bindableParameter.getIndex()));
        }

        return parameters;
    }

    private void convertAndAddParameter(MapSqlParameterSource parameters, Parameter p, Object value) {
        String parameterName = p.getName().orElseThrow(() -> new IllegalStateException(PARAMETER_NEEDS_TO_BE_NAMED));

        Class<?> parameterType = queryMethod.getParameters().getParameter(p.getIndex()).getType();
        Class<?> conversionTargetType = JdbcColumnTypes.INSTANCE.resolvePrimitiveType(parameterType);

        JdbcValue jdbcValue = converter.writeJdbcValue(value, conversionTargetType,
                JdbcUtil.targetSqlTypeFor(conversionTargetType));

        SQLType jdbcType = jdbcValue.getJdbcType();
        if (jdbcType == null) {
            parameters.addValue(parameterName, jdbcValue.getValue());
        } else {
            parameters.addValue(parameterName, jdbcValue.getValue(), jdbcType.getVendorTypeNumber());
        }
    }
}
