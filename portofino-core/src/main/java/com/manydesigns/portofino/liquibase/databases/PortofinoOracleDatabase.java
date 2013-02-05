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

package com.manydesigns.portofino.liquibase.databases;

import com.manydesigns.portofino.liquibase.LiquibaseUtils;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoOracleDatabase extends OracleDatabase {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Override
    public String escapeDatabaseObject(String objectName) {
        return LiquibaseUtils.escapeDatabaseObject(objectName, "\"");
    }

    @Override
    public String escapeIndexName(String schemaName, String indexName) {
        if (StringUtils.trimToNull(schemaName) == null) {
            return escapeDatabaseObject(indexName);
        } else {
            return escapeDatabaseObject(schemaName) + "." + escapeDatabaseObject(indexName);
        }
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            return getDefaultDatabaseSchemaName();
        } else {
            return requestedSchema;
        }
    }

}
