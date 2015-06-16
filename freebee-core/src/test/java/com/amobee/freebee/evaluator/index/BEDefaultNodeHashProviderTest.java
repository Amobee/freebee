package com.amobee.freebee.evaluator.index;

import com.amobee.freebee.config.BEDataTypeConfig;
import com.amobee.freebee.expression.BENode;
import com.amobee.freebee.expression.BEPredicateNode;
import com.amobee.freebee.expression.BEReferenceNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.amobee.freebee.ExpressionUtil.*;
import static org.junit.Assert.*;

public class BEDefaultNodeHashProviderTest {

    static BEDataTypeConfigSupplier DATA_TYPES;

    static BEExpressionInfo<String> G_BASE = (createExpressionInfo("e1", idExpr("e1", "gender", "M", "F")));
    static BEExpressionInfo<String> G_SAME = (createExpressionInfo("e1", idExpr("e1", "GENDER", "f", "m")));
    static BEExpressionInfo<String> G_DIFF = (createExpressionInfo("e1", idExpr("e1", "gender", "F")));
    static BEExpressionInfo<String> A_BASE = (createExpressionInfo("e2", expr("age", "[18,24]", "[25,30]")));
    static BEExpressionInfo<String> A_SAME = (createExpressionInfo("e2", expr("AGE", "[25,30]", "[18,24]")));
    static BEExpressionInfo<String> A_DIFF = (createExpressionInfo("e2", expr("age", "[25,30]")));
    static BEExpressionInfo<String> D_BASE = (createExpressionInfo("e3", expr("domain", "foo.com", "foo.org", "BAR.com", "BAR.org")));
    static BEExpressionInfo<String> D_SAME = (createExpressionInfo("e3", expr("DOMAIN", "bar.org", "bar.com", "FOO.org", "FOO.com")));
    static BEExpressionInfo<String> D_DIFF = (createExpressionInfo("e3", expr("domain", "example.com")));
    static BEExpressionInfo<String> C_BASE = (createExpressionInfo("e4", expr("country", "US", "UK")));
    static BEExpressionInfo<String> C_SAME = (createExpressionInfo("e4", expr("COUNTRY", "UK", "US")));
    static BEExpressionInfo<String> C_DIFF = (createExpressionInfo("e4", expr("country", "AU")));

    BEExpressionHashProvider<String> hashProvider;

    @Before
    public void setUp() {
        final List<BEDataTypeConfig> dataTypeConfigs = Arrays.asList(
                new BEDataTypeConfig("gender", "string", true, false, false, false),
                new BEDataTypeConfig("age", "byte", false, false, true, false),
                new BEDataTypeConfig("domain", "string", true, true, false, true),
                new BEDataTypeConfig("country", "string", false, true, false, false)
        );
        DATA_TYPES = BEDataTypeConfigLookupSupplier.builder()
                .addAllDataTypeConfigs(dataTypeConfigs)
                .defaultDataTypeConfig(BEDataTypeConfigSupplier.CASE_SENSITIVE_STRING_SUPPLIER)
                .build();

        hashProvider = new BEDefaultExpressionHashProvider<>();
    }

