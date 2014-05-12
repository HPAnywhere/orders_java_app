<%--
  Created by IntelliJ IDEA.
  User: doronn
  Date: 20/04/13
  Time: 12:48
  To change this template use File | Settings | File Templates.
--%>
<%@page import="com.hp.hpa.platform.propview.PropertiesViewer" %>
<%@page import="java.io.InputStream" %>
<%@page import="java.util.Properties" %>
<%@page import="java.util.Properties" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
<style>
    h1 {color:orange; text-align:center; }

    #svcStatus {
        font-size:20px;
        padding-top: 50px;
    }

    #gitProperties {
        font-size:20px;
        padding-top: 50px;
    }
    #metaInfProperties{
        font-size:20px;
        padding-top: 50px;
    }

    .propertiesView {
        font-size:20px;
    }
    .list {
        display:none;
        height:auto;
        padding:100;
    }
    .show {
        display: none;
    }
    .hide:target + .show {
        display: inline;
    }
    .hide:target {
        display: none;
    }
    .hide:target ~ .list {
        display:inline;
    }

        /*style the (+) and (-) */
    .hide, .show {

        border-radius: 5px;
        font-size: 20px;
        color: black;
        text-shadow: 0 1px 0 #666;
        text-align: left;
        text-decoration: none;
        /*box-shadow: 13px 13px 23px #000;*/
        background: #65e2ff;
        opacity: .95;
        margin-right: 0;
        float: left;
        margin-bottom: 25px;
        width: 300;
    }

    .hide:hover, .show:hover {
        color: #deb887;
        text-shadow: 0 0 1px #666;
        text-decoration: none;
        opacity: 1;
        margin-bottom: 25px;
    }


    .list p{
        height:auto;
        margin:30;
    }

    p.yellow{
        width: 550px;
        padding: 20px;
        background: yellow;
    }
    p.green{
        padding: 20px;
        width: 550px;
        background: green;
    }
    p.blue{
        padding: 20px;
        width: 550px;
        background: blue;
    }

</style>
<%
    String serviceName = null;
    String mfAlert = null;
    String yellowServiceStatus = null;
    String greenServiceStatus = null;
    String blueServiceStatus = null;
    String buildNum = null;
    String miniAppVersion = null;

    InputStream stream = application.getResourceAsStream("/WEB-INF/classes/git.properties");
    Properties props = new Properties();
    if(stream != null) {
        props.load(stream);
        stream.close();
    }

    stream = null;
    stream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
    Properties mfprops = new Properties();
    if(stream != null)  {
        mfprops.load(stream);
        serviceName = mfprops.getProperty("Implementation-Title");
        //System.out.println(mfprops);
        stream.close();
    }else{
        serviceName = "Service";
        mfAlert = "Manifest file could not be found";
    }


    File acManifest = new File("./tomcat/webapps/ROOT/APPCONTAINER.MF");
    Properties acprops = new Properties();
    if (acManifest.exists()){
        System.out.println("File exists");
        stream = null;
        stream = new FileInputStream(acManifest);
        if(stream != null)  {
            acprops.load(stream);
            //appContainerBuildNum = acprops.getProperty("build-number");
            stream.close();
        }
    }else{
        System.out.println("App Container Manifest does not exists");
    }

    File rdManifest = new File("./tomcat/webapps/ROOT/runtime-deployer.MF");
    Properties rdprops = new Properties();
    if (rdManifest.exists()){
        System.out.println("File exists");
        stream = null;
        stream = new FileInputStream(rdManifest);
        if(stream != null)  {
            rdprops.load(stream);
            //appContainerBuildNum = acprops.getProperty("build-number");
            stream.close();
        }
    }else{
        System.out.println("Runtime Deployer Manifest does not exists");
    }

    stream = null;
    stream = application.getResourceAsStream("/META-INF/yellow-history.txt");
    Properties yatsprops = new Properties();
    if(stream != null)  {
        yatsprops.load(stream);
        yellowServiceStatus = yatsprops.getProperty("summery");
        stream.close();
    }else{
        yellowServiceStatus = "No details";
    }

    stream = null;
    stream = application.getResourceAsStream("/META-INF/green-history.txt");
    Properties gatsprops = new Properties();
    if(stream != null)  {
        gatsprops.load(stream);
        greenServiceStatus = gatsprops.getProperty("summery");
        stream.close();
    }else{
        greenServiceStatus = "No details";
    }

    stream = null;
    stream = application.getResourceAsStream("/META-INF/blue-history.txt");
    Properties batsprops = new Properties();
    if(stream != null)  {
        batsprops.load(stream);
        blueServiceStatus = batsprops.getProperty("summery");
        stream.close();
    }else{
        blueServiceStatus = "No details";
    }

    //For IDE Mini-App About.jsp
    stream = null;
    stream = session.getServletContext().getResourceAsStream("/WEB-INF/classes/descriptor.xml");
    //Properties maprops = new Properties();
    if (stream != null){
        System.out.println("descriptor.xml File exists");
        // maprops.loadFromXML(stream);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document doc = documentBuilder.parse(stream);
        //System.out.println(doc);

        NodeList labTestList = doc.getElementsByTagName("miniAppVersion");
        for (int i = 0; i < labTestList.getLength(); ++i){
            Element labTest = (Element) labTestList.item(i);
            miniAppVersion = labTest.getFirstChild().getNodeValue();
            //System.out.println(conditionText);
        }
        stream.close();
    }else{
        System.out.println("Descriptor.xml File does not exists");
    }

