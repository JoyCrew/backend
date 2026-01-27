# 1. Namespace 생성
resource "kubernetes_namespace_v1" "monitoring" {
  metadata {
    name = "monitoring"
  }
}

# 2. Helm Release (YAML 설정 방식)
resource "helm_release" "prometheus_stack" {
  name       = "joycrew-monitoring"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-prometheus-stack"
  namespace  = kubernetes_namespace_v1.monitoring.metadata[0].name

  # set 블록 대신 YAML 형식을 직접 전달하여 문법 에러 원천 차단
  values = [
    <<-EOF
    grafana:
      adminPassword: "admin1234"
      service:
        type: LoadBalancer
    EOF
  ]

  # EKS 클러스터가 완전히 생성된 후 실행되도록 보장
  depends_on = [module.eks]
}