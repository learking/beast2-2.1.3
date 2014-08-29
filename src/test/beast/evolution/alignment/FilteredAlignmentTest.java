package test.beast.evolution.alignment;


import java.util.Arrays;

import junit.framework.TestCase;
import org.junit.Test;

import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.FilteredAlignment;
import beast.evolution.alignment.Sequence;

/** test FilteredAlignment as well as some aspects of Alignment **/
public class FilteredAlignmentTest extends TestCase {

    static public Alignment getAlignment() throws Exception {
        Sequence human = new Sequence("human", "AAAACCCCGGGGTTTT");
        Sequence chimp = new Sequence("chimp", "ACGTACGTACGTACGT");

        Alignment data = new Alignment();
        data.initByName("sequence", human, "sequence", chimp,
                "dataType", "nucleotide"
        );
        return data;
    }

    static public Alignment getAlignment2() throws Exception {
    	// reordered from getAlignment()
        Sequence human = new Sequence("human", "AAAACCCCTTTTGGGG");
        Sequence chimp = new Sequence("chimp", "ACTGACGTACGTACGT");

        Alignment data = new Alignment();
        data.initByName("sequence", human, "sequence", chimp,
                "dataType", "nucleotide"
        );
        return data;
    }
    
    static public Alignment getAlignment3() throws Exception {
    	// reordered from getAlignment() & with duplicates
        Sequence human = new Sequence("human", "GGGAAA");
        Sequence chimp = new Sequence("chimp", "AGGACA");

        Alignment data = new Alignment();
        data.initByName("sequence", human, "sequence", chimp,
                "dataType", "nucleotide"
        );
        return data;
    }

    @Test
    public void testWeightedSites() throws Exception {
    	// add constant sites to ordinary alignment
        Alignment data = getAlignment();
        try {
        	data.siteWeightsInput.setValue("11232,2,3,4", data);
        	data.initAndValidate();
        	throw new Exception("Should have failed by now");
        } catch (RuntimeException e) {
        	System.err.println(e.getMessage());
        }
        
        // set up weighted alignment
        data = getAlignment();
    	data.siteWeightsInput.setValue("11232, 2, 3, 4 ,1123,2,3,4,112,2,3,4,11,2,3,	4 ", data);
    	data.initAndValidate();
        String weights = Arrays.toString(data.getWeights());
        System.err.println(weights + "\n" + alignmentToString(data, 0) + "\n" + alignmentToString(data, 1));
        assertEquals("[11232, 2, 3, 4, 1123, 2, 3, 4, 112, 2, 3, 4, 11, 2, 3, 4]", weights);
        

        data = getAlignment2();
    	data.siteWeightsInput.setValue("11232, 2, 3, 4 ,1123,2,3,4,112,2,3,4,11,2,3,	4 ", data);
    	data.initAndValidate();
        weights = Arrays.toString(data.getWeights());
        System.err.println(weights + "\n" + alignmentToString(data, 0) + "\n" + alignmentToString(data, 1));
        assertEquals("[11232, 2, 4, 3, 1123, 2, 3, 4, 11, 2, 3, 4, 112, 2, 3, 4]", weights);
        
        data = getAlignment3();
    	data.siteWeightsInput.setValue("1, 10, 100, 1000, 10000, 100000", data);
    	data.initAndValidate();
        weights = Arrays.toString(data.getWeights());
        System.err.println(weights + "\n" + alignmentToString(data, 0) + "\n" + alignmentToString(data, 1));
        assertEquals("[101000, 10000, 1, 110]", weights);
        

        // pass alignment to filtered alignment
        data = getAlignment();
    	data.siteWeightsInput.setValue("11232, 2, 3, 4 ,1123,2,3,4,112,2,3,4,11,2,3,	4 ", data);
    	data.initAndValidate();

        FilteredAlignment data2 = new FilteredAlignment();
        try {
        	data2.initByName("data", data, "filter", "-");
        	throw new Exception("Should have failed by now");        	
        } catch (RuntimeException e) {
        	System.err.println(e.getMessage());
        }
//        weights = Arrays.toString(data2.getWeights());
//        System.err.println(weights);
//        assertEquals("[11232, 2, 3, 4, 1123, 2, 3, 4, 112, 2, 3, 4, 11, 2, 3, 4]", weights);
        
//        data2 = new FilteredAlignment();
//        data2.initByName("data", data, "filter", "3-8");
//        weights = Arrays.toString(data2.getWeights());
//        System.err.println(weights);
//        assertEquals("[4, 1123, 2, 3, 4, 112]", weights);
    }    

    
    @Test
    public void testConstantSites() throws Exception {
        Alignment data = getAlignment();

        FilteredAlignment data2 = new FilteredAlignment();
        // missing one weight for constant sites (nucleotide data)
        try {
            data2.initByName("data", data, "filter", "-", "constantSiteWeights", "11232  2  3");
        	throw new Exception("Should have failed by now");
        } catch (RuntimeException e) {
        	System.err.println(e.getMessage());
        }
        
        // filter alignment with constant sites
        data2 = new FilteredAlignment();
        data2.initByName("data", data, "filter", "-", "constantSiteWeights", "11232  2  3 4");
        String weights = Arrays.toString(data2.getWeights());
        System.err.println(weights);
        assertEquals("[11233, 1, 1, 1, 1, 3, 1, 1, 1, 1, 4, 1, 1, 1, 1, 5]", weights);

        // filter alignment with constant sites, so some constant sites are missing from the original alignment
        data2 = new FilteredAlignment();
        data2.initByName("data", data, "filter", "-8", "constantSiteWeights", "11232 2 3 4");
        weights = Arrays.toString(data2.getWeights());
        System.err.println(weights);
        assertEquals("[11233, 1, 1, 1, 1, 3, 1, 1, 3, 4]", weights);
        
        // add constant sites to ordinary alignment and strip constant sites
        data2 = new FilteredAlignment();
        data2.initByName("data", data, "filter", "-8", "constantSiteWeights", "11232 2 3 4", "strip", true);
        weights = Arrays.toString(data2.getWeights());
        System.err.println(weights);
        assertEquals("[11232, 1, 1, 1, 1, 2, 1, 1, 3, 4]", weights);
    }
    
