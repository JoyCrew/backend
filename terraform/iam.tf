module "iam_eks_role" {
  source    = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version   = "~> 5.39.0"
  role_name = "joycrew-backend-role"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["default:joycrew-service-account"]
    }
  }

  role_policy_arns = {
    secrets_manager = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
  }
}