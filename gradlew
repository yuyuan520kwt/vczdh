#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Set script variables for current location, script name and script options
SAVED="$0"

# Change the current directory to the location of the script
cd "`dirname "$SAVED"`"
APP_BASE_NAME=`basename "$SAVED"`
APP_HOME="`pwd -P`"

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
mingw=false

case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    mingw=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Determine Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
fi

# Determine if we need to use a wrapper script for the JVM.
if $cygwin ; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # If JAVA_HOME is set, we use it as-is.
    if [ -n "$JAVA_HOME" ] ; then
        JAVA_HOME=`cygpath --path --unix "$JAVA_HOME"`
    fi
fi

# Build classpath.
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# For Cygwin, switch paths to Windows format before running Java.
if $cygwin ; then
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    cygwin=false
fi

# Run Gradle.
"$JAVACMD" $DEFAULT_JVM_OPTS "$@" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"