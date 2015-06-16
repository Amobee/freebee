package com.amobee.freebee.evaluator.evaluator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;

import static org.junit.Assert.*;

public class BEInputTest
{

    @Test
    public void testBeInputCloned()
    {
        final BEInput input = new BEInput();
        input.getOrCreateByteCategory("b1").add((byte) 10);
        input.getOrCreateDoubleCategory("d1").add(20D);
        input.getOrCreateIntCategory("i1").add(30);
        input.getOrCreateLongCategory("l1").add(40);
        input.getOrCreateStringCategory("s1").add("50");

        final BEInput cloned = input.clone();

        cloned.getOrCreateByteCategory("b1").add((byte) 11);
        cloned.getOrCreateDoubleCategory("d1").add(21D);
        cloned.getOrCreateIntCategory("i1").add(31);
        cloned.getOrCreateLongCategory("l1").add(41);
        cloned.getOrCreateStringCategory("s1").add("51");

        assertEquals(ByteLists.immutable.of((byte) 10), ((BEByteInputAttributeCategory) input.getCategory("b1")).getValues());
        assertEquals(ByteLists.immutable.of((byte) 10, (byte) 11), ((BEByteInputAttributeCategory) cloned.getCategory("b1")).getValues());

        assertEquals(DoubleLists.immutable.of(20), ((BEDoubleInputAttributeCategory) input.getCategory("d1")).getValues());
        assertEquals(DoubleLists.immutable.of(20, 21), ((BEDoubleInputAttributeCategory) cloned.getCategory("d1")).getValues());

        assertEquals(IntLists.immutable.of(30), ((BEIntInputAttributeCategory) input.getCategory("i1")).getValues());
        assertEquals(IntLists.immutable.of(30, 31), ((BEIntInputAttributeCategory) cloned.getCategory("i1")).getValues());

        assertEquals(LongLists.immutable.of(40), ((BELongInputAttributeCategory) input.getCategory("l1")).getValues());
        assertEquals(LongLists.immutable.of(40, 41), ((BELongInputAttributeCategory) cloned.getCategory("l1")).getValues());

        assertEquals(Lists.immutable.of("50"), ((BEStringInputAttributeCategory) input.getCategory("s1")).getValues());
        assertEquals(Lists.immutable.of("50", "51"), ((BEStringInputAttributeCategory) cloned.getCategory("s1")).getValues());
    }

    @Test
    public void testBeInputRemoved()
    {
        final BEInput input = new BEInput();
        input.getOrCreateStringCategory("s1").add("s");

        input.removeCategory("s1");

        assertNull(input.getCategory("s1"));
    }

}