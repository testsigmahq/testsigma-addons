package com.testsigma.addons.common;

import com.github.os72.protocjar.Protoc;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.testsigma.sdk.Logger;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shared helper methods for the per-application-type GrpcClientAction classes
 * (web, ios, android, restapi), which otherwise duplicated this logic verbatim.
 */
public final class GrpcClientUtils {

    private GrpcClientUtils() {
    }

    public static void downloadProtoImports(Path destination, Logger logger) throws Exception {
        String[] requiredImports = {
                "google/protobuf/wrappers.proto",
                "google/protobuf/empty.proto"
        };

        String baseUrl = "https://raw.githubusercontent.com/protocolbuffers/protobuf/main/src/";

        for (String importPath : requiredImports) {
            URL url = new URL(baseUrl + importPath);
            Path destPath = destination.resolve(importPath);
            Files.createDirectories(destPath.getParent());

            logger.info("Downloading proto import: " + url + " to " + destPath);
            try (InputStream in = url.openStream()) {
                Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Generates a descriptor set using explicitly downloaded proto imports (used by the web action).
     */
    public static Path generateDescriptor(String protoDir, String protoFile, Path importsDir, Logger logger) throws Exception {
        Path tempFile = Files.createTempFile("descriptor", ".pb");

        String[] args = new String[]{
                "--proto_path=" + protoDir,
                "--proto_path=" + importsDir.toAbsolutePath().toString(),
                "--include_imports",
                "--descriptor_set_out=" + tempFile.toAbsolutePath().toString(),
                protoFile
        };

        logger.info("Executing embedded protoc with args: " + String.join(" ", args));

        int exitCode = Protoc.runProtoc(args);
        if (exitCode != 0) {
            throw new RuntimeException("protoc compilation failed with exit code " + exitCode + ". Check if all imports are correct and available.");
        }
        return tempFile;
    }

    /**
     * Generates a descriptor set using protoc-jar's bundled well-known types
     * (used by the ios, android and restapi actions).
     */
    public static Path generateDescriptor(String protoDir, String protoFile, Logger logger) throws Exception {
        Path tempFile = Files.createTempFile("descriptor", ".pb");

        String[] args = new String[]{
                "--proto_path=" + protoDir,
                "--include_imports",
                "--include_std_types",
                "--descriptor_set_out=" + tempFile.toAbsolutePath().toString(),
                protoFile
        };

        logger.info("Executing embedded protoc with args: " + String.join(" ", args));

        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

        int exitCode = Protoc.runProtoc(args, stdOut, stdErr);
        if (exitCode != 0) {
            String protocOutput = (stdErr.toString(StandardCharsets.UTF_8) + stdOut.toString(StandardCharsets.UTF_8)).trim();
            String detail = protocOutput.isEmpty()
                    ? "protoc produced no diagnostic output."
                    : "protoc output: " + protocOutput;
            throw new RuntimeException("protoc compilation failed with exit code " + exitCode + ". " + detail);
        }
        return tempFile;
    }

    public static Descriptors.MethodDescriptor loadServiceDescriptors(Path descriptorPath, String serviceName, String methodName) throws Exception {
        try (FileInputStream descriptorStream = new FileInputStream(descriptorPath.toFile())) {
            DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(descriptorStream);
            if (descriptorSet.getFileCount() == 0) {
                throw new RuntimeException("Descriptor set is empty. Check your proto file.");
            }

            Map<String, Descriptors.FileDescriptor> descriptorMap = new HashMap<>();
            List<DescriptorProtos.FileDescriptorProto> protosToProcess = new ArrayList<>(descriptorSet.getFileList());

            while (!protosToProcess.isEmpty()) {
                int processedCount = 0;
                Iterator<DescriptorProtos.FileDescriptorProto> iterator = protosToProcess.iterator();
                while (iterator.hasNext()) {
                    DescriptorProtos.FileDescriptorProto fileProto = iterator.next();
                    boolean allDependenciesMet = true;
                    List<Descriptors.FileDescriptor> dependencies = new ArrayList<>();
                    for (String depName : fileProto.getDependencyList()) {
                        Descriptors.FileDescriptor dependencyDescriptor = descriptorMap.get(depName);
                        if (dependencyDescriptor == null) {
                            allDependenciesMet = false;
                            break;
                        }
                        dependencies.add(dependencyDescriptor);
                    }

                    if (allDependenciesMet) {
                        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileProto, dependencies.toArray(new Descriptors.FileDescriptor[0]));
                        descriptorMap.put(fileDescriptor.getName(), fileDescriptor);
                        iterator.remove();
                        processedCount++;
                    }
                }

                if (processedCount == 0 && !protosToProcess.isEmpty()) {
                    throw new RuntimeException(
                            "Unresolvable dependencies found in proto files. This could be due to a circular import or a missing file. Remaining files: " +
                                    protosToProcess.stream().map(DescriptorProtos.FileDescriptorProto::getName).collect(Collectors.joining(", "))
                    );
                }
            }

            // The last file in the original list from protoc is the main user file.
            String mainFileName = descriptorSet.getFile(descriptorSet.getFileCount() - 1).getName();
            Descriptors.FileDescriptor mainFileDescriptor = descriptorMap.get(mainFileName);
            if (mainFileDescriptor == null) {
                throw new IllegalStateException("Could not find main file descriptor '" + mainFileName + "' in the processed map.");
            }

            Descriptors.ServiceDescriptor serviceDescriptor = mainFileDescriptor.findServiceByName(serviceName);
            if (serviceDescriptor == null) {
                String availableServices = mainFileDescriptor.getServices().stream().map(Descriptors.ServiceDescriptor::getName).collect(Collectors.joining(", "));
                throw new RuntimeException("Service not found: '" + serviceName + "'. Available services in proto are: [" + availableServices + "]");
            }

            Descriptors.MethodDescriptor methodDescriptor = serviceDescriptor.findMethodByName(methodName);
            if (methodDescriptor == null) {
                String availableMethods = serviceDescriptor.getMethods().stream().map(Descriptors.MethodDescriptor::getName).collect(Collectors.joining(", "));
                throw new RuntimeException("Method not found: '" + methodName + "'. Available methods in service '" + serviceName + "' are: [" + availableMethods + "]");
            }
            return methodDescriptor;
        }
    }

    public static MethodDescriptor<DynamicMessage, DynamicMessage> buildGrpcMethod(Descriptors.MethodDescriptor methodDescriptor) {
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(
                        methodDescriptor.getService().getFullName(), methodDescriptor.getName()))
                .setRequestMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(methodDescriptor.getInputType())))
                .setResponseMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(methodDescriptor.getOutputType())))
                .build();
    }

    public static DynamicMessage performDynamicCall(ManagedChannel channel,
                                                      MethodDescriptor<DynamicMessage, DynamicMessage> method,
                                                      DynamicMessage request) {
        return ClientCalls.blockingUnaryCall(channel, method, CallOptions.DEFAULT, request);
    }

    public static String sanitizeJson(String input) {
        String cleaned = input
                .replace("﻿", "")
                .replaceAll("[\\u00A0\\u2007\\u202F]", " ")
                .trim();

        try (JsonReader reader = new JsonReader(new StringReader(cleaned))) {
            reader.setLenient(true);
            JsonElement parsed = JsonParser.parseReader(reader);
            return parsed.toString();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON request (after normalization): " + cleaned, e);
        }
    }

    public static void deleteDirectoryRecursively(Path directoryPath) throws Exception {
        if (Files.exists(directoryPath)) {
            try (Stream<Path> walk = Files.walk(directoryPath)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    /**
     * grpc-java's ManagedChannelBuilder#forTarget expects a bare "host:port" or an
     * explicitly-registered authority scheme (e.g. "dns:///host:port") - it has no
     * NameResolverProvider for "grpc://" or "grpcs://", so passing those straight
     * through throws IllegalArgumentException: Could not find a NameResolverProvider.
     * Strip the scheme here and use it only to decide plaintext vs TLS.
     */
    public static ManagedChannelBuilder<?> buildChannel(String rawUrl) {
        String target = rawUrl.trim();
        boolean useTls;

        if (target.regionMatches(true, 0, "grpcs://", 0, 8)) {
            useTls = true;
            target = target.substring(8);
        } else if (target.regionMatches(true, 0, "grpc://", 0, 7)) {
            useTls = false;
            target = target.substring(7);
        } else {
            // No scheme provided - default to TLS, since that's the safer assumption
            // for endpoints reached over the public internet/port 443.
            useTls = true;
        }

        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(target);
        return useTls ? builder.useTransportSecurity() : builder.usePlaintext();
    }
}
