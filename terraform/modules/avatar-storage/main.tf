resource "aws_s3_bucket" "avatar_bucket" {
  bucket = var.bucket_name
}

resource "aws_s3_bucket_public_access_block" "avatar_bucket" {
  bucket = aws_s3_bucket.avatar_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_cors_configuration" "avatar_bucket" {
  bucket = aws_s3_bucket.avatar_bucket.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE"]
    allowed_origins = var.allowed_origins
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket_policy" "avatar_bucket" {
  bucket = aws_s3_bucket.avatar_bucket.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.avatar_bucket.arn}/*"
      },
    ]
  })
}