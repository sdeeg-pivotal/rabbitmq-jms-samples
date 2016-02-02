TCSERVER=~/bin/pivotal-tc-server-standard-3.1.3.SR1/server1
${TCSERVER}/bin/tcruntime-ctl.sh stop
rm -rf ${TCSERVER}/webapps/*-simple*
mvn clean package
cp target/rabbitmq-jms-simple-webapp.war ${TCSERVER}/webapps
${TCSERVER}/bin/tcruntime-ctl.sh start
