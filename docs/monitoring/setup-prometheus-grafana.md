# Prometheus & Grafana (Google Managed)

ChronoFlow 的微服务已经暴露 Micrometer 指标到 `/actuator/prometheus`。当前推荐的监控方案是使用 **Google Managed Service for Prometheus** 搭配 **Managed Grafana**，无需自建 Prometheus/Grafana 集群。

> 如仍想采用自建 `kube-prometheus-stack`，可参考旧版提交 `cicd` 分支历史记录。本指南仅覆盖托管方案。

---

## 1. 切换到团队 GCP 账号与项目

```bash
gcloud auth login                             # 登录团队账号
gcloud config set project <TEAM_PROJECT_ID>   # 指向目标项目
gcloud container clusters get-credentials <CLUSTER_NAME> \
  --region <REGION>                            # 或 --zone <ZONE>
kubectl config current-context                # 确认连接的是目标集群
kubectl get nodes                              # 快速检查连接情况
```

## 2. 启用 Managed Service for Prometheus

只需对现有 GKE 集群执行一次：

```bash
gcloud container clusters update <CLUSTER_NAME> \
  --region <REGION> \
  --enable-managed-prometheus
```

启用后 `gmp-system` 命名空间会自动部署 Collector、Rules Operator 等组件，可用下列命令确认状态：

```bash
kubectl get pods -n gmp-system
```

## 3. 部署 PodMonitoring 采集配置

仓库中的 `k8s/<service>/podmonitoring.yaml` 已为每个服务定义好 `PodMonitoring`，GitHub Actions 在部署阶段会自动 `kubectl apply`。如需手动应用或调试，也可执行：

```bash
kubectl apply -f k8s/user-service/podmonitoring.yaml
# 其它服务同理
```

如需新服务被采集，复制范例文件并更换 `app` 标签、端口，提交后即可。

## 4. 验证指标采集

```bash
kubectl get podmonitoring -n microservices
kubectl logs -n gmp-system -l app=gmp-collector
```

也可在 Cloud Console → Monitoring → Metrics 浏览器中搜索 `prometheus.googleapis.com/http_server_requests_seconds_count` 等指标确认数据是否流入。

## 5. 启用 Managed Grafana（可选）

```bash
gcloud grafana instances create chronoflow-grafana \
  --region <REGION>
```

在控制台打开 Grafana 实例，使用团队账号授权。首次进入后：

1. Connections → Data sources → Add data source → 选择 **Prometheus**。
2. URL 填写 `https://monitoring.googleapis.com/`，身份验证选择 **Google Cloud Monitoring**。
3. 保存后可导入常用 Dashboard（例如 4701 Spring Boot Metrics、14365 JVM Micrometer）。

## 6. 告警与规则

Managed Prometheus 支持 `Rule` 资源（`rules.monitoring.googleapis.com`）。如需 K8s YAML 管理告警：

```yaml
apiVersion: monitoring.googleapis.com/v1alpha1
kind: Rules
metadata:
  name: spring-boot-alerts
  namespace: microservices
spec:
  groups:
    - name: http-alerts
      rules:
        - alert: High5xxRatio
          expr: |
            rate(http_server_requests_seconds_count{status=~"5.."}[5m]) >
            rate(http_server_requests_seconds_count[5m]) * 0.05
          for: 2m
          labels:
            severity: warning
          annotations:
            description: "High HTTP 5xx ratio detected."
```

同时，Cloud Monitoring Alerting 也可以直接基于托管指标创建通知策略。

## 7. 后续建议

- 若需要长期保存指标，可在 Cloud Monitoring 中启用自定义保留策略。
- 请避免在外部 Service（如 `gateway` 的 LoadBalancer）暴露 `/actuator/prometheus`，PodMonitoring 会直接连接 Pod 内部端口。
- 如需暂时禁用某服务的采集，可 `kubectl delete podmonitoring <name> -n microservices`。

至此，Prometheus 指标将由 Google 托管采集和存储，Grafana 用于仪表板展示与分析。无需手动维护 Prometheus/Grafana 组件。***
