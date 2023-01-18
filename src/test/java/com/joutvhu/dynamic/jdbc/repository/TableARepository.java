package com.joutvhu.dynamic.jdbc.repository;

import com.joutvhu.dynamic.jdbc.DynamicQuery;
import com.joutvhu.dynamic.jdbc.entity.TableA;
import com.joutvhu.dynamic.jdbc.model.TableAB;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TableARepository extends CrudRepository<TableA, Long> {
    @DynamicQuery(value = "select * from TABLE_A where FIELD_B = :fieldB\n" +
            "<#if fieldC?has_content>\n" +
            "  and FIELD_C = :fieldC\n" +
            "</#if>"
    )
    List<TableA> findA1(Long fieldB, String fieldC);

    @Query(value = "select * from TABLE_A t where t.FIELD_A = :fieldA and t.FIELD_C = :fieldC")
    List<TableA> findA2(Long fieldA, String fieldC);

    @DynamicQuery(value = "select * from TABLE_A a inner join TABLE_B b\n" +
            "on a.FIELD_A = b.FIELD_A\n" +
            "<#if fieldB??>\n" +
            "  and a.FIELD_B = :fieldB\n" +
            "</#if>" +
            "<#if fieldD??>\n" +
            "  and b.FIELD_D = :fieldD\n" +
            "</#if>"
    )
    List<TableAB> findJ(Long fieldB, Long fieldD);
}
