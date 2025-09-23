package com.testsigma.addons.restapi;

import com.github.os72.protocjar.Protoc;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Data
@Action(
        actionText = "Call gRPC service from proto file proto-file-path at url grpc-url for service service-name, method method-name with json request json-request and store response in variable-name",
        description = "Dynamically calls a gRPC service using a .proto file and JSON request. Automatically generates descriptors at runtime and stores the JSON response in a runtime variable.",
        applicationType = ApplicationType.REST_API
)
public class GrpcClientAction extends WebAction {

    @TestData(reference = "proto-file-path")
    private com.testsigma.sdk.TestData protoFilePath;

    @TestData(reference = "grpc-url")
    private com.testsigma.sdk.TestData grpcUrl;

    @TestData(reference = "service-name")
    private com.testsigma.sdk.TestData serviceName;

    @TestData(reference = "method-name")
    private com.testsigma.sdk.TestData methodName;

    @TestData(reference = "json-request")
    private com.testsigma.sdk.TestData jsonRequest;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        Path tempDescriptorFile = null;
        ManagedChannel channel = null;

        try {
            File protoFile = new File(protoFilePath.getValue().toString());
            String protoDir = protoFile.getParent();
            String protoFileName = protoFile.getName();

            tempDescriptorFile = generateDescriptor(protoDir, protoFileName);

            Descriptors.MethodDescriptor methodDescriptor =
                    loadServiceDescriptors(tempDescriptorFile, serviceName.getValue().toString(), methodName.getValue().toString());

            String rawJson = sanitizeJson(jsonRequest.getValue().toString());

            DynamicMessage.Builder requestBuilder = DynamicMessage.newBuilder(methodDescriptor.getInputType());
            JsonFormat.parser().ignoringUnknownFields().merge(rawJson, requestBuilder);
            DynamicMessage requestMessage = requestBuilder.build();

            channel = ManagedChannelBuilder.forTarget(grpcUrl.getValue().toString())
                    .usePlaintext()
                    .build();
            MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethod = buildGrpcMethod(methodDescriptor);

            DynamicMessage responseMessage = performDynamicCall(channel, grpcMethod, requestMessage);

            String jsonResponse = JsonFormat.printer().print(responseMessage);
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(jsonResponse);

            setSuccessMessage("Successfully called gRPC service and stored the response in runtime variable: " + variableName.getValue());
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Error during gRPC call: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("gRPC call failed: " + e.getMessage());
            return Result.FAILED;
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Error shutting down gRPC channel " + ExceptionUtils.getStackTrace(e));
                }
            }
            if (tempDescriptorFile != null) {
                try {
                    Files.delete(tempDescriptorFile);
                } catch (Exception e) {
                    logger.warn("Failed to delete temporary descriptor file: " + tempDescriptorFile + " " + ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    private Path generateDescriptor(String protoDir, String protoFile) throws Exception {
        Path tempFile = Files.createTempFile("descriptor", ".pb");

        String[] args = new String[]{
                "--proto_path=" + protoDir,
                "--include_imports",
                "--descriptor_set_out=" + tempFile.toAbsolutePath().toString(),
                protoFile
        };

        logger.info("Executing embedded protoc with args: " + String.join(" ", args));

        int exitCode = Protoc.runProtoc(args);
        if (exitCode != 0) {
            throw new RuntimeException("protoc compilation failed with exit code " + exitCode);
        }
        return tempFile;
    }

    private Descriptors.MethodDescriptor loadServiceDescriptors(Path descriptorPath, String serviceName, String methodName) throws Exception {
        try (FileInputStream descriptorStream = new FileInputStream(descriptorPath.toFile())) {
            DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(descriptorStream);
            if (descriptorSet.getFileCount() == 0) {
                throw new RuntimeException("Descriptor set is empty. Check your proto file.");
            }
            DescriptorProtos.FileDescriptorProto fileDescriptorProto = descriptorSet.getFile(0);

            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[]{});

            Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
            if (serviceDescriptor == null) {
                throw new RuntimeException("Service not found: " + serviceName);
            }

            Descriptors.MethodDescriptor methodDescriptor = serviceDescriptor.findMethodByName(methodName);
            if (methodDescriptor == null) {
                throw new RuntimeException("Method not found: " + methodName);
            }
            return methodDescriptor;
        }
    }

    private MethodDescriptor<DynamicMessage, DynamicMessage> buildGrpcMethod(Descriptors.MethodDescriptor methodDescriptor) {
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

    private DynamicMessage performDynamicCall(ManagedChannel channel,
                                              MethodDescriptor<DynamicMessage, DynamicMessage> method,
                                              DynamicMessage request) {
        return ClientCalls.blockingUnaryCall(channel, method, CallOptions.DEFAULT, request);
    }

    private String sanitizeJson(String input) {
        String cleaned = input
                .replace("\uFEFF", "")
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
}
