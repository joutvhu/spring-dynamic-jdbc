package com.joutvhu.dynamic.jdbc;

import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare finder dynamic queries directly on repository methods.
 *
 * @author Giao Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface DynamicQuery {
    /**
     * Provides a query template method name, which is used to find external query templates.
     * The default is {@code entityName:methodName}, entityName is entity class name, methodName is query method name.
     *
     * @return the query template method name
     * @since x.x.1
     */
    String name() default "";

    /**
     * Defines the query template to be executed when the annotated method is called.
     */
    String value() default "";

    /**
     * Optional {@link RowMapper} to use to convert the result of the query to domain class instances. Cannot be used
     * along with {@link #resultSetExtractorClass()} only one of the two can be set.
     */
    Class<? extends RowMapper> rowMapperClass() default RowMapper.class;

    /**
     * Optional name of a bean of type {@link RowMapper} to use to convert the result of the query to domain class instances. Cannot be used
     * along with {@link #resultSetExtractorClass()} only one of the two can be set.
     */
    String rowMapperRef() default "";

    /**
     * Optional {@link ResultSetExtractor} to use to convert the result of the query to domain class instances. Cannot be
     * used along with {@link #rowMapperClass()} only one of the two can be set.
     */
    Class<? extends ResultSetExtractor> resultSetExtractorClass() default ResultSetExtractor.class;

    /**
     * Optional name of a bean of type {@link ResultSetExtractor} to use to convert the result of the query to domain class instances. Cannot be
     * used along with {@link #rowMapperClass()} only one of the two can be set.
     */
    String resultSetExtractorRef() default "";
}
