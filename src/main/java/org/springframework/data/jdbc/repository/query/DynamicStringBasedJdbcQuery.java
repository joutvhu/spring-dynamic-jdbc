package org.springframework.data.jdbc.repository.query;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.jdbc.core.convert.JdbcColumnTypes;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.jdbc.support.JdbcUtil;
import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.relational.repository.query.RelationalParameters;
import org.springframework.data.relational.repository.query.RelationalParametersParameterAccessor;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.SpelEvaluator;
import org.springframework.data.repository.query.SpelQueryContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.sql.SQLType;
import java.util.ArrayList;
import java.util.List;

public abstract class DynamicStringBasedJdbcQuery extends StringBasedJdbcQuery {
    private static final String PARAMETER_NEEDS_TO_BE_NAMED = "For queries with named parameters you need to provide names for method parameters; Use @Param for query method parameters, or when on Java 8+ use the javac flag -parameters";

    private JdbcQueryMethod queryMethod;
    private JdbcConverter converter;
    private RowMapperFactory rowMapperFactory;
    private BeanFactory beanFactory;
    private QueryMethodEvaluationContextProvider evaluationContextProvider;

    public DynamicStringBasedJdbcQuery(JdbcQueryMethod queryMethod, NamedParameterJdbcOperations operations, RowMapper<?> defaultRowMapper, JdbcConverter converter, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        this(queryMethod, operations, result -> (RowMapper<Object>) defaultRowMapper, converter, evaluationContextProvider);
    }

    public DynamicStringBasedJdbcQuery(JdbcQueryMethod queryMethod, NamedParameterJdbcOperations operations, RowMapperFactory rowMapperFactory, JdbcConverter converter, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        super(queryMethod, operations, rowMapperFactory, converter, evaluationContextProvider);

        this.queryMethod = queryMethod;
        this.converter = converter;
        this.rowMapperFactory = rowMapperFactory;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    public Object execute(Object[] objects) {
        RelationalParameterAccessor accessor = new RelationalParametersParameterAccessor(getQueryMethod(), objects);
        ResultProcessor processor = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);
        JdbcQueryExecution.ResultProcessingConverter converter = new JdbcQueryExecution
                .ResultProcessingConverter(processor, this.converter.getMappingContext(), this.converter.getEntityInstantiators());

        RowMapper<Object> rowMapper = determineRowMapper(rowMapperFactory.create(resolveTypeToRead(processor)), converter,
                accessor.findDynamicProjection() != null);

        JdbcQueryExecution<?> queryExecution = getQueryExecution(queryMethod, determineResultSetExtractor(rowMapper), rowMapper);

        MapSqlParameterSource parameterMap = this.bindParameters(accessor);

        String query = createQuery(accessor);

        if (ObjectUtils.isEmpty(query)) {
            throw new IllegalStateException(String.format("No query specified on %s", queryMethod.getName()));
        }

        return queryExecution.execute(processSpelExpressions(objects, parameterMap, query), parameterMap);
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

    private String processSpelExpressions(Object[] objects, MapSqlParameterSource parameterMap, String query) {
        SpelQueryContext.EvaluatingSpelQueryContext queryContext = SpelQueryContext
                .of((counter, expression) -> String.format("__$synthetic$__%d", counter + 1), String::concat)
                .withEvaluationContextProvider(evaluationContextProvider);

        SpelEvaluator spelEvaluator = queryContext.parse(query, queryMethod.getParameters());

        spelEvaluator.evaluate(objects).forEach(parameterMap::addValue);

        return spelEvaluator.getQueryString();
    }

    private void convertAndAddParameter(MapSqlParameterSource parameters, Parameter p, Object value) {
        String parameterName = p.getName().orElseThrow(() -> new IllegalStateException(PARAMETER_NEEDS_TO_BE_NAMED));

        RelationalParameters.RelationalParameter parameter = queryMethod.getParameters().getParameter(p.getIndex());
        ResolvableType resolvableType = parameter.getResolvableType();
        Class<?> type = resolvableType.resolve();
        Assert.notNull(type, "@Query parameter type could not be resolved");

        JdbcValue jdbcValue;
        if (value instanceof Iterable) {

            List<Object> mapped = new ArrayList<>();
            SQLType jdbcType = null;

            Class<?> elementType = resolvableType.getGeneric(0).resolve();

            Assert.notNull(elementType, "@Query Iterable parameter generic type could not be resolved");

            for (Object o : (Iterable<?>) value) {
                JdbcValue elementJdbcValue = converter.writeJdbcValue(o, elementType,
                        JdbcUtil.targetSqlTypeFor(JdbcColumnTypes.INSTANCE.resolvePrimitiveType(elementType)));
                if (jdbcType == null) {
                    jdbcType = elementJdbcValue.getJdbcType();
                }

                mapped.add(elementJdbcValue.getValue());
            }

            jdbcValue = JdbcValue.of(mapped, jdbcType);
        } else {
            jdbcValue = converter.writeJdbcValue(value, type,
                    JdbcUtil.targetSqlTypeFor(JdbcColumnTypes.INSTANCE.resolvePrimitiveType(type)));
        }

        SQLType jdbcType = jdbcValue.getJdbcType();
        if (jdbcType == null) {

            parameters.addValue(parameterName, jdbcValue.getValue());
        } else {
            parameters.addValue(parameterName, jdbcValue.getValue(), jdbcType.getVendorTypeNumber());
        }
    }
}
