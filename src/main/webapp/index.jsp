<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>DB Console</title>
    <link rel="stylesheet" href="css/style.css"/>
    <link rel="stylesheet" href="css/jquery.dataTables.css"/>
    <link rel="stylesheet" href="css/jquery.dataTables_themeroller.css"/>
    <link rel="stylesheet" href="css/codemirror.css"/>
    <link rel="stylesheet" href="doc/docs.css"/>
</head>
<body>

<form>
    <label>DataSource: <select id="dataSources"></select></label>
    <textarea id="sqlInput" name="sqlInput">select table_name from user_tables</textarea>
</form>
<%--<p><strong>MIME types defined:</strong>--%>
    <%--<code><a href="?mime=text/x-sql">text/x-sql</a></code>,--%>
    <%--<code><a href="?mime=text/x-mysql">text/x-mysql</a></code>,--%>
    <%--<code><a href="?mime=text/x-mariadb">text/x-mariadb</a></code>,--%>
    <%--<code><a href="?mime=text/x-cassandra">text/x-cassandra</a></code>,--%>
    <%--<code><a href="?mime=text/x-plsql">text/x-plsql</a></code>,--%>
    <%--<code><a href="?mime=text/x-mssql">text/x-mssql</a></code>.--%>
<%--</p>--%>

<div id="dynamic"></div>

<script src="js/jquery.js"></script>
<script src="js/jquery.dataTables.js"></script>
<script src="js/codemirror.js"></script>
<script src="mode/sql/sql.js"></script>
<script>
(function (window, $, CodeMirror, undef) {
    var mime = 'text/x-mariadb';
    // get mime type
    if (window.location.href.indexOf('mime=') > -1) {
        mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
    }

    CodeMirror.commands.execStmtCmd = function(cm) { execStmt(); };
    CodeMirror.keyMap["default"]["Cmd-Enter"] = "execStmtCmd";
    CodeMirror.keyMap["default"]["Cmd-Enter"] = "execStmtCmd";

    var editor = CodeMirror.fromTextArea(document.getElementById('sqlInput'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets : true,
        autofocus: true
    });

    var dataSources = $('#dataSources');
    var sqlInput = $('#sqlInput');
    var sqlResults = $('#sqlResults');

    function isExecKeyEvent(event) {
        return (event.keyCode === 13 && (event.metaKey || event.ctrlKey));
    }

    function keyHandler(event) {
        if (isExecKeyEvent(event)) {
            execStmt();
        }
    }

    function execStmt() {
//        var sql = sqlInput.val();
        var sql = editor.doc.getValue();
        $.ajax({
            type: 'POST',
            url:  '/api/dbconsole/' + dataSources.find('option:selected').text(),
            data: {q: sql}
        }).done(render)
    }

    function render(data) {
        var msg = data[0][0];
        if (msg.toLowerCase().indexOf('error') >= 0) {
            renderError(msg);
        } else {
            renderTable(data);
        }
    }

    function renderError(msg) {
        $('#dynamic').html($('<p>', { text: msg }));
    }

    function renderTable(data) {

        var aDataSet = data.splice(2);
        var aoHeaders = $.map(data[1], function(n, i) {
            return { "sTitle": n }
        });

        $('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="sqlResults"></table>' );
        $('#sqlResults').dataTable({
            "aaData": aDataSet,
            "aoColumns": aoHeaders
        })
    }

    function renderDataSources(data) {
        // create fragment if we get too many reflows
        $(data).each(function(i, e) {
            var selected = i == 0;
            dataSources.append($('<option>', {
                value: e,
                text: e,
                selected: selected
            }));
        });
    }

    $.ajax({
        type: 'GET',
        url:  '/api/dbconsole'
    }).done(renderDataSources);

//    sqlInput.keydown(keyHandler);
//    sqlInput.focus();
})(window, jQuery, CodeMirror);
</script>
</body>
</html>