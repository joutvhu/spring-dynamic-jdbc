package com.joutvhu.dynamic.jdbc.query;

import com.joutvhu.dynamic.commons.DynamicQueryTemplate;
import com.joutvhu.dynamic.commons.DynamicQueryTemplateProvider;
import com.joutvhu.dynamic.commons.util.ApplicationContextHolder;
import com.joutvhu.dynamic.jdbc.DynamicQuery;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jdbc.repository.query.DynamicOpenJdbcQueryMethod;
import org.springframework.data.jdbc.repository.query.JdbcQueryMethod;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC specific extension of {@link JdbcQueryMethod}.
 *
 * @author Giao Ho
 */
public class DynamicJdbcQueryMethod extends DynamicOpenJdbcQueryMethod {
    private static final Map<String, String> templateMap = new HashMap<>();

    private final Method method;

    private DynamicQueryTemplateProvider queryTemplateProvider;
    private DynamicQueryTemplate queryTemplate;

    static {
        templateMap.put("value", "");
    }

    /**
     * Creates a {@link JdbcQueryMethod}.
     *
     * @param method   must not be {@literal null}
     * @param metadata must not be {@literal null}
     * @param factory  must not be {@literal null}
     */
    protected DynamicJdbcQueryMethod(
            Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries,
            MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> mappingContext) {
        super(method, metadata, factory, namedQueries, mappingContext);
        this.method = method;
    }

    private DynamicQueryTemplateProvider getTemplateProvider() {
        if (queryTemplateProvider == null)
            queryTemplateProvider = ApplicationContextHolder.getBean(DynamicQueryTemplateProvider.class);
        return queryTemplateProvider;
    }

    protected DynamicQueryTemplate findTemplate(String name) {
        DynamicQueryTemplateProvider provider = getTemplateProvider();
        return provider != null ? provider.findTemplate(name) : null;
    }

    protected DynamicQueryTemplate createTemplate(String name, String query) {
        DynamicQueryTemplateProvider provider = getTemplateProvider();
        return provider != null ? provider.createTemplate(name, query) : null;
    }

    protected DynamicQueryTemplate getTemplate(String name) {
        String templateName = templateMap.get(name);
        if (StringUtils.hasText(templateName)) templateName = "." + templateName;
        String templateMethodName = getMergedOrDefaultAnnotationValue("name", DynamicQuery.class, String.class);
        if (!StringUtils.hasText(templateMethodName)) templateMethodName = getTemplateKey();
        templateName = templateMethodName + templateName;
        String query = getMergedOrDefaultAnnotationValue(name, DynamicQuery.class, String.class);
        return StringUtils.hasText(query) ? createTemplate(templateName, query) : findTemplate(templateName);
    }

    @Nullable
    public DynamicQueryTemplate getQueryTemplate() {
        if (queryTemplate == null)
            queryTemplate = getTemplate("value");
        return queryTemplate;
    }

    private String getEntityName() {
        return getEntityInformation().getJavaType().getSimpleName();
    }

    private String getTemplateKey() {
        return getEntityName() + ":" + getName();
    }

    /**
     * Returns the class to be used as {@link org.springframework.jdbc.core.RowMapper}
     *
     * @return May be {@code null}.
     */
    @Nullable
    @Override
    public Class<? extends RowMapper> getRowMapperClass() {
        return getMergedOrDefaultAnnotationValue("rowMapperClass", DynamicQuery.class, Class.class);
    }

    /**
     * Returns the name of the bean to be used as {@link org.springframework.jdbc.core.RowMapper}
     *
     * @return May be {@code null}.
     */
    @Nullable
    @Override
    public String getRowMapperRef() {
        return getMergedOrDefaultAnnotationValue("rowMapperRef", DynamicQuery.class, String.class);
    }

    /**
     * Returns the class to be used as {@link org.springframework.jdbc.core.ResultSetExtractor}
     *
     * @return May be {@code null}.
     */
    @Nullable
    @Override
    public Class<? extends ResultSetExtractor> getResultSetExtractorClass() {
        return getMergedOrDefaultAnnotationValue("resultSetExtractorClass", DynamicQuery.class, Class.class);
    }

    /**
     * Returns the bean name to be used as {@link org.springframework.jdbc.core.ResultSetExtractor}
     *
     * @return May be {@code null}.
     */
    @Nullable
    @Override
    public String getResultSetExtractorRef() {
        return getMergedOrDefaultAnnotationValue("resultSetExtractorRef", DynamicQuery.class, String.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> T getMergedOrDefaultAnnotationValue(String attribute, Class annotationType, Class<T> targetType) {
        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation == null)
            return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
        return targetType.cast(AnnotationUtils.getValue(annotation, attribute));
    }
}
