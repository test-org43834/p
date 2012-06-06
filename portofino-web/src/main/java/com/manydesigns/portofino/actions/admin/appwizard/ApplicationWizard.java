/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

package com.manydesigns.portofino.actions.admin.appwizard;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.AdminAction;
import com.manydesigns.portofino.actions.admin.ConnectionProvidersAction;
import com.manydesigns.portofino.actions.forms.ConnectionProviderForm;
import com.manydesigns.portofino.actions.forms.SelectableSchema;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.pageactions.calendar.configuration.CalendarConfiguration;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudConfiguration;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.sync.DatabaseSyncer;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.ActionResolver;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/admin/wizard")
@RequiresAdministrator
public class ApplicationWizard extends AbstractActionBean implements AdminAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String JDBC = "JDBC";
    public static final String JNDI = "JNDI";

    protected Form jndiCPForm;
    protected Form jdbcCPForm;
    protected Form connectionProviderForm;
    protected Field userTableField;
    protected Field userNamePropertyField;
    protected Field userIdPropertyField;
    protected Field userPasswordPropertyField;

    protected String connectionProviderType;
    protected ConnectionProvider connectionProvider;
    protected boolean advanced;

    public TableForm schemasForm;
    protected List<SelectableSchema> selectableSchemas;
    public TableForm rootsForm;
    protected List<SelectableRoot> selectableRoots = new ArrayList<SelectableRoot>();
    protected String userTableName;
    protected String userNameProperty;
    protected String userIdProperty;
    protected String userPasswordProperty;

    protected List<Table> roots;
    protected MultiMap children;
    protected Table userTable;
    protected int columnsInSummary = 5;
    protected int maxDepth = 5;
    protected int depth;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    public static final Logger logger = LoggerFactory.getLogger(ApplicationWizard.class);

    @DefaultHandler
    @Button(list = "select-schemas", key="wizard.prev")
    public Resolution start() {
        buildCPForms();
        return createSelectionProviderForm();
    }

    protected Resolution createSelectionProviderForm() {
        return new ForwardResolution("/layouts/admin/appwizard/create-connection-provider.jsp");
    }

    protected void buildCPForms() {
        jndiCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(ConnectionProvidersAction.jndiEditFields)
                            .configPrefix("jndi")
                            .configMode(Mode.CREATE)
                            .build();
        jdbcCPForm = new FormBuilder(ConnectionProviderForm.class)
                            .configFields(ConnectionProvidersAction.jdbcEditFields)
                            .configPrefix("jdbc")
                            .configMode(Mode.CREATE)
                            .build();

        //Handle back
        jndiCPForm.readFromRequest(context.getRequest());
        jdbcCPForm.readFromRequest(context.getRequest());
    }

    @Buttons({
        @Button(list = "create-connection-provider", key="wizard.next"),
        @Button(list = "select-tables", key="wizard.prev")
    })
    public Resolution createConnectionProvider() {
        buildCPForms();
        if(JDBC.equals(connectionProviderType)) {
            connectionProvider = new JdbcConnectionProvider();
            connectionProviderForm = jdbcCPForm;
        } else if(JNDI.equals(connectionProviderType)) {
            connectionProvider = new JndiConnectionProvider();
            connectionProviderForm = jndiCPForm;
        } else {
            throw new Error("Unknown connection provider type: " + connectionProviderType);
        }
        Database database = new Database();
        database.setConnectionProvider(connectionProvider);
        connectionProvider.setDatabase(database);
        ConnectionProviderForm edit = new ConnectionProviderForm(database);
        connectionProviderForm.readFromRequest(context.getRequest());
        if(connectionProviderForm.validate()) {
            connectionProviderForm.writeToObject(edit);
            return afterCreateConnectionProvider();
        } else {
            return createSelectionProviderForm();
        }
    }

    public Resolution afterCreateConnectionProvider() {
        try {
            configureEditSchemas();
        } catch (Exception e) {
            logger.error("Couldn't read schema names from db", e);
            SessionMessages.addErrorMessage("Couldn't read schema names from db: " + e);
            return createSelectionProviderForm();
        }
        return selectSchemasForm();
    }

    protected Resolution selectSchemasForm() {
        return new ForwardResolution("/layouts/admin/appwizard/select-schemas.jsp");
    }

    protected void configureEditSchemas() throws Exception {
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        Connection conn = connectionProvider.acquireConnection();
        logger.debug("Reading database metadata");
        DatabaseMetaData metadata = conn.getMetaData();
        List<String> schemaNamesFromDb =
                connectionProvider.getDatabasePlatform().getSchemaNames(metadata);
        connectionProvider.releaseConnection(conn);

        List<Schema> selectedSchemas = connectionProvider.getDatabase().getSchemas();

        selectableSchemas = new ArrayList<SelectableSchema>(schemaNamesFromDb.size());
        for(String schemaName : schemaNamesFromDb) {
            boolean selected = false;
            for(Schema schema : selectedSchemas) {
                if(schemaName.equalsIgnoreCase(schema.getSchemaName())) {
                    selected = true;
                    break;
                }
            }
            SelectableSchema schema = new SelectableSchema(schemaName, selected);
            selectableSchemas.add(schema);
        }
        schemasForm = new TableFormBuilder(SelectableSchema.class)
                .configFields(
                        "selected", "schemaName"
                )
                .configMode(Mode.EDIT)
                .configNRows(selectableSchemas.size())
                .configPrefix("schemas_")
                .build();
        schemasForm.readFromObject(selectableSchemas);
        //Handle back
        schemasForm.readFromRequest(context.getRequest());
    }

    @Buttons({
        @Button(list = "select-schemas", key="wizard.next"),
        @Button(list = "select-user-fields", key="wizard.prev")
    })
    public Resolution selectSchemas() {
        createConnectionProvider();
        schemasForm.readFromRequest(context.getRequest());
        if(schemasForm.validate()) {
            schemasForm.writeToObject(selectableSchemas);
            boolean atLeastOneSelected = false;
            for(SelectableSchema schema : selectableSchemas) {
                if(schema.selected) {
                    atLeastOneSelected = true;
                    break;
                }
            }
            if(atLeastOneSelected) {
                if (!addSchemasToModel()) {
                    return selectSchemasForm();
                }
                return afterSelectSchemas();
            } else {
                SessionMessages.addErrorMessage("Select at least a schema");
                return selectSchemasForm();
            }
        }
        return selectSchemasForm();
    }

    protected boolean addSchemasToModel() {
        Database database = connectionProvider.getDatabase();
        for(SelectableSchema schema : selectableSchemas) {
            if(schema.selected) {
                Schema modelSchema = new Schema();
                modelSchema.setSchemaName(schema.schemaName);
                modelSchema.setDatabase(database);
                database.getSchemas().add(modelSchema);
            }
        }
        Database targetDatabase;
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        try {
            targetDatabase = dbSyncer.syncDatabase(new Model());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SessionMessages.addErrorMessage(e.toString());
            return false;
        }
        connectionProvider.setDatabase(targetDatabase);
        connectionProvider.init(application.getDatabasePlatformsManager(), application.getAppDir());
        Model model = new Model();
        model.getDatabases().add(targetDatabase);
        model.init();
        return true;
    }

    public Resolution afterSelectSchemas() {
        children = new MultiHashMap();
        roots = determineRoots(children);
        rootsForm = new TableFormBuilder(SelectableRoot.class)
                .configFields(
                        "selected", "tableName"
                )
                .configMode(Mode.EDIT)
                .configNRows(selectableRoots.size())
                .configPrefix("roots_")
                .build();
        rootsForm.readFromObject(selectableRoots);

        try {
            ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(getClass());
            PropertyAccessor propertyAccessor = classAccessor.getProperty("userTableName");
            DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("userTableName");
            for(SelectableSchema selectableSchema : selectableSchemas) {
                if(selectableSchema.selected) {
                    Schema schema = DatabaseLogic.findSchemaByName(
                            connectionProvider.getDatabase(), selectableSchema.schemaName);

                    List<Table> tables = new ArrayList<Table>(schema.getTables());
                    Collections.sort(tables, new Comparator<Table>() {
                        public int compare(Table o1, Table o2) {
                            return o1.getActualEntityName().compareToIgnoreCase(o2.getActualEntityName());
                        }
                    });
                    for(Table table : tables) {
                        selectionProvider.appendRow(
                                table.getQualifiedName(),
                                schema.getSchemaName() + "." + table.getActualEntityName(),
                                true);
                    }
                }
            }
            userTableField = new SelectField(propertyAccessor, selectionProvider, Mode.CREATE, "");
            //Handle back
            userTableField.readFromRequest(context.getRequest());
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }

        return selectTablesForm();
    }

    protected Resolution selectTablesForm() {
        return new ForwardResolution("/layouts/admin/appwizard/select-tables.jsp");
    }

    protected List<Table> determineRoots(MultiMap children) {
        List<Table> roots = new ArrayList<Table>();
        for(SelectableSchema selectableSchema : selectableSchemas) {
            if(selectableSchema.selected) {
                Schema schema = DatabaseLogic.findSchemaByName(
                        connectionProvider.getDatabase(), selectableSchema.schemaName);
                roots.addAll(schema.getTables());
            }
        }
        for(Iterator<Table> it = roots.iterator(); it.hasNext();) {
            Table table = it.next();

            if(table.getPrimaryKey() == null) {
                SessionMessages.addWarningMessage("Table " + table.getQualifiedName() + " has no primary key and has been skipped.");
                it.remove();
                continue;
            }

            boolean removed = false;
            boolean selected = false; //Così che selected => known
            boolean known = false;

            for(SelectableRoot root : selectableRoots) {
                if(root.tableName.equals(table.getSchemaName() + "." + table.getTableName())) {
                    selected = root.selected;
                    known = true;
                    break;
                }
            }

            if(known && !selected) {
                it.remove();
                removed = true;
            }

            if(!table.getForeignKeys().isEmpty()) {
                for(ForeignKey fk : table.getForeignKeys()) {
                    for(Reference ref : fk.getReferences()) {
                        Column column = ref.getActualToColumn();
                        if(column.getTable() != table) {
                            children.put(column.getTable(), ref);
                            //TODO potrebbe essere un ciclo nel grafo...
                            if(!selected && !removed) {
                                it.remove();
                                removed = true;
                            }
                        }
                    }
                }
            }
            if(!table.getSelectionProviders().isEmpty()) {
                for(ModelSelectionProvider sp : table.getSelectionProviders()) {
                    for(Reference ref : sp.getReferences()) {
                        Column column = ref.getActualToColumn();
                        if(column.getTable() != table) {
                            children.put(column.getTable(), ref);
                            //TODO potrebbe essere un ciclo nel grafo...
                            if(!selected && !removed) {
                                it.remove();
                                removed = true;
                            }
                        }
                    }
                }
            }

            if(!known) {
                SelectableRoot root =
                        new SelectableRoot(table.getSchemaName() + "." + table.getTableName(), !removed);
                selectableRoots.add(root);
            }
        }
        return roots;
    }

    @Button(list = "select-tables", key="wizard.next")
    public Resolution selectTables() {
        //Schemas
        createConnectionProvider();
        schemasForm.readFromRequest(context.getRequest());
        schemasForm.writeToObject(selectableSchemas);
        addSchemasToModel();

        //Roots
        for(SelectableSchema selectableSchema : selectableSchemas) {
            if(selectableSchema.selected) {
                Schema schema = DatabaseLogic.findSchemaByName(
                        connectionProvider.getDatabase(), selectableSchema.schemaName);
                for(Table table : schema.getTables()) {
                    selectableRoots.add(
                            new SelectableRoot(table.getSchemaName() + "." + table.getTableName(), false));
                }
            }
        }

        rootsForm = new TableFormBuilder(SelectableRoot.class)
                .configFields(
                        "selected", "tableName"
                )
                .configMode(Mode.EDIT)
                .configNRows(selectableRoots.size())
                .configPrefix("roots_")
                .build();
        rootsForm.readFromObject(selectableRoots);
        rootsForm.readFromRequest(context.getRequest());
        rootsForm.writeToObject(selectableRoots);

        //Recalc roots
        afterSelectSchemas();
        if(!advanced) {
            SessionMessages.consumeWarningMessages(); //Per non rivedere gli stessi messaggi di prima
        }

        if(roots.isEmpty()) {
            SessionMessages.addWarningMessage("No root table selected");
        }

        userTableField.readFromRequest(context.getRequest());
        userTableField.writeToObject(this);
        if(!StringUtils.isEmpty(userTableName)) {
            Model tmpModel = new Model();
            tmpModel.getDatabases().add(connectionProvider.getDatabase());
            userTable = DatabaseLogic.findTableByQualifiedName(tmpModel, userTableName);

            DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("");
            for(Column column : userTable.getColumns()) {
                selectionProvider.appendRow(
                        column.getActualPropertyName(),
                        column.getActualPropertyName(),
                        true);
            }
            try {
                ClassAccessor classAccessor = JavaClassAccessor.getClassAccessor(getClass());
                PropertyAccessor propertyAccessor = classAccessor.getProperty("userIdProperty");
                userIdPropertyField = new SelectField(propertyAccessor, selectionProvider, Mode.CREATE, "");
                userIdPropertyField.setRequired(true);
                userIdPropertyField.readFromObject(this);
                propertyAccessor = classAccessor.getProperty("userNameProperty");
                userNamePropertyField = new SelectField(propertyAccessor, selectionProvider, Mode.CREATE, "");
                userNamePropertyField.setRequired(true);
                userNamePropertyField.readFromObject(this);
                propertyAccessor = classAccessor.getProperty("userPasswordProperty");
                userPasswordPropertyField = new SelectField(propertyAccessor, selectionProvider, Mode.CREATE, "");
                userPasswordPropertyField.setRequired(true);
                userPasswordPropertyField.readFromObject(this);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
            return selectUserFieldsForm();
        } else {
            return buildAppForm();
        }
    }

    @Button(list = "select-user-fields", key="wizard.next")
    public Resolution selectUserFields() {
        selectTables();
        if(userTable != null) {
            userIdPropertyField.readFromRequest(context.getRequest());
            userNamePropertyField.readFromRequest(context.getRequest());
            userPasswordPropertyField.readFromRequest(context.getRequest());
            if(userIdPropertyField.validate() &&
               userNamePropertyField.validate() &&
               userPasswordPropertyField.validate()) {
                userIdPropertyField.writeToObject(this);
                userNamePropertyField.writeToObject(this);
                userPasswordPropertyField.writeToObject(this);
                return buildAppForm();
            } else {
                return selectUserFieldsForm();
            }
        } else {
            return buildAppForm();
        }
    }

    protected Resolution selectUserFieldsForm() {
        return new ForwardResolution("/layouts/admin/appwizard/select-user-fields.jsp");
    }

    protected Resolution buildAppForm() {
        return new ForwardResolution("/layouts/admin/appwizard/build-app.jsp");
    }

    @Button(list = "build-app", key="wizard.prev")
    public Resolution goBackFromBuildApplication() {
        selectUserFields();
        if(userTable == null) {
            return selectTablesForm();
        } else {
            return selectUserFieldsForm();
        }
    }

    @Button(list = "build-app", key="wizard.finish")
    public Resolution buildApplication() {
        selectUserFields();
        application.getModel().getDatabases().add(connectionProvider.getDatabase());
        application.initModel();
        try {
            application.saveXmlModel();
            String scriptTemplate = PageActionLogic.getScriptTemplate(CrudAction.class);
            List<ChildPage> childPages = new ArrayList<ChildPage>();
            for(Table table : roots) {
                File dir = new File(application.getPagesDir(), table.getActualEntityName());
                depth = 1;
                createCrudPage(dir, table, childPages, scriptTemplate);
            }
            if(userTable != null) {
                setupUsers(childPages, scriptTemplate);
            }
            setupCalendar(childPages);
            Page rootPage = DispatcherLogic.getPage(application.getPagesDir());
            Collections.sort(childPages, new Comparator<ChildPage>() {
                public int compare(ChildPage o1, ChildPage o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            rootPage.getLayout().getChildPages().addAll(childPages);
            DispatcherLogic.savePage(application.getPagesDir(), rootPage);
        } catch (Exception e) {
            logger.error("Errore in sincronizzazione", e);
            SessionMessages.addErrorMessage(
                    "Synchronization error: " +
                            ExceptionUtils.getRootCauseMessage(e));
            application.getModel().getDatabases().remove(connectionProvider.getDatabase());
            application.initModel();
            return buildAppForm();
        }
        SessionMessages.addInfoMessage(getMessage("appwizard.finished"));
        if(userTable != null) {
            SessionMessages.addWarningMessage(getMessage("appwizard.warning.userTable.created"));
        }
        return new RedirectResolution("/");
    }

    protected String getMessage(String key, Object... args) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        String msg = resourceBundle.getString(key);
        return MessageFormat.format(msg, args);
    }

    protected void setupCalendar(List<ChildPage> childPages) throws Exception {
        List<List<String>> calendarDefinitions = new ArrayList<List<String>>();
        Color[] colors = {
                Color.RED, Color.BLUE, Color.CYAN.darker(), Color.GRAY, Color.GREEN.darker(),
                Color.ORANGE, Color.YELLOW.darker(), Color.MAGENTA.darker(), Color.PINK
            };
        int colorIndex = 0;
        List<Table> allTables = new ArrayList<Table>();
        for(Schema schema : connectionProvider.getDatabase().getSchemas()) {
            allTables.addAll(schema.getTables());
        }
        Collections.sort(allTables, new Comparator<Table>() {
            public int compare(Table o1, Table o2) {
                return o1.getActualEntityName().compareToIgnoreCase(o2.getActualEntityName());
            }
        });
        for(Table table : allTables) {
            List<Column> dateColumns = new ArrayList<Column>();
            for(Column column : table.getColumns()) {
                if(Date.class.isAssignableFrom(column.getActualJavaType())) {
                    dateColumns.add(column);
                }
            }
            if(!dateColumns.isEmpty()) {
                //["Cal 1", "db1.schema1.table1", ["column1", "column2"], Color.RED]
                Color color = colors[colorIndex++ % colors.length];
                List<String> calDef = new ArrayList();
                calDef.add('"' + Util.guessToWords(table.getActualEntityName()) + '"');
                calDef.add('"' + table.getQualifiedName() + '"');
                String cols = "[";
                boolean first = true;
                for(Column column : dateColumns) {
                    if(first) {
                        first = false;
                    } else {
                        cols += ", ";
                    }
                    cols += '"' + column.getActualPropertyName() + '"';
                }
                cols += "]";
                calDef.add(cols);
                calDef.add("new java.awt.Color(" + color.getRed() + ", " + color.getGreen() +
                           ", " + color.getBlue() + ")");
                calendarDefinitions.add(calDef);
            }
        }
        if(!calendarDefinitions.isEmpty()) {
            String calendarDefinitionsStr = "[";
            calendarDefinitionsStr += StringUtils.join(calendarDefinitions, ", ");
            calendarDefinitionsStr += "]";
            File dir = new File(application.getPagesDir(), "calendar"); //TODO gestire exists()
            if(dir.mkdirs()) {
                CalendarConfiguration configuration = new CalendarConfiguration();
                DispatcherLogic.saveConfiguration(dir, configuration);

                Page page = new Page();
                page.setId(RandomUtil.createRandomId());
                page.setTitle("Calendar (generated)");
                page.setDescription("Calendar (generated)");

                DispatcherLogic.savePage(dir, page);
                File actionFile = new File(dir, "action.groovy");
                try {
                    TemplateEngine engine = new SimpleTemplateEngine();
                    Template template = engine.createTemplate(getClass().getResource("CalendarPage.groovy.template"));
                    Map<String, Object> bindings = new HashMap<String, Object>();
                    bindings.put("calendarDefinitions", calendarDefinitionsStr);
                    FileWriter fw = new FileWriter(actionFile);
                    template.make(bindings).writeTo(fw);
                    IOUtils.closeQuietly(fw);
                } catch (Exception e) {
                    logger.warn("Couldn't create calendar", e);
                    SessionMessages.addWarningMessage("Couldn't create calendar: " + e);
                    return;
                }

                ChildPage childPage = new ChildPage();
                childPage.setName(dir.getName());
                childPage.setShowInNavigation(true);
                childPages.add(childPage);
            } else {
                logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
                SessionMessages.addWarningMessage("Couldn't create directory " + dir.getAbsolutePath());
            }
        }
    }

    protected void setupUsers(List<ChildPage> childPages, String scriptTemplate) throws Exception {
        if(!roots.contains(userTable)) {
            File dir = new File(application.getPagesDir(), userTable.getActualEntityName());
            depth = 1;
            createCrudPage(dir, userTable, childPages, scriptTemplate);
        }

        List<Reference> references = (List<Reference>) children.get(userTable);
        if(references != null) {
            for(Reference ref : references) {
                depth = 1;
                //TODO prendere solo i riferimenti all'ID, non ad altre colonne
                Column fromColumn = ref.getActualFromColumn();
                Table fromTable = fromColumn.getTable();
                String entityName = fromTable.getActualEntityName();
                String childQuery =
                        "from " + entityName +
                        " where " + fromColumn.getActualPropertyName() +
                        " = %{#securityUtils.getPrincipal(1)}";
                String dirName = "my-" + entityName;
                boolean multipleRoles = false;
                for(Reference ref2 : references) {
                    if(ref2 != ref && ref2.getActualFromColumn().getTable().equals(fromTable)) {
                        multipleRoles = true;
                        break;
                    }
                }
                if(multipleRoles) {
                    dirName += "-as-" + fromColumn.getActualPropertyName();
                }
                File dir = new File(application.getPagesDir(), dirName);
                String title = Util.guessToWords(dirName);
                createCrudPage(
                        dir, fromTable, childQuery,
                        childPages, scriptTemplate, title);
            }
        }
        try {
            TemplateEngine engine = new SimpleTemplateEngine();
            Template template = engine.createTemplate(getClass().getResource("security.groovy"));
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("databaseName", connectionProvider.getDatabase().getDatabaseName());
            bindings.put("userTableEntityName", userTable.getActualEntityName());
            bindings.put("userIdProperty", userIdProperty);
            bindings.put("userNameProperty", userNameProperty);
            bindings.put("passwordProperty", userPasswordProperty);
            FileWriter fw = new FileWriter(new File(application.getAppScriptsDir(), "security.groovy"));
            template.make(bindings).writeTo(fw);
            IOUtils.closeQuietly(fw);
        } catch (Exception e) {
            logger.warn("Couldn't configure users", e);
            SessionMessages.addWarningMessage("Couldn't configure users: " + e);
        }
    }

    protected void createCrudPage(File dir, Table table, List<ChildPage> childPages, String scriptTemplate) throws Exception {
        String query = "from " + table.getActualEntityName();
        String title = Util.guessToWords(table.getActualEntityName());
        createCrudPage(dir, table, query, childPages, scriptTemplate, title);
    }

    protected void createCrudPage(
            File dir, Table table, String query, List<ChildPage> childPages, String scriptTemplate, String title)
            throws Exception {
        if(dir.mkdirs()) {
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.setDatabase(connectionProvider.getDatabase().getDatabaseName());

            configuration.setQuery(query);
            String variable = table.getActualEntityName();
            configuration.setVariable(variable);
            int summ = 0;
            for(Column column : table.getColumns()) {
                CrudProperty crudProperty = new CrudProperty();
                crudProperty.setEnabled(true);
                crudProperty.setName(column.getColumnName());
                crudProperty.setUpdatable(!column.isAutoincrement());
                if(table.getPrimaryKey().getColumns().contains(column) || summ < columnsInSummary) {
                    crudProperty.setInSummary(true);
                    summ++;
                }
                configuration.getProperties().add(crudProperty);
            }
            DispatcherLogic.saveConfiguration(dir, configuration);
            Page page = new Page();
            page.setId(RandomUtil.createRandomId());
            page.setTitle(title);
            page.setDescription(title);

            List<Reference> references = (List<Reference>) children.get(table);
            if(references != null && depth < maxDepth) {
                depth++;
                for(Reference ref : references) {
                    Column fromColumn = ref.getActualFromColumn();
                    Table fromTable = fromColumn.getTable();
                    String entityName = fromTable.getActualEntityName();
                    String propertyName = ref.getActualToColumn().getActualPropertyName();
                    String childQuery =
                            "from " + entityName +
                            " where " + fromColumn.getActualPropertyName() +
                            " = %{#" + variable + "." + propertyName + "}";
                    String childDirName = entityName;
                    /*if(table.getPrimaryKey().getColumns().size() == 1 &&
                       !table.getPrimaryKey().getColumns().contains(ref.getActualToColumn())) {
                        childDirName += "-by-" + propertyName;
                    }*/
                    File childDir = new File(new File(dir, "_detail"), childDirName);
                    if(childDir.exists()) {
                        childDirName += "-as-" + fromColumn.getActualPropertyName();
                        childDir = new File(new File(dir, "_detail"), childDirName);
                    }
                    String childTitle = Util.guessToWords(childDirName);
                    createCrudPage(
                            childDir, fromTable, childQuery,
                            page.getDetailLayout().getChildPages(), scriptTemplate, childTitle);
                }
                Collections.sort(page.getDetailLayout().getChildPages(), new Comparator<ChildPage>() {
                    public int compare(ChildPage o1, ChildPage o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
            }

            DispatcherLogic.savePage(dir, page);
            File actionFile = new File(dir, "action.groovy");
            FileWriter fileWriter = new FileWriter(actionFile);
            IOUtils.write(scriptTemplate, fileWriter);
            IOUtils.closeQuietly(fileWriter);

            ChildPage childPage = new ChildPage();
            childPage.setName(dir.getName());
            childPage.setShowInNavigation(true);
            childPages.add(childPage);
        } else {
            logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
            SessionMessages.addWarningMessage("Couldn't create directory " + dir.getAbsolutePath());
        }
    }

    public Form getJndiCPForm() {
        return jndiCPForm;
    }

    public Form getJdbcCPForm() {
        return jdbcCPForm;
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public boolean isJdbc() {
        return connectionProviderType == null || connectionProviderType.equals(JDBC);
    }

    public boolean isJndi() {
        return StringUtils.equals(connectionProviderType, JNDI);
    }

    public String getActionPath() {
        return (String) getContext().getRequest().getAttribute(ActionResolver.RESOLVED_ACTION);
    }

    public String getConnectionProviderType() {
        return connectionProviderType;
    }

    public void setConnectionProviderType(String connectionProviderType) {
        this.connectionProviderType = connectionProviderType;
    }

    public Form getConnectionProviderForm() {
        return connectionProviderForm;
    }

    public TableForm getSchemasForm() {
        return schemasForm;
    }

    public List<SelectableSchema> getSelectableSchemas() {
        return selectableSchemas;
    }

    public String getUserTableName() {
        return userTableName;
    }

    public void setUserTableName(String userTableName) {
        this.userTableName = userTableName;
    }

    public Field getUserTableField() {
        return userTableField;
    }

    public Field getUserNamePropertyField() {
        return userNamePropertyField;
    }

    public Field getUserIdPropertyField() {
        return userIdPropertyField;
    }

    public Field getUserPasswordPropertyField() {
        return userPasswordPropertyField;
    }

    public String getUserNameProperty() {
        return userNameProperty;
    }

    public void setUserNameProperty(String userNameProperty) {
        this.userNameProperty = userNameProperty;
    }

    public String getUserIdProperty() {
        return userIdProperty;
    }

    public void setUserIdProperty(String userIdProperty) {
        this.userIdProperty = userIdProperty;
    }

    public String getUserPasswordProperty() {
        return userPasswordProperty;
    }

    public void setUserPasswordProperty(String userPasswordProperty) {
        this.userPasswordProperty = userPasswordProperty;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }
}