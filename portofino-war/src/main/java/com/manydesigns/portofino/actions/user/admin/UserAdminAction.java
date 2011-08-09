/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.actions.user.admin;


import com.manydesigns.portofino.actions.CrudAction;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class UserAdminAction extends CrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
/*

    //**************************************************************************
    // Constants
    //**************************************************************************

    private static final String userTable = "portofino.public.users";
    private static final String groupTable = "portofino.public.groups";
    private static final String usersGroupsTable = "portofino.public.users_groups";
    private final int pwdLength;
    private final Boolean enc;

    //**************************************************************************
    // Injections
    //**************************************************************************


    @InjectHttpSession
    public HttpSession session;

    public UserAdminAction() {
        super();
        this.pwdLength = Integer.parseInt(PortofinoProperties.getProperties()
                .getProperty("pwd.lenght.min","6"));
        enc = Boolean.parseBoolean(PortofinoProperties.getProperties()
                .getProperty(PortofinoProperties.PWD_ENCRYPTED, "false"));

    }


    //**************************************************************************
    // Remove user from Group
    //**************************************************************************

    public String removeGroups() throws NoSuchFieldException {
        if (null==subCrudUnits.get(1).selection) {
            SessionMessages.addInfoMessage("No group selected");
            return PortofinoAction.RETURN_TO_READ;
        }
        for (String current : rootCrudUnit.subCrudUnits.get(1).selection) {
            TableAccessor ugAccessor = context.getTableAccessor(usersGroupsTable);

            TableCriteria criteria = new TableCriteria(ugAccessor.getTable());
            criteria.eq(ugAccessor.getProperty("userid"), Long.parseLong(pk));
            criteria.eq(ugAccessor.getProperty("groupid"), Long.parseLong(current));
            criteria.isNull(ugAccessor.getProperty("deletionDate"));
            
            List<Object> ugList = context.getObjects(criteria);
            for(Object obj : ugList) {
                UsersGroups ug = (UsersGroups) obj;
                ug.setDeletionDate(new Timestamp(new Date().getTime()));
                context.updateObject(usersGroupsTable, ug);
            }
        }
        context.commit("portofino");
        SessionMessages.addInfoMessage("Group(s) removed");
        return PortofinoAction.RETURN_TO_READ;
    }

    //**************************************************************************
    // Add user to Group
    //**************************************************************************

    public String addGroups(){
        String pk = rootCrudUnit.pk;
        if (null==rootCrudUnit.subCrudUnits.get(0).selection) {
            SessionMessages.addInfoMessage("No group selected");
            return PortofinoAction.RETURN_TO_READ;
        }
        for (String current : rootCrudUnit.subCrudUnits.get(0).selection) {
            UsersGroups newUg = new UsersGroups();
            newUg.setCreationDate(new Timestamp(new Date().getTime()));
            newUg.setGroupid(Long.valueOf(current));
            Group pkGrp = new Group(Long.valueOf(current));
            newUg.setGroup((Group) context.getObjectByPk(groupTable, pkGrp));
            newUg.setUserid(Long.valueOf(pk));
            User pkUsr = new User(Long.valueOf(pk));
            newUg.setUser((User) context.getObjectByPk(userTable, pkUsr));

            context.saveObject(usersGroupsTable, newUg);
        }
        context.commit("portofino");
        SessionMessages.addInfoMessage("Group added");
        return PortofinoAction.RETURN_TO_READ;
    }


    //**************************************************************************
    // ResetPassword
    //**************************************************************************

    public String resetPassword() {

        String pk = rootCrudUnit.pk;
        Serializable pkObject = rootCrudUnit.pkHelper.parsePkString(pk);
        User user =  (User)context.getObjectByPk(userTable, pkObject);
        user.passwordGenerator(pwdLength);
        String generatedPwd = user.getPwd();

        final Properties properties = PortofinoProperties.getProperties();

        boolean mailEnabled = Boolean.parseBoolean(
                properties.getProperty(PortofinoProperties.MAIL_ENABLED,
                        "false"));

        if (mailEnabled) {
            String msg = "La tua nuova password è " + generatedPwd;
            Long userId = (Long) session.getAttribute(UserUtils.USERID);
            User thisUser =
            (User) context.getObjectByPk(UserUtils.USERTABLE, new User(userId));
            EmailBean email = new EmailBean("new password", msg,
                    user.getEmail(), thisUser.getEmail());
            context.saveObject(EmailUtils.EMAILQUEUE_TABLE, email);
        } else {
           SessionMessages.addInfoMessage("La nuova password per l'utente è "
                   +generatedPwd);
        }
        if (enc){
            user.encryptPwd();
        }
        String databaseName = model
                .findTableByQualifiedName(userTable).getDatabaseName();
        context.updateObject(userTable, user);
        context.commit(databaseName);

        SessionMessages.addInfoMessage("UPDATE avvenuto con successo");
        return PortofinoAction.RETURN_TO_READ;
    }
    */
}
