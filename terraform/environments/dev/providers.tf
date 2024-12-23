terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "trip-rover-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "us-west-2"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}

provider "aws" {
  region = var.aws_region

  # If using AWS CLI profile
  profile = var.aws_profile

  # Alternatively, use access keys directly
  # access_key = var.aws_access_key
  # secret_key = var.aws_secret_key
} 