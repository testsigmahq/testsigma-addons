package com.testsigma.addons.restapi;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.testsigma.addons.common.GrpcClientUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Data
@Action(
        actionText = "Call gRPC service from proto file proto-file-path at url grpc-url for service service-name, method method-name with json request json-request and store response in variable-name",
        description = "Dynamically calls a gRPC service using a .proto file and JSON request. Uses protoc's bundled standard well-known type imports, generates descriptors at runtime, and stores the JSON response in a runtime variable.",
        applicationType = ApplicationType.REST_API
)
public class GrpcClientAction extends RestApiAction {

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
            String protoDir = protoFile.getParent() != null ? protoFile.getParent() : ".";
            String protoFileName = protoFile.getName();

            tempDescriptorFile = GrpcClientUtils.generateDescriptor(protoDir, protoFileName, logger);

            Descriptors.MethodDescriptor methodDescriptor =
                    GrpcClientUtils.loadServiceDescriptors(tempDescriptorFile, serviceName.getValue().toString(), methodName.getValue().toString());

            String rawJson = GrpcClientUtils.sanitizeJson(jsonRequest.getValue().toString());

            DynamicMessage.Builder requestBuilder = DynamicMessage.newBuilder(methodDescriptor.getInputType());
            JsonFormat.parser().ignoringUnknownFields().merge(rawJson, requestBuilder);
            DynamicMessage requestMessage = requestBuilder.build();

            ManagedChannelBuilder<?> channelBuilder = GrpcClientUtils.buildChannel(grpcUrl.getValue().toString());
            channel = channelBuilder.build();
            MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethod = GrpcClientUtils.buildGrpcMethod(methodDescriptor);

            DynamicMessage responseMessage = GrpcClientUtils.performDynamicCall(channel, grpcMethod, requestMessage);

            String jsonResponse = JsonFormat.printer().print(responseMessage);
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(jsonResponse);

            setSuccessMessage("Successfully called gRPC service and stored the response in runtime variable: " + variableName.getValue() + ", value: " + jsonResponse);
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
                    logger.warn("Failed to delete temporary descriptor file: " + tempDescriptorFile + e);
                }
            }
        }
    }
}
