package org.amplafi.jawr.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
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
    
    public static void main(String[] args) throws IOException {
        JawrMojo test = new JawrMojo();
        test.setConfigLocation("file:/home/andyhot/projects/java/amplafi/amplafi-web/src/main/ext-resources/jaws.properties");
        test.setRootPath("/home/andyhot/projects/java/amplafi/amplafi-web/src/main/webapp");
        test.setBundles("/css/bundle-member.css", "/css/bundle-guest.css");
        test.createBundles();
    }
    
    public void createBundles() throws IOException {
        String type = "css";
        Map contextAttributes = new HashMap();
        contextAttributes.put("javax.servlet.context.tempdir", new File(System.getProperty("java.io.tmpdir")));
        Response respData = new Response();
        
        ServletConfig config = createMock(ServletConfig.class);
        ServletContext context = createMock(ServletContext.class);        
        HttpServletRequest req = createMock(HttpServletRequest.class);
        HttpServletResponse resp = createMock(HttpServletResponse.class);
        
        setupJawrConfig(config, context, contextAttributes, type);
        setupRequest(req, resp, respData);
        
        replay(config, context, req, resp);
        
        JawrServlet jawr = new JawrServlet();
        try {
            jawr.init(config);
            
            ResourceBundlesHandler handler = (ResourceBundlesHandler) context.getAttribute(ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE);
            
            for (String bundle : getBundles()) {
                respData.clear();
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
        renderer.renderBundleLinks(path,"", "", new HashSet(), false, sw);
        return sw.toString();
    }

    private void setupJawrConfig(ServletConfig config, ServletContext context,
            final Map attributes, String type) {        
        expect(config.getServletContext()).andReturn(context).anyTimes();
        expect(config.getServletName()).andReturn("maven-jawr-plugin").anyTimes();
        
        context.log(isA(String.class));
        expectLastCall().anyTimes();

        expect(context.getResourcePaths(isA(String.class))).andAnswer(new IAnswer<Set>() {

            public Set answer() throws Throwable {
                final Set set = new HashSet();
                
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
        
        expect(config.getInitParameterNames()).andReturn(new Vector().elements());
        
        expect(config.getInitParameter("type")).andReturn(type);        
        expect(config.getInitParameter("configLocation")).andReturn(getConfigLocation());
        expect(config.getInitParameter("configPropertiesSourceClass")).andReturn(null);
    }

    private void setupRequest(HttpServletRequest req, HttpServletResponse resp, 
            final Response respData) throws IOException {
        expect(req.getMethod()).andReturn("GET").anyTimes();
        expect(req.getServletPath()).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return respData.getPath();
            }
        }).anyTimes();
        expect(req.getHeader("If-Modified-Since")).andReturn(null).anyTimes();
        
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
