variable "environment" {
  type        = string
  description = "Environment name"
  default     = "dev"
}

variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "us-west-2"
}

variable "aws_profile" {
  type        = string
  description = "AWS CLI profile name"
  default     = "default"
}

# Only needed if not using AWS CLI profile
variable "aws_access_key" {
  type        = string
  description = "AWS access key"
  default     = ""
}

variable "aws_secret_key" {
  type        = string
  description = "AWS secret key"
  default     = ""
}

variable "project_name" {
  type        = string
  description = "Project name"
  default     = "trip-rover"
}

variable "allowed_origins" {
  type        = list(string)
  description = "List of allowed origins for CORS"
  default     = ["http://localhost:3000", "http://localhost:8080"]
} 