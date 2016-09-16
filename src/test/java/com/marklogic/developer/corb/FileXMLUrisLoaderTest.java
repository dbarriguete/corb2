/*
 * * Copyright (c) 2004-2016 MarkLogic Corporation
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * *
 * * The use of the Apache License does not indicate that this project is
 * * affiliated with the Apache Software Foundation.
 */
package com.marklogic.developer.corb;

import com.marklogic.xcc.ContentSource;
import org.junit.*;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Praveen Venkata
 */
public class FileXMLUrisLoaderTest {

    private static final String ANCHOR1 = "<a href=\"test1.html\">test1</a>";
    private static final String ANCHOR2 = "<a href=\"test2.html\">test2</a>";
    private static final String ANCHOR3 = "<a href=\"test3.html\">test3</a>";
    private static final String ANCHOR4 = "<a href=\"\">\n<!---->\n</a>"; //indent options result in extra carriage returns
    private static final String TEST1 = "test1";
    private static final String TEST2 = "test2";
    private static final String TEST3 = "test3";
    private static final String HTML_SUFFIX = ".html";
    
    /**
     * Test of setOptions method, of class FileUrisXMLLoader.
     */
    @Test
    public void testSetOptions_null() {
        TransformOptions options = null;
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setOptions(options);
            assertNull(instance.options);
        }
    }

    @Test
    public void testSetOptions() {
        TransformOptions options = mock(TransformOptions.class);
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setOptions(options);
            assertEquals(options, instance.options);
        }
    }

    /**
     * Test of setContentSource method, of class FileUrisXMLLoader.
     */
    @Test
    public void testSetContentSource_null() {
        ContentSource cs = null;
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setContentSource(cs);
            assertNull(instance.cs);
        }
    }

    /**
     * Test of setCollection method, of class FileUrisXMLLoader.
     */
    @Test
    public void testSetCollection_null() {
        String collection = null;
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setCollection(collection);
            assertNull(instance.collection);
        }
    }

    @Test
    public void testSetCollection() {
        String collection = "foo";
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setCollection(collection);
            assertEquals(collection, instance.collection);
        }
    }

    /**
     * Test of setProperties method, of class FileUrisXMLLoader.
     */
    @Test
    public void testSetProperties_null() {
        Properties properties = null;
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.setProperties(properties);
            assertNull(instance.properties);
        }
    }

    @Test
    public void testSetProperties_properties() {
        Properties properties = new Properties();
        FileUrisXMLLoader instance = new FileUrisXMLLoader();
        instance.setProperties(properties);
        assertEquals(properties, instance.properties);
        instance.close();
    }

    private FileUrisXMLLoader getDefaultFileUrisXMLLoader() {
        FileUrisXMLLoader instance = new FileUrisXMLLoader();
        TransformOptions options = new TransformOptions();
        Properties props = new Properties();
        props.setProperty(Options.URIS_LOADER, FileUrisLoader.class.getName());
        props.setProperty(Options.XML_FILE, "src/test/resources/xml-file.xml");
        props.setProperty(Options.XML_NODE, "/root/a");
        instance.properties = props;
        instance.options = options;
        return instance;
    }

    /**
     * Test of open method, of class FileUrisXMLLoader.
     */
    @Test
    public void testOpen() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }
        assertEquals(4, nodes.size());
        assertTrue(nodes.contains(ANCHOR1));
        assertTrue(nodes.contains(ANCHOR2));
        assertTrue(nodes.contains(ANCHOR3));
        assertTrue(nodes.contains(ANCHOR4));
    }

    @Test
    public void testOpenWithoutXPath() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.remove(Options.XML_NODE);
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }
        assertEquals(4, nodes.size());
        assertTrue(nodes.contains(ANCHOR1));
        assertTrue(nodes.contains(ANCHOR2));
        assertTrue(nodes.contains(ANCHOR3));
        assertTrue(nodes.contains(ANCHOR4));
    }

    @Test
    public void testSelectRootNode() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "/");
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }
        assertEquals(1, nodes.size());
    }

    @Test
    public void testSelectDocumentElement() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "/*");
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }
        assertEquals(1, nodes.size());
    }

    @Test
    public void testSelectAttributes() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "/root/a/@*");
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }

        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(TEST1 + HTML_SUFFIX));
        assertTrue(nodes.contains(TEST2 + HTML_SUFFIX));
        assertTrue(nodes.contains(TEST3 + HTML_SUFFIX));
    }

    @Test
    public void testSelectTextNodes() throws Exception {
        List<String> nodes;
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "/root/a/text()");
            instance.open();
            assertNotNull(instance.nodeIterator);
            nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
        }
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(TEST1));
        assertTrue(nodes.contains(TEST2));
        assertTrue(nodes.contains(TEST3));
    }

    @Test
    public void testSelectComments() throws Exception {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "//comment()");
            instance.open();
            assertNotNull(instance.nodeIterator);
            List<String> nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
            assertEquals(1, nodes.size());
            assertTrue(nodes.contains("http://test.com/test1.html"));
        }
    }

    @Test
    public void testSelectProcessingInstructions() throws Exception {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "//processing-instruction()");
            instance.open();
            assertNotNull(instance.nodeIterator);
            List<String> nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
            assertEquals(1, nodes.size());
            assertTrue(nodes.contains("http://test.com/test2.html"));
        }
    }

    @Test
    public void testSelectWithUnion() throws Exception {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.setProperty(Options.XML_NODE, "//comment() | //@* | /*/*/text()");
            instance.open();
            assertNotNull(instance.nodeIterator);
            List<String> nodes = new ArrayList<>();
            while (instance.hasNext()) {
                nodes.add(instance.next());
            }
            assertEquals(7, nodes.size());
            //comment()
            assertTrue(nodes.contains("http://test.com/test1.html"));
            //@*
            assertTrue(nodes.contains(TEST1 + HTML_SUFFIX));
            assertTrue(nodes.contains(TEST2 + HTML_SUFFIX));
            assertTrue(nodes.contains(TEST3 + HTML_SUFFIX));
            //text()
            assertTrue(nodes.contains(TEST1));
            assertTrue(nodes.contains(TEST2));
            assertTrue(nodes.contains(TEST3));
        }
    }

    @Test(expected = CorbException.class)
    public void testOpen_fileDoesNotExist() throws Exception {
        FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader();
        instance.properties.setProperty(Options.XML_FILE, "does/not/exit.xml");
        try {
            instance.open();
        } finally {
            instance.close();
        }
        fail();
    }

    /**
     * Test of getBatchRef method, of class FileUrisXMLLoader.
     */
    @Test
    public void testGetBatchRef() {
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            assertNull(instance.getBatchRef());
        }
    }

    /**
     * Test of getTotalCount method, of class FileUrisXMLLoader.
     */
    @Test
    public void testGetTotalCount_defaultValue() {
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            assertEquals(0, instance.getTotalCount());
        }
    }

    @Test
    public void testGetTotalCount() throws CorbException {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.open();
            assertEquals(4, instance.getTotalCount());
        }
    }

    /**
     * Test of hasNext method, of class FileUrisXMLLoader.
     */
    @Test(expected = CorbException.class)
    public void testHasNext_throwException() throws Exception {
        try (FileUrisXMLLoader instance = new FileUrisXMLLoader()) {
            instance.hasNext();
        }
        fail();
    }

    @Test
    public void testHasNext() throws Exception {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.properties.remove(Options.XML_NODE);
            instance.open();
            
            for (int i = 0; i < instance.getTotalCount(); i++) {
                assertTrue(instance.hasNext());
            }
            //Verify that hasNext() does not advance the buffered reader to the next line
            assertTrue(instance.hasNext());
        }
    }

    /**
     * Test of next method, of class FileUrisXMLLoader.
     */
    @Test
    public void testNext() throws Exception {
        try (FileUrisXMLLoader instance = getDefaultFileUrisXMLLoader()) {
            instance.open();
            //Verify that hasNext() does not advance the buffered reader to the next line
            for (int i = 0; i < instance.getTotalCount(); i++) {
                assertNotNull(instance.next());
            }
            assertFalse(instance.hasNext());
            assertNull(instance.next());
        }
    }

    /**
     * Test of close method, of class FileUrisXMLLoader.
     */
    @Test
    public void testClose() {
        FileUrisXMLLoader instance = new FileUrisXMLLoader();
        instance.doc = mock(Document.class);
        instance.close();
        assertNull(instance.doc);
        instance.close();
    }

    /**
     * Test of cleanup method, of class FileUrisXMLLoader.
     */
    @Test
    public void testCleanup() {
        FileUrisXMLLoader instance = new FileUrisXMLLoader();
        instance.doc = mock(Document.class);
        instance.collection = "testCollection";
        instance.cs = mock(ContentSource.class);
        instance.nextUri = "<test>testData</test>";
        instance.options = new TransformOptions();
        instance.properties = new Properties();
        instance.replacements = new String[]{};
        instance.setTotalCount(100);
        instance.close();
        instance.cleanup();
        assertNull(instance.doc);
        assertNull(instance.collection);
        assertNull(instance.cs);
        assertNull(instance.options);
        assertNull(instance.replacements);
    }

}
