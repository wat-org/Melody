#!/bin/sh
#
# JBoss DataGrid 'add-user' wrapper

## Load JBoss DataGrid Service configuration.
JBOSS_CONF="$(dirname "$(readlink -f "$0")")/../configuration/jboss-jdgd.conf"
[ -r "${JBOSS_CONF}" ] || {
  echo "Cannot read configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

. "${JBOSS_CONF}" || {
  echo "Failed to load configuration file '${JBOSS_CONF}'." >&2
  exit 1
}

if [ -z "${JBOSS_BASE_DIR}" ]; then
  echo "Variable \$JBOSS_BASE_DIR is not defined or empty. It should contain the JBoss EAP Standalone instance's base dir." >&2
  echo "This variable must be defined defined in the file ${JBOSS_CONF}." >&2
  exit 1
fi

## Set defaults.
# no need for default value for ${JBOSS_MODULEPATH}
[ -z "${JBOSS_HOME}" ]                && JBOSS_HOME="/opt/jboss-datagrid-server-6"
[ -z "${JBOSS_ADD_USER}" ]            && JBOSS_ADD_USER="${JBOSS_HOME}/bin/add-user.sh"

## Compute some variables
ADD_USER_CMD="LANG=\"${LANG}\" \
              JAVA_HOME=\"${JAVA_HOME}\" \
              JAVA_OPTS=\"${JAVA_OPTS} -Djava.io.tmpdir=${JBOSS_BASE_DIR}/tmp/ -Djboss.server.config.user.dir=${JBOSS_BASE_DIR}/configuration -Djboss.domain.config.user.dir=/noway\" \
              JBOSS_MODULEPATH=\"${JBOSS_MODULEPATH}\" \
              \"${JBOSS_ADD_USER}\""

###
### validate some stuff
[ -e "${JBOSS_ADD_USER}" ] || {
  echo "File '${JBOSS_ADD_USER}' doesn't exists."
  echo "The variable \$JBOSS_ADD_USER must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine Add-User script." >&2
  exit 1
}

[ -x "${JBOSS_ADD_USER}" ] || {
  echo "File '${JBOSS_ADD_USER}' is not executable."
  echo "The variable \$JBOSS_ADD_USER must be defined defined in the file '${JBOSS_CONF}' and must point to the JBoss DataGrid Engine Add-User script." >&2
  exit 1
}

###
### main
eval "${ADD_USER_CMD}" '"$@"'
exit $?
