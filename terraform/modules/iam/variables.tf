variable "project_name" {
  type        = string
  description = "Project name"
}

variable "environment" {
  type        = string
  description = "Environment name"
}

variable "avatar_bucket_arn" {
  type        = string
  description = "ARN of the avatar S3 bucket"
} 