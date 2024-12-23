locals {
  bucket_name = "${var.project_name}-avatars-${var.environment}"
}

module "avatar_storage" {
  source = "../../modules/avatar-storage"

  environment     = var.environment
  bucket_name     = local.bucket_name
  allowed_origins = var.allowed_origins
}

module "iam" {
  source = "../../modules/iam"

  project_name      = var.project_name
  environment       = var.environment
  avatar_bucket_arn = module.avatar_storage.bucket_arn
}

output "app_user_access_key" {
  value = module.iam.app_user_access_key
}

output "app_user_secret_key" {
  value     = module.iam.app_user_secret_key
  sensitive = true
}

output "avatar_bucket_name" {
  value = module.avatar_storage.bucket_name
}

output "avatar_bucket_domain" {
  value = module.avatar_storage.bucket_domain_name
}