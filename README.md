## senior-e-commerce (local enterprise e-comm playground)

Goal: teach juniors common enterprise setups (microservices, Kafka events, OAuth/JWT, GitOps, observability).

### Local cluster (k3s) + GitOps

This repo is meant to be deployed into a **local k3s cluster** using **Helm** and **ArgoCD** (GitOps).

Infra dependencies (Postgres, Kafka KRaft, Keycloak, ClickHouse, etc.) are installed into the cluster from Helm charts, tracked as ArgoCD `Application`s under `argocd-apps/`.

### Java

Services use **Java 25** (virtual threads enabled).

> Note: to run `mvn package` locally you must install JDK 25 on your machine. CI uses JDK 25.

### GitOps flow (high level)

- Push to `main`
- GitHub Actions builds/pushes images to GHCR
- ArgoCD watches manifests/Helm values and updates the cluster
