/*
 * Copyright 2013 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.tslint.services;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.palantir.tslint.TSLintPlugin;

/**
 * This handles all requests for TSLint.
 *
 * @author aramaswamy
 */
public final class Bridge {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String ERROR_PREFIX = "ERROR: ";
    private static final String RESULT_PREFIX = "RESULT: ";

    private static final String OS_NAME = System.getProperty("os.name");
    private static final Splitter PATH_SPLITTER = Splitter.on(File.pathSeparatorChar);

    private Process nodeProcess;
    private BufferedReader nodeStdout;
    private PrintWriter nodeStdin;

    private final ObjectMapper mapper;

    public Bridge() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // start the node process
        this.start();
    }

    public <T> T call(Request request, Class<T> resultType) {
        checkNotNull(request);
        checkNotNull(resultType);

        JavaType type = TypeFactory.defaultInstance().constructType(resultType);

        return this.call(request, type);
    }

    public synchronized <T> T call(Request request, JavaType resultType) {
        checkNotNull(request);
        checkNotNull(resultType);

        // process the request
        String resultJson;
        try {
            String requestJson = this.mapper.writeValueAsString(request);

            resultJson = this.processRequest(requestJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // convert the JSON result into a Java object
        try {
            return this.mapper.readValue(resultJson, resultType);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing result: " + resultJson, e);
        }
    }

    public void dispose() {
        this.nodeStdin.close();

        try {
            this.nodeStdout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.nodeProcess.destroy();
        this.nodeProcess = null;
    }

    private String processRequest(String requestJson) throws IOException {
        checkNotNull(requestJson);

        // write the request JSON to the bridge's stdin
        this.nodeStdin.println(requestJson);

        // read the response JSON from the bridge's stdout
        String resultJson = null;
        do {
            String line = this.nodeStdout.readLine();

            // process errors and logger statements
            if (line == null) {
                throw new IllegalStateException("The node process has crashed.");
            } else if (line.startsWith(ERROR_PREFIX)) {
                // remove prefix
                line = line.substring(ERROR_PREFIX.length(), line.length());
                // put newlines back
                line = line.replaceAll("\\\\n", LINE_SEPARATOR); // put newlines back
                // replace soft tabs with hardtabs to match Java's error stack trace.
                line = line.replaceAll("    ", "\t");

                throw new RuntimeException("The following request caused an error to be thrown:" + LINE_SEPARATOR
                        + requestJson + LINE_SEPARATOR
                        + line);
            } else if (line.startsWith(RESULT_PREFIX)) {
                resultJson = line.substring(RESULT_PREFIX.length());
            } else { // log statement
                System.out.println(line);
            }
        } while (resultJson == null);

        return resultJson;
    }

    private void start() {
        File nodeFile = Bridge.findNode();
        String nodePath = nodeFile.getAbsolutePath();

        // get the path to the bridge.js file
        File bundleFile;
        try {
            bundleFile = FileLocator.getBundleFile(TSLintPlugin.getDefault().getBundle());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File bridgeFile = new File(bundleFile, "bin/bridge.js");
        String bridgePath = bridgeFile.getAbsolutePath();

        // construct the arguments
        ImmutableList.Builder<String> argsBuilder = ImmutableList.builder();
        argsBuilder.add(nodePath);
        argsBuilder.add(bridgePath);

        // start the node process and create a reader/writer for its stdin/stdout
        List<String> args = argsBuilder.build();
        ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[args.size()]));
        try {
            this.nodeProcess = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.nodeStdout = new BufferedReader(new InputStreamReader(this.nodeProcess.getInputStream(), Charsets.UTF_8));
        this.nodeStdin = new PrintWriter(new OutputStreamWriter(this.nodeProcess.getOutputStream(), Charsets.UTF_8), true);

        // add a shutdown hook to destroy the node process in case its not properly disposed
        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
    }

    private static File findNode() {
        String nodeFileName = getNodeFileName();
        String path = System.getenv("PATH");
        List<String> directories = Lists.newArrayList(PATH_SPLITTER.split(path));

        // ensure /usr/local/bin is included for OS X
        if (OS_NAME.startsWith("Mac OS X")) {
            directories.add("/usr/local/bin");
        }

        // search for Node.js in the PATH directories
        for (String directory : directories) {
            File nodeFile = new File(directory, nodeFileName);

            if (nodeFile.exists()) {
                return nodeFile;
            }
        }

        throw new IllegalStateException("Could not find Node.js.");
    }

    private static String getNodeFileName() {
        if (OS_NAME.startsWith("Windows")) {
            return "node.exe";
        }

        return "node";
    }

    private class ShutdownHookThread extends Thread {
        @Override
        public void run() {
            Process process = Bridge.this.nodeProcess;

            if (process != null) {
                process.destroy();
            }
        }
    }
}
