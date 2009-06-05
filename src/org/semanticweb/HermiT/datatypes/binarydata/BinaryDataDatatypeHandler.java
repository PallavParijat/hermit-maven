// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.binarydata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeHandler;
import org.semanticweb.HermiT.datatypes.MalformedLiteralException;
import org.semanticweb.HermiT.datatypes.UnsupportedFacetException;
import org.semanticweb.HermiT.datatypes.ValueSpaceSubset;
import org.semanticweb.HermiT.model.DatatypeRestriction;

public class BinaryDataDatatypeHandler implements DatatypeHandler {
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");
    protected static final String XSD_HEX_BINARY=XSD_NS+"hexBinary";
    protected static final String XSD_BASE_64_BINARY=XSD_NS+"base64Binary";
    protected static final ValueSpaceSubset HEX_BINARY_ALL=new BinaryDataValueSpaceSubset(new BinaryDataLengthInterval(BinaryDataType.HEX_BINARY,0,Integer.MAX_VALUE));
    protected static final ValueSpaceSubset BASE_64_BINARY_ALL=new BinaryDataValueSpaceSubset(new BinaryDataLengthInterval(BinaryDataType.BASE_64_BINARY,0,Integer.MAX_VALUE));
    protected static final ValueSpaceSubset EMPTY=new BinaryDataValueSpaceSubset();
    protected static final Set<String> s_managedDatatypeURIs=new HashSet<String>();
    static {
        s_managedDatatypeURIs.add(XSD_HEX_BINARY);
        s_managedDatatypeURIs.add(XSD_BASE_64_BINARY);
    }
    protected static final Set<Class<?>> s_managedDataValueClasses=new HashSet<Class<?>>();
    static {
        s_managedDataValueClasses.add(BinaryData.class);
    }
    protected static final Set<String> s_supportedFacetURIs=new HashSet<String>();
    static {
        s_supportedFacetURIs.add(XSD_NS+"minLength");
        s_supportedFacetURIs.add(XSD_NS+"maxLength");
        s_supportedFacetURIs.add(XSD_NS+"length");
    }