    @Test
    public void testComputeHashMatchSingleExpression() {

        int hc1;
        int hc2;

        hc1 = hashProvider.computeHash(G_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(G_SAME, DATA_TYPES);
        assertEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(A_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(A_SAME, DATA_TYPES);
        assertEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(D_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(D_SAME, DATA_TYPES);
        assertEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(C_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(C_SAME, DATA_TYPES);
        assertEquals(hc1, hc2);

    }

    @Test
    public void testComputeHashNotMatchSingleExpression() {

        int hc1;
        int hc2;

        hc1 = hashProvider.computeHash(G_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(G_DIFF, DATA_TYPES);
        assertNotEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(A_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(A_DIFF, DATA_TYPES);
        assertNotEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(D_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(D_DIFF, DATA_TYPES);
        assertNotEquals(hc1, hc2);

        hc1 = hashProvider.computeHash(C_BASE, DATA_TYPES);
        hc2 = hashProvider.computeHash(C_DIFF, DATA_TYPES);
        assertNotEquals(hc1, hc2);

    }

    @Test
    public void testComputeHashNotMatchNegatedExpression() {
        final BEPredicateNode gBaseExpr = (BEPredicateNode) G_BASE.getExpression();
        final BENode gBaseExprNegated = new BEPredicateNode(gBaseExpr.getId(), gBaseExpr.getType(), !gBaseExpr.isNegative(), gBaseExpr.getValues());
        final BEExpressionInfo<String> gBaseNegated = new BEExpressionInfo<>(G_BASE.getData(), gBaseExprNegated);

        final int hc1 = hashProvider.computeHash(G_BASE, DATA_TYPES);
        final int hc2 = hashProvider.computeHash(gBaseNegated, DATA_TYPES);
        assertNotEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashNotMatchDiffInfoExpression() {
        final BEExpressionInfo<String> gBaseDiffInfo =
                new BEExpressionInfo<>(G_BASE.getData().concat("-diff"), G_BASE.getExpression());

        final int hc1 = hashProvider.computeHash(G_BASE, DATA_TYPES);
        final int hc2 = hashProvider.computeHash(gBaseDiffInfo, DATA_TYPES);
        assertNotEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashMatchMultiExpression() {
        final int hc1 = hashProvider.computeHash(Arrays.asList(G_BASE, A_BASE, D_BASE, C_BASE), DATA_TYPES);
        final int hc2 = hashProvider.computeHash(Arrays.asList(C_SAME, D_SAME, A_SAME, G_SAME), DATA_TYPES);
        assertEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashNotMatchMultiExpression() {
        final int hc1 = hashProvider.computeHash(Arrays.asList(G_BASE, A_BASE, D_BASE, C_BASE), DATA_TYPES);
        final int hc2 = hashProvider.computeHash(Arrays.asList(G_DIFF, A_DIFF, D_DIFF, C_DIFF), DATA_TYPES);
        assertNotEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashMatchSingleRefExpression() {
        final BEPredicateNode gBaseExpr = (BEPredicateNode) G_BASE.getExpression();
        final BEPredicateNode gSameExpr = (BEPredicateNode) G_SAME.getExpression();
        final BENode partialExpression1 = new BEPredicateNode("id1", gBaseExpr.getType(), gBaseExpr.isNegative(), gBaseExpr.getValues());
        final BENode partialExpression2 = new BEPredicateNode("id2", gSameExpr.getType(), gSameExpr.isNegative(), gSameExpr.getValues());
        final BENode referenceExpression1 = new BEReferenceNode(Collections.singletonList("id1"));
        final BENode referenceExpression2 = new BEReferenceNode(Collections.singletonList("id2"));
        final Collection<BENode> partialExpressions = Arrays.asList(partialExpression1, partialExpression2);
        final int hc1 = hashProvider.computeHash(new BEExpressionInfo<>("test", referenceExpression1), partialExpressions, DATA_TYPES);
        final int hc2 = hashProvider.computeHash(new BEExpressionInfo<>("test", referenceExpression2), partialExpressions, DATA_TYPES);
        assertEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashMatchMultiRefExpression() {
        final BEPredicateNode gBaseExpr = (BEPredicateNode) G_BASE.getExpression();
        final BEPredicateNode gSameExpr = (BEPredicateNode) G_SAME.getExpression();
        final BENode partialExpression1 = new BEPredicateNode("id1", gBaseExpr.getType(), gBaseExpr.isNegative(), gBaseExpr.getValues());
        final BENode partialExpression2 = new BEPredicateNode("id2", gSameExpr.getType(), gSameExpr.isNegative(), gSameExpr.getValues());
        final BENode referenceExpression1 = new BEReferenceNode(Collections.singletonList("id1"));
        final BENode referenceExpression2 = new BEReferenceNode(Collections.singletonList("id2"));
        final Collection<BENode> partialExpressions = Arrays.asList(partialExpression1, partialExpression2);
        final Collection<BEExpressionInfo<String>> expressions1 = Arrays.asList(
                new BEExpressionInfo<>("test1", referenceExpression1),
                new BEExpressionInfo<>("test2", referenceExpression2));
        final Collection<BEExpressionInfo<String>> expressions2 = Arrays.asList(
                new BEExpressionInfo<>("test2", referenceExpression2),
                new BEExpressionInfo<>("test1", referenceExpression1));
        final int hc1 = hashProvider.computeHash(expressions1, partialExpressions, DATA_TYPES);
        final int hc2 = hashProvider.computeHash(expressions2, partialExpressions, DATA_TYPES);
        assertEquals(hc1, hc2);
    }

    @Test
    public void testComputeHashNotMatchSingleRefExpression() {
        final BEPredicateNode gBaseExpr = (BEPredicateNode) G_BASE.getExpression();
        final BEPredicateNode gDiffExpr = (BEPredicateNode) G_DIFF.getExpression();
        final BENode partialExpression1 = new BEPredicateNode("id1", gBaseExpr.getType(), gBaseExpr.isNegative(), gBaseExpr.getValues());
        final BENode partialExpression2 = new BEPredicateNode("id2", gDiffExpr.getType(), gDiffExpr.isNegative(), gDiffExpr.getValues());
        final BENode referenceExpression1 = new BEReferenceNode(Collections.singletonList("id1"));
        final BENode referenceExpression2 = new BEReferenceNode(Collections.singletonList("id2"));
        final Collection<BENode> partialExpressions = Arrays.asList(partialExpression1, partialExpression2);
        final int hc1 = hashProvider.computeHash(new BEExpressionInfo<>("test", referenceExpression1), partialExpressions, DATA_TYPES);
        final int hc2 = hashProvider.computeHash(new BEExpressionInfo<>("test", referenceExpression2), partialExpressions, DATA_TYPES);
        assertNotEquals(hc1, hc2);
    }
}