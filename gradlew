#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# @author date
#

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass any JVM options to Gradle and Java applications.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

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
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/lib/gradle-launcher-@GRADLE_VERSION@.jar

# Determine the Java command to use to start the JVM.
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

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # Use the system limit
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
  GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted to Windows paths
    # It turns out that list of arguments such as -p, --project-dir, --build-file, 
    # --init-script, -I, --include, -g, --gradle-user-home, --system-prop,
    # -D, --settings-file, --configure-on-demand, --continuous, -t,
    # --offline, --recompile-scripts, --rerun-tasks, --no-search-upward, -b, -i
    # are all followed by paths to files and directories.
    # So, we convert them to Windows format before passing them to Java
    for i do
        case "$i" in
            -p|--project-dir|--build-file|--init-script|-I|--include|-g|--gradle-user-home|--system-prop|-D|--settings-file)
                # The jvmargs can be quoted, mostly with spaces, that is why we need to deal with it
                # The final parameter is the filename and it may be quoted
                eval `echo args=\"$@\"`
                # This loop is to ensure that we are not swapping the next parameter of the current option
                for j in 1 2 3 4 5 6 7 8 9
                do
                    if [ -z "$j" ] ; then
                        break
                    fi
                    # `shift` is failing in cygwin, so we are going to use `expr` to get the index of the next argument
                    next_arg_index=`expr $j + 1`
                    # Getting the next argument, it may be quoted
                    next_arg=`echo $args | cut -d ' ' -f$next_arg_index`
                    # Remove quotes from the argument
                    next_arg=`echo $next_arg|sed 's/"//g'`
                    # Check if the argument is not a parameter
                    if [ "`echo $next_arg| cut -b1`" != "-" ] ; then
                        # Check if the argument is a file
                        if [ -f "$next_arg" -o -d "$next_arg" ] ; then
                            # Adding the argument to the list of arguments to be converted
                            path_args="$path_args $next_arg"
                        fi
                    fi
                done
                ;;
        esac
    done
    for i in $path_args
    do
        # In case the argument is a directory, we don't want to convert it to a Windows path
        if [ ! -d "$i" ] ; then
            i_cyg=`cygpath --path --mixed "$i"`
            # The backslashes need to be escaped
            i_cyg=`echo $i_cyg|sed 's/\\/\\\\/g'`
            # The spaces need to be escaped
            i_cyg=`echo $i_cyg|sed 's/ /\\ /g'`
            # Replacing the argument with the converted one
            # The first sed is to escape the argument, the second is to replace the argument
            eval `echo args=\"$args\"|sed 's,'"$i"','\\\"$i_cyg\\\"',g'`
        fi
    done
    # We are setting the arguments to the new arguments
    eval "set -- $args"
fi

# Split up the JVM options only if the JAVA_OPTS variable is not defined.
if [ -z "$JAVA_OPTS" ] ; then
    JVM_OPTS=($DEFAULT_JVM_OPTS)
fi

# Add the gradle launcher jar to the classpath
if [ -n "$CLASSPATH" ] ; then
    # Add the jars from the given classpath to the command
    JAVACMD="$JAVACMD -cp \"$CLASSPATH\""
fi

# Add the JVM options to the command
if [ -n "$JVM_OPTS" ] ; then
    for JVM_OPT in "${JVM_OPTS[@]}"
    do
        JAVACMD="$JAVACMD $JVM_OPT"
    done
fi

# Add the Gradle options to the command
if [ -n "$GRADLE_OPTS" ] ; then
    for GRADLE_OPT in "${GRADLE_OPTS[@]}"
    do
        JAVACMD="$JAVACMD $GRADLE_OPT"
    done
fi

# Add the main class to the command
JAVACMD="$JAVACMD org.gradle.launcher.GradleMain"

# Add the arguments to the command
if [ -n "$@" ] ; then
    for ARG in "$@"
    do
        JAVACMD="$JAVACMD \"$ARG\""
    done
fi

# Start the JVM
eval "$JAVACMD"
