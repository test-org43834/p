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

package com.manydesigns.portofino.pages;

import com.manydesigns.elements.annotations.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlAccessorType(value = XmlAccessType.NONE)
public class Self {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String container;
    protected String order;

    //**************************************************************************
    // Actual fields
    //**************************************************************************

    protected int actualOrder;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Self.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Self() {
    }

    public void init() {
        actualOrder = 0;
        if(order != null) {
            try {
                actualOrder = Integer.parseInt(order);
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse value of 'order': " + order, e);
            }
        }
    }

    //**************************************************************************
    // Getters/Setters
    //**************************************************************************

    @XmlAttribute(required = false)
    @Required
    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    @XmlAttribute(required = false)
    @Required
    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getActualOrder() {
        return actualOrder;
    }
}
