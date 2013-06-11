/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.elements;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.elements.servlet.WebFramework;
import com.manydesigns.elements.xml.XmlBuffer;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public abstract class AbstractElementsTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public Configuration elementsConfiguration;

    public MutableHttpServletRequest req;

    public final static boolean PRINT_LOGBACK_STATUS = false;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (PRINT_LOGBACK_STATUS) {
            // assume SLF4J is bound to logback in the current environment
            LoggerContext lc =
                    (LoggerContext) LoggerFactory.getILoggerFactory();
            // print logback's internal status
            StatusPrinter.print(lc);
        }
        
        XmlBuffer.checkWellFormed = true;

        setUpProperties();
        setUpSingletons();
        setUpRequest();
        setUpElementsThreadLocals();
    }

    public void setUpProperties() {
        elementsConfiguration = ElementsProperties.getConfiguration();
    }

    public void setUpSingletons() {
        WebFramework.resetSingleton();
    }

    public void setUpRequest() {
        req = new MutableHttpServletRequest();
        req.setContextPath("");
    }

    public void setUpElementsThreadLocals() {
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setHttpServletRequest(req);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        ElementsThreadLocals.removeElementsContext();
    }

}