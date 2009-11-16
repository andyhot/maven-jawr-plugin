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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.servlet.JawrServlet;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import static org.easymock.EasyMock.*;

/**
 *
 * @author andyhot
 * @goal bundle
 */
public class JawrMojo extends AbstractJawrMojo {
    
    public void createBundles() throws IOException {
        Map<String, Object> contextAttributes = new HashMap<String, Object>();
        contextAttributes.put("javax.servlet.context.tempdir", new File(System.getProperty("java.io.tmpdir")));
        Response respData = new Response();
        
        ServletConfig config = createMock(ServletConfig.class);
        ServletContext context = createMock(ServletContext.class);        
        HttpServletRequest req = createMock(HttpServletRequest.class);
        HttpServletResponse resp = createMock(HttpServletResponse.class);
        
        setupJawrConfig(config, context, contextAttributes, respData);
        setupRequest(req, resp, respData);
        
        replay(config, context, req, resp);
        
        try {

            for (String bundle : getBundles()) {
                respData.clear();
                respData.setTypeFromBundle(bundle);

                JawrServlet jawr = new JawrServlet();
                jawr.init(config);
                String attrName = "css".equals(respData.getType()) ?
                		JawrConstant.CSS_CONTEXT_ATTRIBUTE : JawrConstant.JS_CONTEXT_ATTRIBUTE;  
                ResourceBundlesHandler handler = (ResourceBundlesHandler) context.getAttribute(attrName);

                respData.setPath(createLinkToBundle(handler, bundle));
                jawr.service(req, resp);
                System.out.println(respData);
                File file = new File(getRootPath() + bundle);
                FileUtils.writeStringToFile(file, respData.getData());
            }
            
        } catch (ServletException ex) {
            Logger.getLogger(JawrMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JawrMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    private String createLinkToBundle(ResourceBundlesHandler handler, String path) throws IOException {

        BundleLinkRenderer renderer = new BundleLinkRenderer(handler, true);
        StringWriter sw = new StringWriter();  
        BundleRendererContext ctx = new BundleRendererContext("", "", false, false);
        renderer.renderBundleLinks(path, ctx, sw);
        return sw.toString();
    }

    private void setupJawrConfig(ServletConfig config, ServletContext context, 
    		final Map<String, Object> attributes, final Response respData) {
        expect(config.getServletContext()).andReturn(context).anyTimes();
        expect(config.getServletName()).andReturn("maven-jawr-plugin").anyTimes();
        
        context.log(isA(String.class));
        expectLastCall().anyTimes();

        expect(context.getResourcePaths(isA(String.class))).andAnswer(new IAnswer<Set>() {

            public Set<String> answer() throws Throwable {
                final Set<String> set = new HashSet<String>();
                
                // hack to disallow orphan bundles
                Exception e = new Exception();
                for (StackTraceElement trace : e.getStackTrace()) {
                    if (trace.getClassName().endsWith("OrphanResourceBundlesMapper")) {
                        return set;
                    }
                }
                
                String path = (String) EasyMock.getCurrentArguments()[0];
                File file = new File(getRootPath() + path);                
                
                if (file.exists() && file.isDirectory()) {
                    for (String one : file.list()) {
                        set.add(path + one);
                    }
                }
                
                return set;
            }
            
        }).anyTimes();
        
        expect(context.getResourceAsStream(isA(String.class))).andAnswer(new IAnswer<InputStream>() {

            public InputStream answer() throws Throwable {
                String path = (String) EasyMock.getCurrentArguments()[0];
                File file = new File(getRootPath(), path);
                return new FileInputStream(file);
            }
            
        }).anyTimes();        
        
        expect(context.getAttribute(isA(String.class))).andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                return attributes.get(EasyMock.getCurrentArguments()[0]);
            }
            
        }).anyTimes();
        
        
        context.setAttribute(isA(String.class), isA(Object.class));
        expectLastCall().andAnswer(new IAnswer<Object>(){

            public Object answer() throws Throwable {
                String key = (String) EasyMock.getCurrentArguments()[0];
                Object value = EasyMock.getCurrentArguments()[1];
                attributes.put(key, value);
                return null;
            }
            
        }).anyTimes();
        
        expect(config.getInitParameterNames()).andReturn(new Enumeration<String>() {
        	
			public boolean hasMoreElements() {
				return false;
			}

			public String nextElement() {
				return null;
			}
        	
        }).anyTimes();

        expect(config.getInitParameter(JawrConstant.TYPE_INIT_PARAMETER)).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return respData==null ? null : respData.getType();
            }
        }).anyTimes();

        expect(config.getInitParameter("configLocation")).andReturn(getConfigLocation()).anyTimes();
        expect(config.getInitParameter("configPropertiesSourceClass")).andReturn(null).anyTimes();
    }

    private void setupRequest(HttpServletRequest req, HttpServletResponse resp, final Response respData) throws IOException {
        expect(req.getSession(false)).andReturn(null).anyTimes();
    	expect(req.getMethod()).andReturn("GET").anyTimes();
        expect(req.getServletPath()).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return respData.getPath();
            }
        }).anyTimes();
        expect(req.getHeader("If-Modified-Since")).andReturn(null).anyTimes();        
        expect(req.getHeader("If-None-Match")).andReturn(null).anyTimes();

        resp.setHeader(isA(String.class), isA(String.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                String key = (String) EasyMock.getCurrentArguments()[0];
                String value = (String) EasyMock.getCurrentArguments()[1];
                respData.getHeaders().put(key, value);
                return null;
            }
            
        }).anyTimes();
        
        resp.setDateHeader(isA(String.class), anyLong());
        expectLastCall().andAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                String key = (String) EasyMock.getCurrentArguments()[0];
                long value = (Long) EasyMock.getCurrentArguments()[1];
                respData.getHeaders().put(key, value);
                return null;
            }
            
        }).anyTimes();
        
        resp.setContentType(isA(String.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                respData.setContentType((String) EasyMock.getCurrentArguments()[0]);
                return null;
            }            
        }).anyTimes();    
        
        resp.setContentLength(anyInt());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                respData.setContentLength((Integer) EasyMock.getCurrentArguments()[0]);
                return null;
            }            
        }).anyTimes();
        
        resp.setStatus(anyInt());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                respData.setStatus((Integer) EasyMock.getCurrentArguments()[0]);
                return null;
            }            
        }).anyTimes();        
        
        expect(resp.getWriter()).andAnswer(new IAnswer<PrintWriter>(){
            public PrintWriter answer() throws Throwable {
                return respData.getWriter();
            }            
        }).anyTimes();
        
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createBundles();
        } catch (IOException ex) {
            Logger.getLogger(JawrMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

}
