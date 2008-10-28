package org.amplafi.jawr.maven;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author andyhot
 */
public abstract class AbstractJawrMojo extends AbstractMojo {
    
    /**
     * The maven project.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;    
    
    /**
     * The location of the jawr properties file.
     * 
     * @parameter
     */     
    private String configLocation;    
    
    /**
     * A list of bundles to generate.
     * 
     * @parameter
     */    
    private String[] bundles;
    
    /**
     * The path to the root of the web application.<p/>
     * That's where the resources are loaded from and where the
     * new files are generated.
     * 
     * was "${project.build.directory}/${project.build.finalName}"
     * @parameter default-value="${basedir}/src/main/webapp"
     */         
    private String rootPath;    
    
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setBundles(String... bundles) {
        this.bundles = bundles;
    }

    public String[] getBundles() {
        return bundles;
    }

    public String getConfigLocation() {
        if (project!=null) {
            System.out.println(new File(project.getBasedir(), configLocation).toURI().toString());
            return new File(project.getBasedir(), configLocation).toURI().toString();
        }     
        return configLocation;
    }

    public String getRootPath() {
        return rootPath;
    }

    public MavenProject getProject() {
        return project;
    }    
}
