variable "region" {
  description = "AWS 리전 설정"
  type        = string
  default     = "ap-northeast-2"
}

variable "cluster_name" {
  description = "EKS 클러스터 이름"
  type        = string
  default     = "joycrew-cluster"
}

variable "vpc_cidr" {
  description = "VPC CIDR 대역"
  type        = string
  default     = "10.0.0.0/16"
}

variable "node_instance_type" {
  description = "EKS 워커 노드 인스턴스 타입"
  type        = string
  default     = "t3.small"
}

variable "db_password" {
  description = "RDS 루트 계정 비밀번호"
  type        = string
  sensitive   = true # 플랜 결과창에서 비밀번호 마스킹 처리
}