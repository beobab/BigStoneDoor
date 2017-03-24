package net.toolan.doorplugin;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 24/03/2017.
 */
public class StringCompressorTest {
    @Test
    public void byteToString() throws Exception {
        byte[] Hello = new byte[] {
                0x48, 0x65, 0x6c, 0x6c, 0x6f
        };

        String result = StringCompressor.byteToString(Hello);

        assertEquals("Hello should equal [H,e,l,l,o]", "Hello", result);
    }

    @Test
    public void stringToBytes() throws Exception {

    }

    @Test
    public void crunch() throws Exception {
        String compressed = StringCompressor.crunch(sampleText);

        assertTrue("Compressed string is shorter than uncompressed string",
                compressed.length() < sampleText.length());
    }

    @Test
    public void decrunch() throws Exception {

    }

    private String sampleText = "A magic string that survives some considerable amount of compression, but is complicated enough that it actually does compress in some form or other..... blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy" +
                                "A magic string that survives some considerable amount of compression, but is complicated enough that it actually does compress in some form or other..... blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy" +
                                "A magic string that survives some considerable amount of compression, but is complicated enough that it actually does compress in some form or other..... blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy" +
                                "A magic string that survives some considerable amount of compression, but is complicated enough that it actually does compress in some form or other..... blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy, blah, blah, blah, easy";

    @Test
    public void compress() throws Exception {
        byte[] compressed = StringCompressor.compress(sampleText);

        assertTrue("Compressed string is shorter than uncompressed string",
                compressed.length < sampleText.length());
    }

    @Test
    public void decompress() throws Exception {
        String result = StringCompressor.decompress(StringCompressor.compress(sampleText));

        assertEquals("Compress and decompress returns same string", sampleText, result);
    }

    @Test
    public void b64encode() throws Exception {
        String actual = StringCompressor.b64encode("Whoa, nelly!");

        assertEquals("Encoding Base64 matches online version", "V2hvYSwgbmVsbHkh", actual);
    }

    @Test
    public void b64decode() throws Exception {
        String actual = StringCompressor.b64decode("TG92ZWx5IHRvIHNlZSB5b3UsIHNpci4=");

        assertEquals("Decoding Base64 matches online version", "Lovely to see you, sir.", actual);
    }

}