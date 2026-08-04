FROM ${docker.acs.image}:${alfresco.platform.version}

ARG TOMCAT_DIR=/usr/local/tomcat
ARG USERNAME=${alfresco.platform.docker.user} 

USER root

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile extensions/*.amp $TOMCAT_DIR/amps/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force

# Increase Tomcat resource cache to prevent cache-full warnings at startup
RUN sed -i 's/cacheMaxSize="[0-9]*"/cacheMaxSize="100000"/' $TOMCAT_DIR/conf/context.xml; \
    grep -q 'cachingAllowed' $TOMCAT_DIR/conf/context.xml || \
    sed -i 's|<Context>|<Context>\n    <Resources cachingAllowed="true" cacheMaxSize="100000"/>|' \
        $TOMCAT_DIR/conf/context.xml

COPY alfresco-global.properties $TOMCAT_DIR/shared/classes/alfresco-global.properties
COPY dev-log4j2.properties $TOMCAT_DIR/shared/classes/alfresco/extension/dev-log4j2.properties
COPY disable-webscript-caching-context.xml $TOMCAT_DIR/shared/classes/alfresco/extension

# Copy Dockerfile to avoid an error if no license file exists
COPY Dockerfile license/*.* $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/extension/license/

# Move the log file
RUN sed -i -e "s_appender.rolling.fileName\=alfresco.log_appender.rolling.fileName\=${TOMCAT_DIR}/logs\/alfresco.log_" \
        ${TOMCAT_DIR}/shared/classes/alfresco/extension/dev-log4j2.properties && \
    sed -i -e "s_appender.rolling.filePattern=alfresco.log.%d{yyyy-MM-dd}_appender.rolling.filePattern\=${TOMCAT_DIR}/logs\/alfresco.log.%d{yyyy-MM-dd}_" \
        ${TOMCAT_DIR}/shared/classes/alfresco/extension/dev-log4j2.properties

USER ${USERNAME}