package org.semanticweb.HermiT.reasoner;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class AnyURITest extends AbstractReasonerTest {
    protected static final String XSD_ANY_URI=Prefixes.s_semanticWebPrefixes.get("xsd")+"anyURI";

    public AnyURITest(String name) {
        super(name);
    }
    public void testInvalidAnyURILiterals() throws Exception {
        assertEquals(URI.create("http://bla.com/test"),DatatypeRegistry.parseLiteral("http://bla.com/test",XSD_ANY_URI));
        try {
            DatatypeRegistry.parseLiteral("abc 123",XSD_ANY_URI);
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
    }
    public void testLength() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:length",INT("2")),
            OO(AURI("ab"))
        );
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:length",INT("3")),
            OO(AURI("ab"))
        );
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(AURI("ab"))
        );
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(AURI("abcdef"))
        );
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(AURI("abcde"))
        );
    }
    public void testSize() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:length",INT("0"))
        );
        assertDRSatisfiable(false,2,
            DR("xsd:anyURI","xsd:length",INT("0"))
        );
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:length",INT("0")),
            NOT(OO(AURI("")))
        );
    }
    public void testIntersection() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:minLength",INT("0")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("1")))
        );
    }
    public void testPattern1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c+)")),
            OO(AURI("abc"),AURI("abbb"))
        );
        assertDRSatisfiable(false,2,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c+)")),
            OO(AURI("abc"),AURI("abbb"))
        );
    }
    public void testPattern2() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c|d|e)"))
        );
        assertDRSatisfiable(false,4,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c|d|e)"))
        );
    }
    public void testPattern3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c|d|e)")),
            NOT(OO(AURI("abc"),AURI("abd"),AURI("abe")))
        );
    }
    public void testPatternAndLength1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c+)"),"xsd:length",INT("5"))
        );
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4"),"xsd:maxLength",INT("5")),
            NOT(OO(AURI("abcc"),AURI("abccc")))
        );
        assertDRSatisfiable(true,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4")),
            NOT(OO(AURI("abcc"),AURI("abccc")))
        );
    }
    public void testPatternAndLength2() throws Exception {
        ValueSpaceSubset subset=subset("xsd:pattern","ab(c+)","xsd:minLength",5);
        assertTrue(subset.hasCardinalityAtLeast(5000));
        try {
            subset.enumerateDataValues(new ArrayList<Object>());
            fail();
        }
        catch (Exception expected) {
        }
        assertFalse(subset.containsDataValue(URI.create("ab")));
        assertTrue(subset.containsDataValue(URI.create("abccccccccccc")));
    }
    public void testPatternAndLength3() throws Exception {
        ValueSpaceSubset subset=subset("xsd:pattern","ab(c+)","xsd:minLength",5,"xsd:maxLength",10);
        assertTrue(subset.hasCardinalityAtLeast(6));
        assertFalse(subset.hasCardinalityAtLeast(7));
        Set<Object> values=new HashSet<Object>();
        subset.enumerateDataValues(values);
        assertContainsAll(values,
            URI.create("abccc"),
            URI.create("abcccc"),
            URI.create("abccccc"),
            URI.create("abcccccc"),
            URI.create("abccccccc"),
            URI.create("abcccccccc")
        );
    }
    public void testPatternComplement1() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("5")))
        );
        assertDRSatisfiable(false,4,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("5")))
        );
        assertDRSatisfiable(false,
            DR("xsd:anyURI","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:anyURI","xsd:minLength",INT("5"))),
            NOT(OO(AURI("ab"),AURI("abc"),AURI("abcc")))
        );
    }
    public void testComplement2() throws Exception {
        ValueSpaceSubset main=subset("xsd:pattern","ab(c*)");
        DatatypeRestriction restriction=restriction("xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertTrue(intersection.hasCardinalityAtLeast(3));
        assertFalse(intersection.hasCardinalityAtLeast(4));
        Set<Object> values=new HashSet<Object>();
        intersection.enumerateDataValues(values);
        assertContainsAll(values,URI.create("ab"),URI.create("abc"),URI.create("abcc"));
    }
    public void testComplement3() throws Exception {
        ValueSpaceSubset main=subset();
        DatatypeRestriction restriction=restriction("xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue(URI.create("abcde")));
        assertTrue(intersection.containsDataValue(URI.create("abcd")));
    }
    public void testComplement4() throws Exception {
        ValueSpaceSubset main=subset("xsd:pattern","a+");
        DatatypeRestriction restriction=restriction("xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue(URI.create("aaaaa")));
        assertTrue(intersection.containsDataValue(URI.create("aaaa")));
    }
    protected static DatatypeRestriction restriction(Object... arguments) {
        String[] facetURIs=new String[arguments.length/2];
        Object[] facetValues=new Object[arguments.length/2];
        for (int index=0;index<arguments.length;index+=2) {
            facetURIs[index/2]=Prefixes.STANDARD_PREFIXES.expandAbbreviatedURI((String)arguments[index]);
            facetValues[index/2]=arguments[index+1];
        }
        return DatatypeRestriction.create(XSD_ANY_URI,facetURIs,facetValues);
    }
    protected static ValueSpaceSubset subset(Object... arguments) {
        DatatypeRestriction restriction=restriction(arguments);
        return DatatypeRegistry.createValueSpaceSubset(restriction);
    }
}
