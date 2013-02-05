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

package com.manydesigns.portofino.database;

import com.manydesigns.portofino.AbstractPortofinoTest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class RoundtripTest extends AbstractPortofinoTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public void setUp() throws Exception {
        super.setUp();
    }

/*    public void testSimpleModel() throws Exception {
        Model model = createSimpleModel();

        // Roundtrip
        Model model2 = doRoundtrip(model);

        // check the database
        assertEquals(1, model2.getDatabases().size());
        Database mydbDatabase2 = model2.getDatabases().get(0);
        assertNotNull(mydbDatabase2);
        assertEquals("mydb", mydbDatabase2.getDatabaseName());

        // check the schema
        assertEquals(1, mydbDatabase2.getSchemas().size());
        Schema publicSchema2 = mydbDatabase2.getSchemas().get(0);
        assertNotNull(publicSchema2);
        assertEquals("PUBLIC", publicSchema2.getSchemaName());

        // check the tables
        assertEquals(2, publicSchema2.getTables().size());

        // check the categoty table
        Table categoryTable2 = publicSchema2.getTables().get(0);
        assertNotNull(categoryTable2);
        checkCategoryTable(categoryTable2);

        // check the product table
        Table productTable2 = publicSchema2.getTables().get(1);
        assertNotNull(productTable2);
        checkProductTable(productTable2);
    }*/

    /**
     * Crea un modello fisico (chiamando createSimplePhysicalModel())
     * e lo arricchisce 

    private Model createSimpleModel() {
        Model model = createSimplePhysicalModel();


        // category table
        Table categoryTable = model.findTableByQualifiedName("mydb.PUBLIC.CATEGORY");
        categoryTable.setJavaClass("com.example.Category");

        Column catidColumn = categoryTable.findColumnByName("CATID");
        catidColumn.setPropertyName("category");
        catidColumn.setJavaType("java.lang.String");

        PrimaryKey categoryPrimaryKey = categoryTable.getPrimaryKey();

        // product table
        Table productTable = model.findTableByQualifiedName("mydb.PUBLIC.PRODUCT");
        productTable.setJavaClass("com.example.Product");

        Annotation productAnnotation =
                new Annotation("com.example.TableAnnotation");
        productTable.getAnnotations().add(productAnnotation);

        Column descnColumn = productTable.findColumnByName("DESCN");
        descnColumn.setPropertyName("description");
        descnColumn.setJavaType("java.lang.String");

        Annotation descnAnnotation =
                new Annotation("com.example.ColumnAnnotation");
        descnAnnotation.getValues().add("value1");
        descnAnnotation.getValues().add("value2");
        descnColumn.getAnnotations().add(descnAnnotation);

        Column productCatidColumn =
                productTable.findColumnByName("PRODUCT_CATID");
        productCatidColumn.setPropertyName("product category");
        productCatidColumn.setJavaType("java.lang.String");

        ForeignKey catidForeignKey =
                productTable.findForeignKeyByName("catid_fk");

        Annotation catidFkAnnotation =
                new Annotation("com.example.ForeignKeyAnnotation");
        catidForeignKey.getAnnotations().add(catidFkAnnotation);

        return model;
    }*/

    /**
     * Crea un modello fisico con due tabelle: CATEGORY e PRODUCT
     *
     * Colonne di CATEGORY:
     * - CATID (chiave primary con nome "category_pk")
     *
     * Colonne di PRODUCT:
     * - DESCN
     * - PRODUCT_CATID (chiave esterna verso CATEGORY.CATID con nome "catid_fk")
     *
     * @return the model
     */
