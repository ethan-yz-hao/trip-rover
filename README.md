[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

# Trip Rover

## Terraform

Set up Terraform backend
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

Set up AWS CLI profile
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

# Navigate to the dev environment directory
cd terraform/environments/dev

# Initialize Terraform
terraform init

# Plan and apply the changes
terraform plan
terraform apply
```

## License
This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
