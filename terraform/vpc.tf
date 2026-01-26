module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "joycrew-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["ap-northeast-2a", "ap-northeast-2c"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true # Private Pod들의 외부 통신을 위함
  single_nat_gateway = true # 비용 절감을 위해 하나만 생성

  tags = {
    "kubernetes.io/cluster/joycrew-cluster" = "shared"
  }
}