/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.ReturnFields;
import org.junit.Assert;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ReturnFieldsTest {

    @Test
    public void testBasic() {
        String spec = "field1,field2,field3";
        DefaultReturnFields fspec = new DefaultReturnFields(spec);

        StringBuilder val = new StringBuilder();
        for (String field : fspec) {
            if (val.length() > 0)
                val.append(',');
            val.append(field);
        }
        Assert.assertEquals(spec, val.toString());

        // check catching errors

        String[] specs = {
                "",
                null,
                ",",
                "field1,",
                ",field2"
        };

        for (String filter : specs) {
            try {
                fspec = new DefaultReturnFields(filter);
                Assert.fail("Parsing of fields spec should have failed! : " + filter);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    @Test
    public void testExclude() {
        DefaultReturnFields spec = new DefaultReturnFields("*,-name,dog(*,-color)");

        assertThat(spec.included("foo")).isTrue();
        assertThat(spec.included("bar")).isTrue();
        assertThat(spec.included("name")).isFalse();
        assertThat(spec.included("dog")).isTrue();
        assertThat(spec.child("dog").included("breed")).isTrue();
        assertThat(spec.child("dog").included("color")).isFalse();

        assertThat(spec.excluded("name")).isTrue();
        assertThat(spec.excluded("foo")).isFalse();
        assertThat(spec.excluded("bar")).isFalse();
        assertThat(spec.child("dog").excluded("color")).isTrue();
        assertThat(spec.child("dog").excluded("breed")).isFalse();
    }

    @Test
    public void testNestedWithGlob() {
        DefaultReturnFields spec = new DefaultReturnFields("name,dog(*)");

        assertThat(spec.included("name")).isTrue();
        assertThat(spec.included("tacos")).isFalse();

        assertThat(spec.child("dog")).isNotNull();
        assertThat(spec.child("dog").included("dogname")).isTrue();

        assertThat(spec.child("cat")).isNotNull();
        assertThat(spec.child("cat").included("name")).isFalse();
    }

    @Test
    public void testNested() {
        String spec = "field1,field2(sub1,sub2(subsub1)),field3";
        DefaultReturnFields fspec = new DefaultReturnFields(spec);

        String val = traverse(fspec);
        Assert.assertEquals(spec, val.toString());


        // check catching errors

        String[] specs = {
                "(",
                ")",
                "field1,(",
                "field1,)",
                "field1,field2(",
                "field1,field2)",
                "field1,field2()",
                "field1,field2(sub1)(",
                "field1,field2(sub1))",
                "field1,field2(sub1),"
        };

        for (String filter : specs) {
            try {
                fspec = new DefaultReturnFields(filter);
                Assert.fail("Parsing of fields spec should have failed! : " + filter);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private String traverse(ReturnFields fspec) {
        StringBuilder buf = new StringBuilder();
        for (String field : fspec) {
            if (buf.length() > 0)
                buf.append(',');
            buf.append(field);

            ReturnFields cspec = fspec.child(field);
            if (cspec != null && cspec != ReturnFields.NONE) {
                buf.append('(');
                buf.append(traverse(cspec));
                buf.append(')');
            }
        }
        return buf.toString();
    }
}
