variable "environment" {
  type        = string
  description = "Environment name (dev, staging, prod)"
}

variable "bucket_name" {
  type        = string
  description = "Name of the S3 bucket for avatars"
}

variable "allowed_origins" {
  type        = list(string)
  description = "List of allowed origins for CORS"
}