package com.joutvhu.dynamic.jdbc.repository;

import com.joutvhu.dynamic.jdbc.DynamicQuery;
import com.joutvhu.dynamic.jdbc.entity.TableB;
import com.joutvhu.dynamic.jdbc.model.ModelC;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TableBRepository extends CrudRepository<TableB, Long> {
    @DynamicQuery
    List<TableB> findB1(String fieldE);

    @DynamicQuery
    List<TableB> findB2(Long maxD);

    @DynamicQuery
    List<TableB> findB3(Long maxD);

    @DynamicQuery
    Long sumB1(Long maxD);

    @DynamicQuery("select * from TABLE_B\n" +
            "<#if modelC.fieldC?has_content>\n" +
            "  where FIELD_E = :#{#modelC.fieldC}\n" +
            "</#if>")
    List<TableB> findB4(ModelC modelC);

    @DynamicQuery(name = "findTableBByFieldD")
    List<TableB> findB5(Long fieldD);
}
