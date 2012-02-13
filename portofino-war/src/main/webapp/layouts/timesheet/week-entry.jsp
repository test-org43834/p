<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.Activity" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.Entry" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.PersonDay" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.timesheet.model.WeekEntryModel" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page
        import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.Period" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.PeriodFormatter" %>
<%@ page import="org.joda.time.format.PeriodFormatterBuilder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="net.sourceforge.stripes.util.UrlBuilder" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.timesheet.TimesheetAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="timesheet-week-entry" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            div.twe-container {
                overflow-x: auto;
            }
            table.twe-table {
                width: 100%;
            }
            table.twe-table col.day-column {
                width: 60px;
            }
            table.twe-table th, table.twe-table td {
                border-color: #dddddd;
            }
            th.twe-activity {
                background-color: #567050;
                color: white;
            }
            th.twe-day {
                background-color: #94B47B;
                color: white;
            }
            th.twe-day.twe-today {
                background-color: #B4C97E;
            }
            th.twe-day.twe-non-working {
                background-color: #74936C;
            }
            table.twe-table tr.even {
                background-color: #FCFAF3
            }
            td.twe-hours.twe-today {
                background-color: #FFF2B5;
            }
            td.twe-hours.twe-non-working {
                background-color: #F0F8E5;
            }
            td.twe-hours input.twe-input {
                width: 30px;
                text-align: right;
            }
            td.twe-hours img.twe-note {
                float: right;
                margin-top: 4px;
            }
            div.twe-note, div.twe-ro-note {
                float: right;
                margin-top: 4px;
                width: 9px;
                height: 9px;
                background-image: url('<stripes:url value="/layouts/timesheet/empty-note.png"/>');
                cursor: pointer;
            }
            div.twe-note.twe-note-with-content {
                background-image: url('<stripes:url value="/layouts/timesheet/note.png"/>');
            }
            table.twe-table tfoot tr {
                background-color: #F0EAD7;
                color: #625644;
                font-weight: bold;
            }
            td.twe-total {
                text-align: right;
            }
            td.twe-summary.twe-today {
                background-color: #F9EFBE;
            }
            span.twe-ro-hours, span.twe-day-total {
                display: block;
                width: 32px;
                text-align: right;
            }
            span.twe-week-total {
                color: black;
            }
            .display-none {
                display: none;
            }
        </style>
        <%
            WeekEntryModel weekEntryModel =
                    actionBean.getWeekEntryModel();
            XhtmlBuffer xb = new XhtmlBuffer(out);

            Locale locale = request.getLocale();
            DateTimeFormatter longDateFormatter =
                    DateTimeFormat.longDate().withLocale(locale);
        %>
        <div style="float: right">
            <portofino:buttons list="timesheet-we-navigation" cssClass="portletButton" />
        </div>
        Person:
        <%
            xb.write(weekEntryModel.getPerson().getLongName());
        %>
        <br/>
        From:
        <%
            xb.write(longDateFormatter.print(weekEntryModel.getDay(0).getDate()));
        %>
        <br/>
        To:
        <%
            xb.write(longDateFormatter.print(weekEntryModel.getDay(6).getDate()));
        %>
        <hr/>
        <div class="twe-container">
        <table class="twe-table">
            <col/>
            <col/>
            <col/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <thead>
            <tr>
                <th class="twe-activity">Commessa</th>
                <th class="twe-activity">Progetto</th>
                <th class="twe-activity">Attivit�</th>
                <%
                    DateTimeFormatter dayOfWeekFormatter =
                    DateTimeFormat.forPattern("E").withLocale(locale);
                    DateTimeFormatter dateFormatter =
                    DateTimeFormat.shortDate().withLocale(locale);

                    for (int i = 0; i < 7; i++) {
                        WeekEntryModel.Day day = weekEntryModel.getDay(i);
                        DateMidnight dayDate = day.getDate();
                        WeekEntryModel.DayStatus dayStatus = day.getStatus();

                        xb.openElement("th");
                        String htmlClass = "twe-day";
                        if (day.isNonWorking()) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.isToday()) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);


                        xb.openElement("div");
                        xb.write(dayOfWeekFormatter.print(dayDate));
                        if (dayStatus == WeekEntryModel.DayStatus.LOCKED) {
                            xb.openElement("span");
                            xb.addAttribute("class", "ui-icon ui-icon-locked");
                            xb.addAttribute("style", "float: right");
                            xb.addAttribute("title", "locked");
                            xb.closeElement("span");
                        }
                        xb.closeElement("div");

                        xb.write(dateFormatter.print(dayDate));

                        if (dayStatus != null) {
                            String id = "swm-" + i;
                            Integer standardWorkingMinutes =
                                    day.getStandardWorkingMinutes();
                            String value = (standardWorkingMinutes == null)
                                    ? "-"
                                    : Integer.toString(standardWorkingMinutes);
                            xb.writeInputHidden(id, id, value);
                        }

                        xb.closeElement("th");
                    }
                %>
            </tr>
            </thead>
            <tbody>
            <%
                boolean even = false;
                List<Activity> weekActivities = weekEntryModel.getActivities();
                int weekActivitiesSize = weekActivities.size();
                for (int activityIndex = 0; activityIndex < weekActivitiesSize; activityIndex++) {
                    Activity activity = weekActivities.get(activityIndex);
                    xb.openElement("tr");
                    if (even) {
                        xb.addAttribute("class", "even");
                        even = false;
                    } else {
                        xb.addAttribute("class", "odd");
                        even = true;
                    }

                    xb.openElement("td");
                    xb.write(activity.getGroup1());
                    xb.closeElement("td");

                    xb.openElement("td");
                    xb.write(activity.getGroup2());
                    xb.closeElement("td");

                    xb.openElement("td");
                    String description = activity.getDescription();
                    if (StringUtils.isNotBlank(description)) {
                        xb.openElement("a");
                        String acInfoId = "ac-info-" + activity.getId();
                        xb.addAttribute("onclick", "$( '#" + acInfoId + "' ).dialog(); return false;");
                        xb.addAttribute("href", "#");

                        xb.openElement("div");
                        xb.addAttribute("class", "ui-icon ui-icon-info");
                        xb.addAttribute("style", "float: right");
                        xb.closeElement("div");

                        xb.closeElement("a");

                        xb.openElement("div");
                        xb.addAttribute("id", acInfoId);
                        xb.addAttribute("class", "display-none");
                        xb.write(description);
                        xb.closeElement("div");
                    }
                    String readUrl = activity.getReadUrl();
                    if (readUrl == null) {
                        xb.write(activity.getTitle());
                    } else {
                        xb.writeAnchor(readUrl, activity.getTitle());
                    }
                    xb.closeElement("td");

                    for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                        WeekEntryModel.Day day = weekEntryModel.getDay(dayIndex);
                        DateMidnight dayDate = day.getDate();
                        WeekEntryModel.DayStatus dayStatus = day.getStatus();

                        xb.openElement("td");
                        String htmlClass = "twe-hours";
                        if (day.isNonWorking()) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.isToday()) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);

                        if (dayStatus == null) {
                            //xb.write("--");
                        } else {
                            String hours;
                            WeekEntryModel.Entry entry =
                                    day.findEntryByActivity(activity);
                            String note = null;

                            String inputName = String.format("cell-%d-%s",
                                    dayIndex, activity.getId());
                            String noteInputName = String.format("note-%d-%s",
                                    dayIndex, activity.getId());
                            int tabIndex = activityIndex +
                                    dayIndex * weekActivitiesSize;


                            if (entry == null) {
                                hours = null;
                            } else {
                                int minutes = entry.getMinutes();
                                if (minutes == 0) {
                                    hours = null;
                                } else {
                                    hours = String.format("%d:%02d",
                                            minutes / 60,
                                            minutes % 60);
                                }
                                note = entry.getNote();
                            }
                            if (dayStatus == WeekEntryModel.DayStatus.LOCKED) {
                                if (hours == null) {
                                    xb.openElement("span");
                                    xb.addAttribute("class", "twe-ro-hours");
                                    xb.write("--");
                                    xb.closeElement("span");
                                } else {
                                    if (StringUtils.isNotBlank(note)) {
                                        xb.openElement("div");
                                        xb.addAttribute("class", "twe-ro-note");
                                        xb.writeInputHidden(noteInputName, note);
                                        xb.closeElement("div");

                                    }
                                    xb.openElement("span");
                                    xb.addAttribute("class", "twe-ro-hours");
                                    xb.write(hours);
                                    xb.closeElement("span");
                                }
                            } else {
                                xb.openElement("div");
                                xb.addAttribute("class", "twe-note");
                                xb.writeInputHidden(noteInputName, note);
                                xb.closeElement("div");

                                xb.openElement("span");
                                xb.openElement("input");
                                xb.addAttribute("type", "text");
                                xb.addAttribute("inputName", inputName);
                                xb.addAttribute("class", "twe-input");
                                xb.addAttribute("value", hours);
                                xb.addAttribute("tabindex", Integer.toString(tabIndex));
                                xb.closeElement("input");
                                xb.closeElement("span");
                            }
                        }

                        xb.closeElement("td");
                    }

                    xb.closeElement("tr");
                }
            %>
            </tbody>
            <tfoot>
            <tr>
                <td class="twe-total" colspan="3">Totale ore settimanali: <span id="twe-week-total" class="twe-week-total"></span></td>
                <%
                    for (int i = 0; i < 7; i++) {
                        WeekEntryModel.Day day = weekEntryModel.getDay(i);
                        DateMidnight dayDate = day.getDate();
                        WeekEntryModel.DayStatus dayStatus = day.getStatus();

                        xb.openElement("td");
                        String htmlClass = "twe-summary";
                        if (day.isNonWorking()) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.isToday()) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);

                        xb.openElement("span");
                        xb.addAttribute("class", "twe-day-total");
                        xb.closeElement("span");

                        xb.closeElement("td");
                    }
                %>
            </tr>
            </tfoot>
        </table>
    </div>
    <input type="hidden" name="referenceDate" value="<c:out value="${actionBean.referenceDate}"/>"/>
    <input type="hidden" name="personId" value="<c:out value="${actionBean.personId}"/>"/>
        <div id="twe-ro-note-dialog">
            Read-only note dialog
        </div>
        <div id="twe-note-dialog">
            <textarea style="width:300px; height: 100px;">Note dialog</textarea>
        </div>
    <script type="text/javascript">
        var hoursRe = /^(\d+):(\d+)$/;
        var hoursRe2 = /^(\d+):?$/;

        function parseHours(text) {
            var match = hoursRe.exec(text);
            if (match) {
                return parseInt(match[1]) * 60 + parseInt(match[2]);
            } else {
                return 0;
            }
        }
        function formatHoursMinutes(hours, minutes) {
            var totalTime = "" + hours + ":";
            if (minutes < 10) {
                totalTime += "0";
            }
            totalTime += minutes;
            return totalTime;
        }
        function updateDayTotal(index) {
            var totalMinutes = 0;
            $("table.twe-table tbody td:nth-child(" + (4 + index) + ")").each( function(tdIndex, currentTd) {
                $(currentTd).find("span.twe-ro-hours").each(
                        function (spanIndex, currentSpan) {
                            var text = $(currentSpan).html();
                            totalMinutes += parseHours(text);
                        }
                );
                $(currentTd).find("input.twe-input").each(
                        function (inputIndex, currentInput) {
                            var text = $(currentInput).val();
                            totalMinutes += parseHours(text);
                        }
                );
            });

            var hours = Math.floor(totalMinutes / 60);
            var minutes = totalMinutes % 60;
            var totalTime = formatHoursMinutes(hours, minutes);
            $("table.twe-table tfoot tr:first-child td:nth-child(" +
                    (2 + index) +
                    ") span").html(totalTime);
        }

        function updateWeekTotal() {
            var totalMinutes = 0;
            $("span.twe-day-total").each(function(spanIndex, currentSpan) {
                    var text = $(currentSpan).html();
                    totalMinutes += parseHours(text);
            });
            var hours = Math.floor(totalMinutes / 60);
            var minutes = totalMinutes % 60;
            var totalTime = formatHoursMinutes(hours, minutes);
            $("span.twe-week-total").html(totalTime);
        }

        function cellUpdated(wrappedInput, columnIndex) {
            // normalize the input
            var text = wrappedInput.val();
            var match = hoursRe.exec(text);
            var hours = 0;
            var minutes = 0;
            var valid = false;
            if (match) {
                hours = parseInt(match[1]);
                minutes = parseInt(match[2]);
                valid = true;
            } else {
                match = hoursRe2.exec(text);
                if (match) {
                    hours = parseInt(match[1]);
                    valid = true;
                }
            }
            if (valid) {
                // reformat hours/minutes
                var totalTime = formatHoursMinutes(hours, minutes);
                wrappedInput.val(totalTime);
            }
            updateDayTotal(columnIndex);
            updateWeekTotal();
        }

        for (var i = 0; i < 7; i++) {
            updateDayTotal(i);
            $("table.twe-table tbody td:nth-child(" + (4 + i) + ")").each( function(tdIndex, currentTd) {
                $(currentTd).find("input.twe-input").each(
                    function(inputIndex, currentInput) {
                        var j = i;
                        var wrappedInput = $(currentInput);
                        wrappedInput.blur(function() {
                            cellUpdated(wrappedInput, j);
                        });
                    }
                );
            });
        }
        updateWeekTotal();

        // note dialogs initialization
        $("#twe-ro-note-dialog").dialog({
            autoOpen: false,
            height: 200,
            width: 350,
            modal: true,
            buttons: {
                "Close": function() {
                    $(this).dialog( "close" );
                }
            },
            close: function() {
            }
        });

        $("#twe-note-dialog").dialog({
            autoOpen: false,
            height: 200,
            width: 350,
            modal: true,
            buttons: {
                "Save": function() {
                    var dialogWrapper = $(this);
                    var inputWrapper = dialogWrapper.data();
                    var dialogTextarea = dialogWrapper.find("textarea");
                    var text = dialogTextarea.text();
                    inputWrapper.val(text);
                    dialogWrapper.dialog( "close" );
                },
                "Cancel": function() {
                    $(this).dialog( "close" );
                }
            },
            close: function() {
            }
        });

        // note events and display
        $("div.twe-ro-note").click(function() {
            var text = $(this).find("input").val();
            var dialogDiv = $("#twe-ro-note-dialog");
            dialogDiv.text(text);
            dialogDiv.dialog("open");
        });

        $("div.twe-note").click(function() {
            var inputWrapper = $(this).find("input");
            var text = inputWrapper.val();
            var dialogDiv = $("#twe-note-dialog");
            dialogDiv.data(inputWrapper);
            var dialogTextarea = dialogDiv.find("textarea");
            dialogTextarea.text(text);
            dialogDiv.dialog("open");
        });


        var unsavedChanges = false;
        $("table.twe-table input[type='text']").change(function(){
            unsavedChanges = true;
        });
        $("button[name!='saveWeekEntryModel']").click(function() {
            if (unsavedChanges) {
                return confirm("There are unsaved changes. Abandon page anyway?");
            } else {
                return true;
            }
        });
    </script>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>