#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
# Gradle start up script for POSIX
##############################################################################

# Attempt to set APP_HOME
app_path=$0
while [ -h "$app_path" ]; do
  ls=$( ls -ld "$app_path" )
  link=${ls#*' -> '}
  case $link in
    /*) app_path=$link ;;
    *) app_path=$APP_HOME$link ;;
  esac
done
APP_BASE_NAME=${0##*/}
APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

DEFAULT_JVM_OPTS="-Dfile.encoding=UTF-8 -Xmx64m -Xms64m"
MAX_FD=maximum
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

warn () { echo "$*" >&2; }
die () { echo "$*" >&2; exit 1; }

# OS specific support
cygwin=false
msys=false
darwin=false
case "$( uname )" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
esac

if [ -n "$JAVA_HOME" ]; then
  JAVACMD=$JAVA_HOME/bin/java
  [ ! -x "$JAVACMD" ] && die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
else
  JAVACMD=java
  which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

if [ ! -f "$CLASSPATH" ]; then
  die "ERROR: gradle-wrapper.jar not found. Run 'gradle wrapper' from project root or open project in Android Studio."
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
