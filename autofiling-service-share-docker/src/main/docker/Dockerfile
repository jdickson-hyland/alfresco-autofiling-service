FROM ${docker.share.image}:${alfresco.share.docker.version}

ARG TOMCAT_DIR=/usr/local/tomcat

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile extensions/*.jar $TOMCAT_DIR/webapps/share/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile extensions/*.amp $TOMCAT_DIR/amps_share/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps_share $TOMCAT_DIR/webapps/share -directory -nobackup -force

              # Increase Tomcat resource cache to prevent cache-full warnings at startup
RUN sed -i 's/cacheMaxSize="[0-9]*"/cacheMaxSize="100000"/' $TOMCAT_DIR/conf/context.xml; \
    grep -q 'cachingAllowed' $TOMCAT_DIR/conf/context.xml || \
    sed -i 's|<Context>|<Context>\n    <Resources cachingAllowed="true" cacheMaxSize="100000"/>|' \
        $TOMCAT_DIR/conf/context.xml

COPY share-config-custom.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension

COPY log4j2.properties $TOMCAT_DIR/webapps/share/WEB-INF/classes
COPY hotswap-agent.properties $TOMCAT_DIR/webapps/share/WEB-INF/classes