    public Set<String> getManagedDatatypeURIs() {
        return s_managedDatatypeURIs;
    }
    public Set<Class<?>> getManagedDataValueClasses() {
        return s_managedDataValueClasses;
    }
    public String toString(Prefixes prefixes,Object dataValue) {
        BinaryData binaryDataValue=(BinaryData)dataValue;
        String lexicalForm=binaryDataValue.toString();
        if (binaryDataValue.getBinaryDataType()==BinaryDataType.HEX_BINARY)
            return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateURI(XSD_HEX_BINARY);
        else
            return '\"'+lexicalForm+"\"^^"+prefixes.abbreviateURI(XSD_BASE_64_BINARY);
    }
    public Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException {
        assert s_managedDatatypeURIs.contains(datatypeURI);
        BinaryData binaryDataValue;
        if (XSD_HEX_BINARY.equals(datatypeURI))
            binaryDataValue=BinaryData.parseHexBinary(lexicalForm);
        else
            binaryDataValue=BinaryData.parseBase64Binary(lexicalForm);
        if (binaryDataValue==null)
            throw new MalformedLiteralException(lexicalForm,datatypeURI);
        else
            return binaryDataValue;
    }
    public void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_managedDatatypeURIs.contains(datatypeURI);
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            if (!s_supportedFacetURIs.contains(facetURI))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' is not supported on "+Prefixes.STANDARD_PREFIXES.abbreviateURI(datatypeURI)+".");
            Object facetValue=datatypeRestriction.getFacetValue(index);
            if (!(facetValue instanceof Integer))
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' takes only integers as values.");
            int value=(Integer)facetValue;
            if (value<0 || value==Integer.MAX_VALUE)
                throw new UnsupportedFacetException("Facet with URI '"+facetURI+"' does not support integer "+value+" as value.");
        }
    }
    public ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert s_managedDatatypeURIs.contains(datatypeURI);
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0)
            if (XSD_HEX_BINARY.equals(datatypeURI))
                return HEX_BINARY_ALL;
            else
                return BASE_64_BINARY_ALL;
        BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
        if (interval==null)
            return EMPTY;
        else
            return new BinaryDataValueSpaceSubset(interval);
    }
    public ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        assert s_managedDatatypeURIs.contains(datatypeRestriction.getDatatypeURI());
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY)
            return valueSpaceSubset;
        else {
            BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return EMPTY;
            else {
                BinaryDataValueSpaceSubset doubleSubset=(BinaryDataValueSpaceSubset)valueSpaceSubset;
                List<BinaryDataLengthInterval> oldIntervals=doubleSubset.m_intervals;
                List<BinaryDataLengthInterval> newIntervals=new ArrayList<BinaryDataLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    BinaryDataLengthInterval oldInterval=oldIntervals.get(index);
                    BinaryDataLengthInterval intersection=oldInterval.intersectWith(interval);
                    if (intersection!=null)
                        newIntervals.add(intersection);
                }
                if (newIntervals.isEmpty())
                    return EMPTY;
                else
                    return new BinaryDataValueSpaceSubset(newIntervals);
            }
        }
    }
    public ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        if (datatypeRestriction.getNumberOfFacetRestrictions()==0 || valueSpaceSubset==EMPTY)
            return EMPTY;
        else {
            BinaryDataLengthInterval interval=getIntervalFor(datatypeRestriction);
            if (interval==null)
                return valueSpaceSubset;
            else {
                BinaryDataType binaryDataType=(XSD_HEX_BINARY.equals(datatypeURI) ? BinaryDataType.HEX_BINARY : BinaryDataType.BASE_64_BINARY);
                BinaryDataValueSpaceSubset doubleSubset=(BinaryDataValueSpaceSubset)valueSpaceSubset;
                BinaryDataLengthInterval complementInterval1=null;
                if (interval.m_minLength!=0)
                    complementInterval1=new BinaryDataLengthInterval(binaryDataType,0,interval.m_minLength-1);
                BinaryDataLengthInterval complementInterval2=null;
                if (interval.m_maxLength!=Integer.MAX_VALUE)
                    complementInterval2=new BinaryDataLengthInterval(binaryDataType,interval.m_maxLength+1,Integer.MAX_VALUE);
                List<BinaryDataLengthInterval> oldIntervals=doubleSubset.m_intervals;
                List<BinaryDataLengthInterval> newIntervals=new ArrayList<BinaryDataLengthInterval>();
                for (int index=0;index<oldIntervals.size();index++) {
                    BinaryDataLengthInterval oldInterval=oldIntervals.get(index);
                    if (complementInterval1!=null) {
                        BinaryDataLengthInterval intersection=oldInterval.intersectWith(complementInterval1);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                    if (complementInterval2!=null) {
                        BinaryDataLengthInterval intersection=oldInterval.intersectWith(complementInterval2);
                        if (intersection!=null)
                            newIntervals.add(intersection);
                    }
                }
                if (newIntervals.isEmpty())
                    return EMPTY;
                else
                    return new BinaryDataValueSpaceSubset(newIntervals);
            }
        }
    }
    protected BinaryDataLengthInterval getIntervalFor(DatatypeRestriction datatypeRestriction) {
        String datatypeURI=datatypeRestriction.getDatatypeURI();
        assert datatypeRestriction.getNumberOfFacetRestrictions()!=0;
        int minLength=0;
        int maxLength=Integer.MAX_VALUE;
        for (int index=datatypeRestriction.getNumberOfFacetRestrictions()-1;index>=0;--index) {
            String facetURI=datatypeRestriction.getFacetURI(index);
            int facetValue=(Integer)datatypeRestriction.getFacetValue(index);
            if ((XSD_NS+"minLength").equals(facetURI))
                minLength=Math.max(minLength,facetValue);
            else if ((XSD_NS+"maxLength").equals(facetURI))
                maxLength=Math.min(maxLength,facetValue);
            else if ((XSD_NS+"length").equals(facetURI)) {
                minLength=Math.max(minLength,facetValue);
                maxLength=Math.min(maxLength,facetValue);
            } 
            else
                throw new IllegalStateException("Internal error: facet '"+facetURI+"' is not supported by "+Prefixes.STANDARD_PREFIXES.abbreviateURI(datatypeURI)+".");
        }
        BinaryDataType binaryDataType=(XSD_HEX_BINARY.equals(datatypeURI) ? BinaryDataType.HEX_BINARY : BinaryDataType.BASE_64_BINARY);
        if (BinaryDataLengthInterval.isIntervalEmpty(binaryDataType,minLength,maxLength))
            return null;
        else
            return new BinaryDataLengthInterval(binaryDataType,minLength,maxLength);
    }
    public boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        return subsetDatatypeURI.equals(supersetDatatypeURI);
    }
    public boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        return !datatypeURI1.equals(datatypeURI2);
    }
}
