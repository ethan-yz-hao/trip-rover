# Trip Rover Deployment

Terraform configurations for deploying Trip Rover's AWS infrastructure.

## Infrastructure Overview

### Resources
- **S3 Bucket for Avatars**
  - Public read access for avatar retrieval
  - CORS configuration for web access
  - Default avatar image pre-loaded programmatically
  - Regional domain name for direct access

- **IAM Resources**
  - Application Role (`trip-rover-{env}-app-role`)
    - Assumes EC2 service role
    - Permissions to manage avatar storage
  - Terraform User (`trip-rover-{env}-terraform`)
    - Administrative access for infrastructure management
  - Application User (`trip-rover-{env}-app`)
    - Limited permissions to assume app role
    - Access keys for API authentication

### Features
- Environment-based deployments (dev, staging, prod)
- Remote state management using S3 and DynamoDB
- Secure access management with dedicated IAM users for Terraform and application

## Prerequisites
- AWS CLI
- Terraform
- AWS IAM user with programmatic access for creating resources

## Set up AWS CLI profile
```
# Install AWS CLI
aws configure --profile trip-rover-dev

# Enter your AWS credentials when prompted
AWS Access Key ID: YOUR_ACCESS_KEY
AWS Secret Access Key: YOUR_SECRET_KEY

# Set the region
Default region name: us-west-2

# Set the output format
Default output format: json

export AWS_PROFILE=trip-rover-dev  # $env:AWS_PROFILE = "trip-rover-dev" for Windows PowerShell
```

## Set up Terraform backend
```
# Create S3 bucket for state
aws s3api create-bucket \
    --bucket trip-rover-terraform-state \
    --region us-west-2 \
    --create-bucket-configuration LocationConstraint=us-west-2

# Enable versioning
aws s3api put-bucket-versioning \
    --bucket trip-rover-terraform-state \
    --versioning-configuration Status=Enabled

# Create DynamoDB table for state locking
aws dynamodb create-table \
    --table-name terraform-state-lock \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

## Deploy development environment

```
# Navigate to the dev environment directory
cd terraform/environments/dev

# Initialize Terraform
terraform init

# Plan and apply the changes
terraform plan
terraform apply
```