    @Test
    public void testRangeFiltered() throws Exception {
        Alignment data = getAlignment();
        FilteredAlignment data2 = new FilteredAlignment();
        data2.initByName("data", data, "filter", "1-9");
        assertEquals(9, data2.getSiteCount());
        assertEquals(9, data2.getPatternCount());

        data2.initByName("data", data, "filter", "2-9");
        assertEquals(8, data2.getSiteCount());
        assertEquals(8, data2.getPatternCount());

        data2.initByName("data", data, "filter", "10-");
        assertEquals(7, data2.getSiteCount());

        data2.initByName("data", data, "filter", "-10");
        assertEquals(10, data2.getSiteCount());

        data2.initByName("data", data, "filter", "-");
        assertEquals(16, data2.getSiteCount());

        data2.initByName("data", data, "filter", "2-5,7-10");
        assertEquals(8, data2.getSiteCount());
        assertEquals(8, data2.getPatternCount());
    }

    @Test
    public void testIteratorFiltered() throws Exception {
        Alignment data = getAlignment();
        FilteredAlignment data2 = new FilteredAlignment();
        data2.initByName("data", data, "filter", "1:16:2");
        assertEquals(8, data2.getSiteCount());
        assertEquals(8, data2.getPatternCount());

        int iPattern = data2.getPatternIndex(0);
        int[] pattern = data2.getPattern(iPattern);
        assertEquals(0, pattern[0]);
        assertEquals(0, pattern[1]);

        data2.initByName("data", data, "filter", "2:16:2");
        assertEquals(8, data2.getSiteCount());

        iPattern = data2.getPatternIndex(0);
        pattern = data2.getPattern(iPattern);
        assertEquals(0, pattern[0]);
        assertEquals(1, pattern[1]);

        data2.initByName("data", data, "filter", "1:10:2");
        assertEquals(5, data2.getSiteCount());

        data2.initByName("data", data, "filter", "1::3");
        assertEquals(6, data2.getSiteCount());

        data2.initByName("data", data, "filter", "2::3");
        assertEquals(5, data2.getSiteCount());

        data2.initByName("data", data, "filter", "::");
        assertEquals(16, data2.getSiteCount());

        data2.initByName("data", data, "filter", "2:5:");
        assertEquals(4, data2.getSiteCount());

        data2.initByName("data", data, "filter", ":5:");
        assertEquals(5, data2.getSiteCount());

        data2.initByName("data", data, "filter", "1::3,2::3");
        assertEquals(11, data2.getSiteCount());

//        System.out.println(alignmentToString(data2, 1));

        data2.initByName("data", data, "filter", "3::3");
        assertEquals(5, data2.getSiteCount());

//        System.out.println(alignmentToString(data2, 1));
    }

    String alignmentToString(Alignment data, int iTaxon) throws Exception {
        int[] nStates = new int[data.getPatternCount()];
//        for (int i = 0; i < data.getSiteCount(); i++) {
//            int iPattern = data.getPatternIndex(i);
//            int[] sitePattern = data.getPattern(iPattern);
//            nStates[i] = sitePattern[iTaxon];
//        }

        for (int i = 0; i < data.getPatternCount(); i++) {
            int[] sitePattern = data.getPattern(i);
            nStates[i] = sitePattern[iTaxon];
        }
        return data.getDataType().state2string(nStates);
    }
}
