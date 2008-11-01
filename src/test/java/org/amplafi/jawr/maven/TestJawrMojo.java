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

    @Test
    public void testCreateBundles() throws IOException {
        JawrMojo test = new JawrMojo();

        String jawsProperties = getClass().getResource("/jaws.properties").getFile();
        test.setConfigLocation("file:/" + jawsProperties);

        String root = getClass().getResource("/").getFile();
        test.setRootPath(root);
        
        test.setBundles("/css/bundle-one.css", "/css/bundle-two.css");

        test.createBundles();

        String one = readStream(getClass().getResourceAsStream("/css/bundle-one.css"));
        String two = readStream(getClass().getResourceAsStream("/css/bundle-two.css"));

        Assert.assertEquals(":link,:visited{text-decoration:none}ul,ol{list-style:none}", one);
	    Assert.assertEquals("h1,h2,h3,h4,h5,h6,pre,code{font-size:1em;}address{font-style:normal}", two);
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
