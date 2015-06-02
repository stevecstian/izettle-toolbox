package com.izettle.msr;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fidde on 02/06/15.
 */
public class Track1DataTest {

    @Test
    public void testSomeMTIPCards() {

        String t1 = null;
        Track1Data t1d = null;

        t1 = "%B5413330056003529^CUST IMP MC 352/^14122059900909900000099909909969929990400?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("CUST IMP MC 352/", t1d.name);
        Assert.assertEquals("205", t1d.serviceCode);

        t1 = "%B5413330056003511^CUST IMP MC 351/^1412101067750500?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("CUST IMP MC 351/", t1d.name);
        Assert.assertEquals("101", t1d.serviceCode);
        Assert.assertEquals("5413330056003511", t1d.pan);

        t1 = "%B5413330056003560^CUST IMP MC 356/ 1^141210100000170099909919769790?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("CUST IMP MC 356/ 1", t1d.name);
        Assert.assertEquals("101", t1d.serviceCode);
        Assert.assertEquals("5413330056003560", t1d.pan);

        t1 = "%B5413330057004062^CUST IMP MC 406/^142512201020730270?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("CUST IMP MC 406/", t1d.name);
        Assert.assertEquals("1425", t1d.expirationDate);
    }

    @Test
    public void testSomeADVTCards() {

        String t1 = null;
        Track1Data t1d = null;

        t1 = "%B4761739001010119^VISA ACQUIRER TEST CARD 01^15122011758900540000000?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("VISA ACQUIRER TEST CARD 01", t1d.name);
        Assert.assertEquals("201", t1d.serviceCode);

        t1 = "%B4761739001010036^VISA ACQUIRER TEST CARD 03^15122011184400799000000?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("VISA ACQUIRER TEST CARD 03", t1d.name);
        Assert.assertEquals("1512", t1d.expirationDate);

        t1 = "%B4761739001010010^VISA ACQUIRER TEST CARD 05^15122011143800575000000?";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("VISA ACQUIRER TEST CARD 05", t1d.name);
        Assert.assertEquals("1143800575000000", t1d.discretionaryData);
    }

    @Test
    public void testSomeRandomCards() {

        String t1 = null;
        Track1Data t1d = null;

        t1 = "%B1234123412341234^fidde was here^030510100000019301000000877000000?;this data matters not";
        t1d = Track1Data.parse(t1);
        Assert.assertEquals("fidde was here", t1d.name);
        Assert.assertEquals("101", t1d.serviceCode);
        Assert.assertEquals("B", t1d.formatCode);
    }

}
