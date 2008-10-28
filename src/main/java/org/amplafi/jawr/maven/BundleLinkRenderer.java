/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.amplafi.jawr.maven;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.AbstractBundleLinkRenderer;

/**
 *
 * @author andyhot
 */
public class BundleLinkRenderer extends AbstractBundleLinkRenderer {

    public BundleLinkRenderer(ResourceBundlesHandler bundler, boolean useRandomParam) {
        super(bundler, useRandomParam);
    }
    
    @Override
    protected String renderLink(String fullPath) {
        return fullPath;
    }

}
