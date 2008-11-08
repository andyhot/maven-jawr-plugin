//   Copyright 2008 Andreas Andreou
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package org.amplafi.jawr.maven;

import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

/**
 * @author Andreas Andreou
 */
public class TestJawrMojo {
    private static final String EXPECTED_JS_ONE = "\n" +
            "var one={_id:0,_name:'',process:function(name){_name=name;return\"Mr. \"+_name+\" (\"+_id+\")\";}};var two={_name:'',setName:function(name){_name=name;}};";
    private static final String EXPECTED_CSS_ONE = ":link,:visited{text-decoration:none}ul,ol{list-style:none}";

    @Test
    public void testCreateCssBundles() throws IOException {

        final String fileOne = "/css/bundle-one.css";
        final String fileTwo = "/css/bundle-two.css";
        doBundleCreation("/jaws.properties", fileOne, fileTwo);

        String one = readStream(getClass().getResourceAsStream(fileOne));
        String two = readStream(getClass().getResourceAsStream(fileTwo));

        Assert.assertEquals(EXPECTED_CSS_ONE, one);
	    Assert.assertEquals("h1,h2,h3,h4,h5,h6,pre,code{font-size:1em;}address{font-style:normal}", two);
    }

    @Test
    public void testCreateJsBundles() throws IOException {

        final String fileOne = "/js/bundle-one.js";
        final String fileTwo = "/js/bundle-two.js";
        doBundleCreation("/jaws-js.properties", fileOne, fileTwo);

        String one = readStream(getClass().getResourceAsStream(fileOne));
        String two = readStream(getClass().getResourceAsStream(fileTwo));

        Assert.assertEquals(EXPECTED_JS_ONE, one);
	    Assert.assertEquals("\n" +
                "var two={_name:'',setName:function(name){_name=name;}};", two);
    }

    @Test
    public void testCreateBothBundles() throws IOException {

        final String fileOne = "/css/bundle-all-one.css";
        final String fileTwo = "/js/bundle-all-one.js";
        doBundleCreation("/jaws-all.properties", fileOne, fileTwo);

        String one = readStream(getClass().getResourceAsStream(fileOne));
        String two = readStream(getClass().getResourceAsStream(fileTwo));

        Assert.assertEquals(EXPECTED_CSS_ONE, one);
        Assert.assertEquals(EXPECTED_JS_ONE, two);
    }

    private void doBundleCreation(String propertyFile, String... bundles) throws IOException {
        JawrMojo test = new JawrMojo();

        String jawsProperties = getClass().getResource(propertyFile).getFile();
        test.setConfigLocation("file:/" + jawsProperties);

        String root = getClass().getResource("/").getFile();
        test.setRootPath(root);

        test.setBundles(bundles);

        test.createBundles();
    }

    private String readStream(InputStream stream) {
        DataInputStream dis = new DataInputStream(stream);
        byte[] bytes = new byte[8000];
        try {
            int read = dis.read(bytes);
            return new String(bytes, 0, read);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
