#!/bin/sh
cur=`dirname $0`;

test $# -eq 1 || {
    echo "usage: $0 <gwt directory>"
    exit 1
}

test -d $1 -a -f $1/gwt-dev.jar || {
    echo "$1 doesn't look like a valid GWT installation"
    exit 1
}

gwt_home=$1
shell_cp=${cur}/src:${cur}/bin:${gwt_home}/gwt-user.jar:${gwt_home}/gwt-dev.jar:theme/minig-theme.jar:lib-compile/gwt-dnd-2.6.4.jar:lib-compile/xmpp4get-1.0.0.jar

for i in lib-runtime/*.jar; do
    shell_cp=${shell_cp}:$i
done

java=java
test -d /usr/lib/jvm/ia32-java-6-sun && {
    java=/usr/lib/jvm/ia32-java-6-sun/bin/java
}

cat > ${cur}/run_in_shell.sh <<EOF
#!/bin/bash

test -d bin || {
    echo "bin directory missing. Import & build the project in eclipse first."
}

${java} -cp "${shell_cp}" \
com.google.gwt.dev.HostedMode \
-startupUrl WebmailUI.html fr.aliasource.webmail.WebmailUI
EOF

chmod +x ${cur}/run_in_shell.sh

cat > ${cur}/WebmailUI.launch <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<launchConfiguration type="org.eclipse.jdt.launching.localJavaApplication">
<booleanAttribute key="org.eclipse.jdt.launching.DEFAULT_CLASSPATH" value="false"/>
<stringAttribute key="org.eclipse.jdt.launching.MAIN_TYPE" value="com.google.gwt.dev.GWTShell"/>
<listAttribute key="org.eclipse.jdt.launching.CLASSPATH">
<listEntry value="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#13;&#10;&lt;runtimeClasspathEntry containerPath=&quot;org.eclipse.jdt.launching.JRE_CONTAINER&quot; javaProject=&quot;WebmailUI&quot; path=&quot;1&quot; type=&quot;4&quot;/&gt;&#13;&#10;"/>
<listEntry value="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#13;&#10;&lt;runtimeClasspathEntry internalArchive=&quot;/WebmailUI/src&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#13;&#10;"/>
<listEntry value="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#13;&#10;&lt;runtimeClasspathEntry id=&quot;org.eclipse.jdt.launching.classpathentry.defaultClasspath&quot;&gt;&#13;&#10;&lt;memento project=&quot;WebmailUI&quot;/&gt;&#13;&#10;&lt;/runtimeClasspathEntry&gt;&#13;&#10;"/>
<listEntry value="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#13;&#10;&lt;runtimeClasspathEntry externalArchive=&quot;${gwt_home}/gwt-dev-linux.jar&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#13;&#10;"/>
<listEntry value="&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry internalArchive=&quot;/WebmailUI/theme/minig-theme.jar&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;"/>
</listAttribute>
<stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value=""/>
<stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="-out www fr.aliasource.webmail.WebmailUI/WebmailUI.html"/>
<stringAttribute key="org.eclipse.jdt.launching.PROJECT_ATTR" value="WebmailUI"/>
<booleanAttribute key="org.eclipse.debug.core.appendEnvironmentVariables" value="true"/>
</launchConfiguration>
EOF

cat > ${cur}/`whoami`.build.properties <<EOF
gwt=${gwt_home}
EOF

tc_doc_base=`pwd`/dist

cat > ${cur}/run_in_tomcat.sh <<EOF0
#!/bin/bash

test -f apache-tomcat-6.0.18.tar.gz || {
    echo "apache-tomcat-6.0.18.tar.gz missing in ${cur}"
    exit 1
}

test -d dist || {
    echo "No dist directory found, building."
    ant dist
}

rm -fr apache-tomcat-6.0.18
tar xfz apache-tomcat-6.0.18.tar.gz
rm -fr apache-tomcat-6.0.18/webapps/ROOT
mkdir -p apache-tomcat-6.0.18/conf/Catalina/localhost/
cat > apache-tomcat-6.0.18/conf/Catalina/localhost/ROOT.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<Context path="" docBase="${tc_doc_base}">

</Context>
EOF

${cur}/apache-tomcat-6.0.18/bin/catalina.sh run

EOF0
chmod +x ${cur}/run_in_tomcat.sh