/*    private Model createSimplePhysicalModel() {
        Model model = new Model();

      Database mydbDatabase = new Database("mydb");
        model.getDatabases().add(mydbDatabase);

        Schema publicSchema = new Schema(mydbDatabase, "PUBLIC");
        mydbDatabase.getSchemas().add(publicSchema);

        // category table
        Table categoryTable = new Table(publicSchema, "CATEGORY");
        publicSchema.getTables().add(categoryTable);

        Column catidColumn = new Column(categoryTable, "CATID", "varchar",
                true, true, 50, 1, true);
        categoryTable.getColumns().add(catidColumn);

        PrimaryKey categoryPrimaryKey =
                new PrimaryKey(categoryTable, "category_pk");
        categoryPrimaryKey.getPrimaryKeyColumns().add(
                new PrimaryKeyColumn(categoryPrimaryKey, "CATID")
        );
        categoryTable.setPrimaryKey(categoryPrimaryKey);

        // product table
        Table productTable = new Table(publicSchema, "PRODUCT");
        publicSchema.getTables().add(productTable);

        Column descnColumn = new Column(productTable, "DESCN", "varchar",
                false, false, 255, 0, false);
        productTable.getColumns().add(descnColumn);

        Column productCatidColumn = new Column(productTable,
                "PRODUCT_CATID", "varchar",
                false, false, 255, 0, false);
        productTable.getColumns().add(productCatidColumn);

        ForeignKey catidForeignKey = new ForeignKey(productTable, "catid_fk",
                "mydb", "PUBLIC", "CATEGORY",
                ForeignKey.RULE_CASCADE, ForeignKey.RULE_NO_ACTION);
        catidForeignKey.getReferences().add(
                new Reference(catidForeignKey, "PRODUCT_CATID", "CATID")
        );
        productTable.getForeignKeys().add(catidForeignKey);

        return model;

    }

    private void checkCategoryTable(Table categoryTable2) {
        assertEquals("CATEGORY", categoryTable2.getTableName());
        assertEquals("com.example.Category", categoryTable2.getJavaClass());
        assertNull(categoryTable2.getManyToMany());
        assertEquals(0, categoryTable2.getAnnotations().size());

        // check the column
        assertEquals(1, categoryTable2.getColumns().size());
        Column catidColumn2 = categoryTable2.getColumns().get(0);
        assertNotNull(catidColumn2);
        assertEquals("CATID", catidColumn2.getColumnName());
        assertEquals("varchar", catidColumn2.getColumnType());
        assertEquals(50, catidColumn2.getLength().intValue());
        assertEquals(1, catidColumn2.getScale().intValue());
        assertEquals(true, catidColumn2.isNullable());
        assertEquals(true, catidColumn2.isSearchable());
        assertEquals(true, catidColumn2.isAutoincrement());
        assertEquals("category", catidColumn2.getPropertyName());
        assertEquals("java.lang.String", catidColumn2.getJavaType());

        PrimaryKey categoryPrimaryKey2 = categoryTable2.getPrimaryKey();
        assertNotNull(categoryPrimaryKey2);
        assertEquals("category_pk", categoryPrimaryKey2.getPrimaryKeyName());
        List<PrimaryKeyColumn> primaryKeyColumns =
                categoryPrimaryKey2.getPrimaryKeyColumns();
        assertEquals(1, primaryKeyColumns.size());
        assertEquals("CATID", primaryKeyColumns.get(0).getColumnName());

        // check foreign key
        assertTrue(categoryTable2.getForeignKeys().isEmpty());
    }

    private void checkProductTable(Table productTable2) {
        assertEquals("PRODUCT", productTable2.getTableName());
        assertEquals("com.example.Product", productTable2.getJavaClass());
        assertNull(productTable2.getManyToMany());

        // check the table annotations
        assertEquals(1, productTable2.getAnnotations().size());
        Annotation productAnnotation2 = productTable2.getAnnotations().get(0);
        assertNotNull(productAnnotation2);
        assertEquals("com.example.TableAnnotation", productAnnotation2.getType());
        assertEquals(0, productAnnotation2.getValues().size());

        // check the columns
        assertEquals(2, productTable2.getColumns().size());

        // check descn column
        Column descnColumn2 = productTable2.getColumns().get(0);
        assertNotNull(descnColumn2);
        assertEquals("DESCN", descnColumn2.getColumnName());
        assertEquals("varchar", descnColumn2.getColumnType());
        assertEquals(255, descnColumn2.getLength().intValue());
        assertEquals(0, descnColumn2.getScale().intValue());
        assertEquals(false, descnColumn2.isNullable());
        assertEquals(false, descnColumn2.isSearchable());
        assertEquals(false, descnColumn2.isAutoincrement());
        assertEquals("description", descnColumn2.getPropertyName());
        assertEquals("java.lang.String", descnColumn2.getJavaType());

        // check the column annotations
        assertEquals(1, descnColumn2.getAnnotations().size());
        Annotation columnAnnotation2 = descnColumn2.getAnnotations().get(0);
        assertNotNull(columnAnnotation2);
        assertEquals("com.example.ColumnAnnotation", columnAnnotation2.getType());
        List<String> values = columnAnnotation2.getValues();
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));

        // check product catid column
        Column productCatidColumn2 = productTable2.getColumns().get(1);
        assertNotNull(productCatidColumn2);
        assertEquals("PRODUCT_CATID", productCatidColumn2.getColumnName());
        assertEquals("varchar", productCatidColumn2.getColumnType());
        assertEquals(255, productCatidColumn2.getLength().intValue());
        assertEquals(0, productCatidColumn2.getScale().intValue());
        assertEquals(false, productCatidColumn2.isNullable());
        assertEquals(false, productCatidColumn2.isSearchable());
        assertEquals(false, productCatidColumn2.isAutoincrement());
        assertEquals("product category", productCatidColumn2.getPropertyName());
        assertEquals("java.lang.String", productCatidColumn2.getJavaType());

        // check primary key
        assertNull(productTable2.getPrimaryKey());

        // check foreign key
        assertEquals(1, productTable2.getForeignKeys().size());
        ForeignKey catidForeignKey2 = productTable2.getForeignKeys().get(0);
        assertNotNull(catidForeignKey2);
        assertEquals("catid_fk", catidForeignKey2.getForeignKeyName());
        assertEquals("mydb", catidForeignKey2.getToDatabase());
        assertEquals("PUBLIC", catidForeignKey2.getToSchema());
        assertEquals("CATEGORY", catidForeignKey2.getToTable());
        assertEquals(ForeignKey.RULE_CASCADE, catidForeignKey2.getOnUpdate());
        assertEquals(ForeignKey.RULE_NO_ACTION, catidForeignKey2.getOnDelete());
        List<Reference> catidReferences2 = catidForeignKey2.getReferences();
        assertEquals(1, catidReferences2.size());
        Reference catidReference2 = catidReferences2.get(0);
        assertEquals("PRODUCT_CATID", catidReference2.getFromColumn());
        assertEquals("CATID", catidReference2.getToColumn());
    }

    public void testFullModel() throws Exception {
        Model model = context.getModel();
        doRoundtrip(model);
    }

    private Model doRoundtrip(Model model) throws Exception {
        // Save the model to a file
        File file = File.createTempFile("portofino-model", ".xml");
        JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(model, file);

        // Parse the model from the file into model2
        Unmarshaller um = jc.createUnmarshaller();
        Model model2 = (Model) um.unmarshal(file);
        assertNotNull(model2);

        // compare model and model2
        List<Database> databases = model.getDatabases();
        List<Database> databases2 = model2.getDatabases();
        assertEquals(databases.size(), databases2.size());

        // compare each database separately
        for (int i = 0; i < databases.size(); i++) {
            DatabaseDiff diff = DiffUtil.diff(databases.get(i), databases2.get(i));
            MessageDiffer visitor = new MessageDiffer();
            visitor.diffDatabase(diff);
            List<String> messages = visitor.getMessages();
            if (!messages.isEmpty()) {
                for (String message : messages) {
                    System.out.println(message);
                }
                fail("Differences were found!");
            }
        }

        return model2;
    }

    public void testMergeModels() throws Exception {
        Model sourceModel = createSimplePhysicalModel();
        Model targetModel = createSimpleModel();

        Database sourceDatabase = sourceModel.getDatabases().get(0);
        Database targetDatabase = targetModel.getDatabases().get(0);

        DatabaseDiff databaseDiff = DiffUtil.diff(sourceDatabase, targetDatabase);


        // check empty protlets/pages/use-cases
        //TODO ripristinare
        //assertTrue(targetModel.getPortlets().isEmpty());
        //assertTrue(targetModel.getPages().isEmpty());
        //assertTrue(targetModel.getCrud().isEmpty());

        // check the database
        assertEquals(1, targetModel.getDatabases().size());
        Database mydbDatabase2 = targetModel.getDatabases().get(0);
        assertNotNull(mydbDatabase2);
        assertEquals("mydb", mydbDatabase2.getDatabaseName());

        // check the schema
        assertEquals(1, mydbDatabase2.getSchemas().size());
        Schema publicSchema2 = mydbDatabase2.getSchemas().get(0);
        assertNotNull(publicSchema2);
        assertEquals("PUBLIC", publicSchema2.getSchemaName());

        // check the tables
        assertEquals(2, publicSchema2.getTables().size());

        // check the categoty table
        Table categoryTable2 = publicSchema2.getTables().get(0);
        assertNotNull(categoryTable2);
        checkCategoryTable(categoryTable2);

        // check the product table
        Table productTable2 = publicSchema2.getTables().get(1);
        assertNotNull(productTable2);
        checkProductTable(productTable2);
    }
*/

}
