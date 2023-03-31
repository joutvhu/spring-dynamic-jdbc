package com.joutvhu.dynamic.jdbc;

import com.joutvhu.dynamic.jdbc.entity.TableA;
import com.joutvhu.dynamic.jdbc.entity.TableB;
import com.joutvhu.dynamic.jdbc.entity.TableC;
import com.joutvhu.dynamic.jdbc.model.ModelC;
import com.joutvhu.dynamic.jdbc.model.TableAB;
import com.joutvhu.dynamic.jdbc.repository.TableARepository;
import com.joutvhu.dynamic.jdbc.repository.TableBRepository;
import com.joutvhu.dynamic.jdbc.repository.TableCRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = JdbcDynamicApplication.class)
@Transactional
public class JdbcDynamicApplicationTest {
    @Autowired
    private TableARepository tableARepository;
    @Autowired
    private TableBRepository tableBRepository;
    @Autowired
    private TableCRepository tableCRepository;

    @Test
    public void findA1() {
        List<TableA> result = tableARepository.findA1(410L, "DSFGT4510A");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findA1CNull() {
        List<TableA> result = tableARepository.findA1(104L, null);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findA1CEmpty() {
        List<TableA> result = tableARepository.findA1(104L, "");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findA2() {
        List<TableA> result = tableARepository.findA2(195L, "DSFGT4510A");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findAllA() {
        long count = tableARepository.count();
        Assertions.assertEquals(3, count);
    }

    @Test
    public void findJ1() {
        List<TableAB> result = tableARepository.findJ(101L, 12042107L);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findJ2() {
        List<TableAB> result = tableARepository.findJ(104L, null);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void findJ3() {
        List<TableAB> result = tableARepository.findJ(null, 41017100L);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findJ4() {
        List<TableAB> result = tableARepository.findJ(null, null);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void findB1StartH() {
        List<TableB> result = tableBRepository.findB1("HBTVB");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findB1StartG() {
        List<TableB> result = tableBRepository.findB1("GSDRB");
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void findB1All() {
        List<TableB> result = tableBRepository.findB1(null);
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void findB2() {
        List<TableB> result = tableBRepository.findB2(50000000L);
        Assertions.assertEquals(4, result.size());
    }

    @Test
    public void findB2UL() {
        List<TableB> result = tableBRepository.findB2(null);
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void findB2P() {
        List<TableB> result = tableBRepository.findB2(50000000L);
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals("HBTVB", result.get(0).getFieldE());
    }

    @Test
    public void findB3() {
        List<TableB> result = tableBRepository.findB3(50000000L);
        Assertions.assertEquals(4, result.size());
    }

    @Test
    public void sumB1() {
        long result = tableBRepository.sumB1(40000000L);
        Assertions.assertEquals(33452681L, result);
    }

    @Test
    public void findB4() {
        List<TableB> result = tableBRepository.findB4(new ModelC(0L, "HTYRB"));
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findB5() {
        List<TableB> result = tableBRepository.findB5(12042107L);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void findC1() {
        List<Long> c = new ArrayList<>();
        c.add(101L);
        c.add(104L);
        c.add(410L);
        List<TableC> result = tableCRepository.search(null, "T", c);
        Assertions.assertEquals(3, result.size());
    }
}