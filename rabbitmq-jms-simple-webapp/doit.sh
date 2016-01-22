TCSERVER=~/bin/pivotal-tc-server-standard-3.1.3.SR1/server1
${TCSERVER}/bin/tcruntime-ctl.sh stop
rm -rf ${TCSERVER}/webapps/simple-*
mvn clean package
cp target/simple-jms.war ${TCSERVER}/webapps
${TCSERVER}/bin/tcruntime-ctl.sh start
