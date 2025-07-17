#!/usr/bin/env bash

QUEUE_URL="https://sqs.us-east-1.amazonaws.com/939880360164/active-repo-queue"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <repository-name>"
    echo "Example: $0 some-owner/some-repo"
    exit 1
fi

REPO_NAME="$1"

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")
MESSAGE_BODY=$(cat <<EOF
{
    "timestamp": "$TIMESTAMP",
    "repositories": [
        {
            "name": "$REPO_NAME"
        }
    ]
}
EOF
)

echo "Submitting message to SQS queue..."
echo "Repository: $REPO_NAME"
echo "Timestamp: $TIMESTAMP"

aws sqs send-message \
    --queue-url "$QUEUE_URL" \
    --message-body "$MESSAGE_BODY"

if [ $? -eq 0 ]; then
    echo "Message submitted successfully!"
else
    echo "Error: Failed to submit message to SQS queue"
    exit 1
fi
