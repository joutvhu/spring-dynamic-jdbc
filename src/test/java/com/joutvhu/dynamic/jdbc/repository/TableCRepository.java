package com.joutvhu.dynamic.jdbc.repository;

import com.joutvhu.dynamic.jdbc.DynamicQuery;
import com.joutvhu.dynamic.jdbc.entity.TableC;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TableCRepository extends CrudRepository<TableC, Long> {
    @DynamicQuery("select * from TABLE_C\n" +
            "<@where>\n" +
            "   <#if fieldA??>\n" +
            "       and FIELD_A = :fieldA\n" +
            "   </#if>\n" +
            "   <#if fieldB??>\n" +
            "       and FIELD_B like concat('%',:fieldB,'%')\n" +
            "   </#if>\n" +
            "   <#if fieldCs??>\n" +
            "       and FIELD_C in (:fieldCs)\n" +
            "   </#if>\n" +
            "</@where>")
    List<TableC> search(Long fieldA, String fieldB, List<Long> fieldCs);
}
