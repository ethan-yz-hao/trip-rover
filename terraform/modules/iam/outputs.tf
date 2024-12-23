output "app_user_access_key" {
  value = aws_iam_access_key.app_user.id
}

output "app_user_secret_key" {
  value     = aws_iam_access_key.app_user.secret
  sensitive = true
}

output "app_role_arn" {
  value = aws_iam_role.app_role.arn
} 