%>
<title><%= serviceName %> About </title>

</head>
<body>




<h1 id="pageTitle"><%=serviceName%> About</h1>

<p class="propertiesView"> <b>Build Number:</b>    <%
    if (miniAppVersion != null) {
            out.println(miniAppVersion);
    }
    else if(mfprops != null){
        buildNum = mfprops.getProperty("build-number");
        if (acManifest.exists())
            out.println(buildNum+"/"+acprops.getProperty("AppContainer-Version"));
        else
            out.println(buildNum);
    }



%><br/>
    <%
        if (rdprops != null) {
        if (rdManifest.exists())
        out.println("<br/><b> Container: </b>"+rdprops.getProperty("RuntimDeployer-Version"));
        }

    %><br/>

</p>
<div class="propertiesView"><label style="float: left"><b>Current Status: &nbsp;</b></label> <%
    if (application.getResourceAsStream("/META-INF/green-history.txt") != null)
    out.print("<label style='background-color:blue; float: left'>Blue</label>");
    else if (application.getResourceAsStream("/META-INF/yellow-history.txt") != null)
    out.print("<label style='background-color:green; float: left'>Green</label>");
    else if (application.getResourceAsStream("/META-INF/blue-history.txt") != null)
    out.print("<label style='background-color:grey; float: left'>Grey</label>");
    else if (application.getResourceAsStream("/META-INF/MANIFEST.MF") != null)
    out.print("<label style='background-color:yellow; float: left'>Yellow</label>");


%></div>

<div id="svcStatus">
    <a href="#hide3" class="hide" id="hide3">+ History</a>
    <a href="#show3" class="show" id="show3">- History</a>
    <div class="list">
        <p class="yellow"><b>Yellow Environment:</b><br><%

            if (application.getResourceAsStream("/META-INF/yellow-history.txt") != null)
            out.println(yellowServiceStatus);
            else
            out.println(yellowServiceStatus);
        %></p>
        <p class="green"><b>Green Environment:</b><br><%

            if (application.getResourceAsStream("/META-INF/green-history.txt") != null)
            out.println(greenServiceStatus);
            else
            out.println(greenServiceStatus);
        %></p>
        <p class="blue"><b>Blue Environment:</b><br><%

            if (application.getResourceAsStream("/META-INF/blue-history.txt") != null)
            out.println(blueServiceStatus);
            else
            out.println(blueServiceStatus);
        %></p>
    </div>
</div>

<div id="gitProperties">
    <a href="#hide2" class="hide" id="hide2">+ Git Details</a>
    <a href="#show2" class="show" id="show2">- Git Details</a>
    <div class="list">
        <p class="message"><br><%=PropertiesViewer.getPropertySet(props)%></p>
    </div>
</div>

<div id="metaInfProperties">
    <a href="#hide1" class="hide" id="hide1">+ Manifest Details</a>
    <a href="#show1" class="show" id="show1">- Manifest Details</a>
    <div class="list">
        <p><%
        if (application.getResourceAsStream("/META-INF/MANIFEST.MF") == null) {
            out.println(mfAlert);
        }
    %>
        <p class="message"><br><%=PropertiesViewer.getPropertySet(mfprops)%><br>
            <%
                if (acManifest.exists()) {
                out.println(String.valueOf(PropertiesViewer.getPropertySet(acprops)));
                }
            %>
        </p> </p>
    </div>
</div>


</body>
</html>