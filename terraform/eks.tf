module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = "joycrew-cluster"
  cluster_version = "1.29"

  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnets
  enable_irsa = true

  eks_managed_node_groups = {
    app_nodes = {
      instance_types = ["t3.small"]
      min_size       = 1
      max_size       = 4
      desired_size   = 2
    }
  }
}