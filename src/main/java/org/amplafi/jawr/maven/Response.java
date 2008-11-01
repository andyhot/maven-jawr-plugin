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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author andyhot
 */
public class Response {
    private Map<String, Object> headers;
    private String contentType;
    private String path;
    private int contentLength;
    private int status;
    private StringWriter writer;

    public Response() {
        clear();
    }
    
    public void clear() {
        headers = new HashMap<String, Object>();
        writer = new StringWriter();
        contentLength = 0;
        contentType = null;
        path = null;
        status = 0;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public PrintWriter getWriter() {
        return new PrintWriter(writer);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public String getData() {
        return writer.toString();
    }
    
    @Override
    public String toString() {
        return "Status: " + status + " Length: " + contentLength + 
                " Type: " + contentType + " Path: " + path + 
                "";//"\n" + getData();
    }
    

}
