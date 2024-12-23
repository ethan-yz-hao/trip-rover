# Service role for the application
resource "aws_iam_role" "app_role" {
  name = "${var.project_name}-${var.environment}-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Avatar storage policy
resource "aws_iam_role_policy" "avatar_policy" {
  name = "${var.project_name}-${var.environment}-avatar-policy"
  role = aws_iam_role.app_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject"
        ]
        Resource = [
          "${var.avatar_bucket_arn}/*"
        ]
      }
    ]
  })
}

# IAM user for Terraform operations
resource "aws_iam_user" "terraform" {
  name = "${var.project_name}-${var.environment}-terraform"
}

resource "aws_iam_user_policy_attachment" "terraform_policy" {
  user       = aws_iam_user.terraform.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess" # You might want to restrict this
}

# Application IAM user
resource "aws_iam_user" "app_user" {
  name = "${var.project_name}-${var.environment}-app"
}

resource "aws_iam_access_key" "app_user" {
  user = aws_iam_user.app_user.name
}

resource "aws_iam_user_policy" "app_user_policy" {
  name = "${var.project_name}-${var.environment}-app-policy"
  user = aws_iam_user.app_user.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sts:AssumeRole"
        ]
        Resource = [
          aws_iam_role.app_role.arn
        ]
      }
    ]
  })
} 