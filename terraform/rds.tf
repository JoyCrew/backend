# 1. RDS 전용 보안 그룹 (Security Group)
resource "aws_security_group" "rds_sg" {
  name        = "joycrew-rds-sg"
  description = "Allow traffic from EKS nodes to RDS"
  vpc_id      = module.vpc.vpc_id # vpc.tf에서 생성된 VPC ID 참조

  # 인바운드 규칙: EKS 노드 그룹에서 오는 3306 포트 트래픽만 허용
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id] # eks.tf의 노드 SG 참조
  }

  # 아웃바운드 규칙: 모든 외부 통신 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "joycrew-rds-sg"
  }
}

# 2. RDS MySQL 인스턴스 모듈
module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  identifier = "joycrew-mysql"

  engine               = "mysql"
  engine_version       = "8.0"
  family               = "mysql8.0" # DB 파라미터 그룹 패밀리
  major_engine_version = "8.0"      # DB 옵션 그룹 메이저 버전

  instance_class    = "db.t3.micro"
  allocated_storage = 20

  db_name  = "joycrew"
  username = "admin"
  port     = "3306"
  password = var.db_password # variables.tf에 선언된 변수 사용

  # 고가용성을 위한 Multi-AZ 설정
  multi_az = true

  # 네트워크 설정
  vpc_security_group_ids = [aws_security_group.rds_sg.id] # 위에서 만든 SG 연결
  db_subnet_group_name   = module.vpc.database_subnet_group # vpc.tf에서 만든 서브넷 그룹

  # 삭제 방지 해제 (테스트용)
  skip_final_snapshot = true
  deletion_protection = false
}