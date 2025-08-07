package com.recceda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recceda.entity.Product;
import com.recceda.payfast.PayFastService;
import com.recceda.payfast.config.PayFastConfig;
import com.recceda.payfast.exception.ConfigurationException;
import com.recceda.payfast.exception.PayFastException;
import com.recceda.payfast.exception.ValidationException;
import com.recceda.payfast.handler.ITNHandler;
import com.recceda.payfast.model.PayFastFormData;
import com.recceda.payfast.model.PaymentRequest;

/**
 * Lambda handler for PayFast payment processing
 * Handles endpoints: /getProducts, /pay, /notify, /cancel
 */
public class ReccedaFastLambdaHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final PayFastConfig payFastConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReccedaFastLambdaHandler() throws ConfigurationException {
        this.payFastConfig = new PayFastConfig(
                System.getenv("PAYFAST_MERCHANT_ID"),
                System.getenv("PAYFAST_MERCHANT_KEY"),
                System.getenv("PAYFAST_PASSPHRASE"),
                System.getenv("IS_SANDBOX").equalsIgnoreCase("true"));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        String path = input.getPath();
        String httpMethod = input.getHttpMethod();

        logger.log("Processing " + httpMethod + " request for path: " + path);

        try {
            // Handle CORS preflight requests
            if ("OPTIONS".equals(httpMethod)) {
                return createCorsResponse();
            }

            if (path.equals("/getProducts")) {
                return handleGetProducts(input, logger);
            } else if (path.startsWith("/pay/") || path.equals("/pay")) {
                return handlePay(input, logger);
            } else if (path.equals("/notify")) {
                return handleNotify(input, logger);
            } else if (path.equals("/cancel")) {
                return handleCancel(input, logger);
            } else {
                return createErrorResponse(404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            logger.log("Error processing request: " + e.getMessage());
            return createErrorResponse(500, "Internal server error");
        }
    }

    private APIGatewayProxyResponseEvent handleGetProducts(APIGatewayProxyRequestEvent input, LambdaLogger logger) {
        try {
            String productsJson = objectMapper.writeValueAsString(ReccedaProducts.getProducts());
            return createSuccessResponse(productsJson);
        } catch (Exception e) {
            logger.log("Error serializing products: " + e.getMessage());
            return createErrorResponse(500, "Failed to retrieve products");
        }
    }

    private APIGatewayProxyResponseEvent handlePay(APIGatewayProxyRequestEvent input, LambdaLogger logger)
            throws JsonProcessingException, PayFastException {
        logger.log("Payment request received for productId: " + input.getPathParameters().get("productId"));
        String productId = input.getPathParameters().get("productId");
        if (productId == null || productId.isEmpty()) {
            return createErrorResponse(400, "Product ID is required");
        }
        Product product = ReccedaProducts.getProductById(productId);
        if (product == null) {
            return createErrorResponse(404, "Product not found with ID: " + productId);
        }

        PayFastService client = new PayFastService(payFastConfig);
        PaymentRequest payment = new PaymentRequest();
        payment.setAmount(BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP));
        payment.setItemName(product.getName());
        payment.setItemDescription(product.getDescription());
        payment.setMPaymentId("RECCEDA" + System.currentTimeMillis());

        payment.setNotifyUrl(System.getenv("PAYFAST_NOTIFY_URL"));
        payment.setReturnUrl(System.getenv("PAYFAST_RETURN_URL"));
        payment.setCancelUrl(System.getenv("PAYFAST_CANCEL_URL"));
        
        PayFastFormData formData = client.createPaymentFormData(payment);

        logger.log("Generated payment form data: " + formData.toString());
        String response = objectMapper.writeValueAsString(formData);

        return createSuccessResponse(response);

    }

    private APIGatewayProxyResponseEvent handleNotify(APIGatewayProxyRequestEvent input, LambdaLogger logger) {
        logger.log("Payment notification received");
        String body = input.getBody();
        ITNHandler itnHandler = new ITNHandler(payFastConfig);
        try {
            Map<String, String> queryParams = itnHandler.parseNotificationString(input.getBody());
            logger.log("Parsed notification parameters: " + queryParams.toString());
            itnHandler.validateITN(queryParams);

        } catch (ValidationException e) {
            logger.log("Validation error in notification: " + e.getMessage());
            return createErrorResponse(400, "Invalid notification data: " + e.getMessage());
        } catch (Exception e) {
            logger.log("Error processing notification: " + e.getMessage());
            return createErrorResponse(500, "Failed to process notification");
        }
        return createSuccessResponse("{\"message\":\"Notification processed\"}");
    }

    private APIGatewayProxyResponseEvent handleCancel(APIGatewayProxyRequestEvent input, LambdaLogger logger) {
        logger.log("Payment cancellation received");
        return createSuccessResponse("{\"message\":\"Payment cancelled\"}");
    }



    private APIGatewayProxyResponseEvent createSuccessResponse(String body) {
        return createResponse(200, body);
    }

    private APIGatewayProxyResponseEvent createCorsResponse() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers",
                "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(headers);
        response.setBody("");
        return response;
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        String errorBody = "{\"error\":\"" + message + "\"}";
        return createResponse(statusCode, errorBody);
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(headers);
        response.setBody(body);
        return response;
    }
}