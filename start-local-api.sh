#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded from .env file"
else
    echo "❌ .env file not found"
    exit 1
fi

# Start SAM local API with environment variables
echo "🚀 Starting SAM local API server..."
sam local start-api \
    --region "${AWS_REGION:-us-east-1}" \
    --parameter-overrides \
        "PayFastMerchantId=${PAYFAST_MERCHANT_ID}" \
        "PayFastMerchantKey=${PAYFAST_MERCHANT_KEY}" \
        "PayFastPassphrase=${PAYFAST_PASSPHRASE}" \
        "IsSandbox=${IS_SANDBOX}" \
        "PayFastNotifyUrl=${PAYFAST_NOTIFY_URL}" \
        "PayFastReturnUrl=${PAYFAST_RETURN_URL}" \
        "PayFastCancelUrl=${PAYFAST_CANCEL_URL}" \