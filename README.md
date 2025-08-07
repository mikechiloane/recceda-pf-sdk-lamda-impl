# Recceda PayFast Lambda

AWS Lambda function for PayFast payment processing integration.

## Overview

This Lambda function provides endpoints for PayFast payment processing:
- `/getProducts` - Retrieve available products
- `/pay/{productId}` - Initialize payment for a product
- `/notify` - Handle PayFast payment notifications
- `/cancel` - Handle payment cancellations

## Environment Variables

The following environment variables must be set:

```
PAYFAST_MERCHANT_ID=your_merchant_id
PAYFAST_MERCHANT_KEY=your_merchant_key
PAYFAST_PASSPHRASE=your_passphrase
IS_SANDBOX=true/false
```

## Building

```bash
mvn clean package
```

This will create a deployment package at `target/recceda-payfast-lambda.jar`.

## Deployment

Deploy the JAR file to AWS Lambda with:
- Runtime: Java 17
- Handler: `com.recceda.ReccedaFastLambdaHandler::handleRequest`
- Memory: 512 MB (minimum recommended)
- Timeout: 30 seconds

## API Gateway Integration

Configure API Gateway with the following routes:
- `GET /getProducts`
- `POST /pay/{productId}`
- `POST /notify`
- `POST /cancel`

## Dependencies

- AWS Lambda Java Core
- AWS Lambda Java Events
- PayFast Java SDK
- Jackson (JSON processing)
- Lombok (code generation)

## Local Testing

Use AWS SAM CLI for local testing:

```bash
sam local start-api
```
