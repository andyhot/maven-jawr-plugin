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

import net.jawr.web.JawrConstant;
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

	public String getResourceType() {
		return JawrConstant.CSS_TYPE;
	}

}
