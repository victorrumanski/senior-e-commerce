# Senior E-Commerce Platform

A **microservices e-commerce platform** running on **Kubernetes (k3s)** with **GitOps (ArgoCD)**, designed to teach junior developers enterprise Java patterns and Kubernetes deployment.

## 📚 Learning Goals

This project covers:
- **Microservices architecture** — 8 independent services communicating via Kafka
- **Event-driven design** — Orders → Payments → Shipping → Delivery workflow
- **Kubernetes & Helm** — Deploy services declaratively with GitOps
- **GitOps (ArgoCD)** — Declarative cluster management from Git
- **Observability** — Prometheus metrics, Loki logs, Tempo traces
- **OAuth 2.0/JWT** — Keycloak authentication & security
- **Database design** — PostgreSQL with service-specific schemas
- **CI/CD** — GitHub Actions → Docker images → Kubernetes deployment

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    LOAD BALANCER (Traefik)                      │
└─────────────────────────────────────────────────────────────────┘
                               ↓
┌──────────────┬──────────────┬──────────────┬──────────────────┐
│   Auth       │   Catalog    │  Checkout    │   Payments       │
│  Service     │   Service    │  Service     │   Service        │
└──────────────┴──────────────┴──────────────┴──────────────────┘
                               ↓
                    ┌──────────────────────┐
                    │  Kafka (KRaft mode)  │
                    │   Message Broker     │
                    └──────────────────────┘
                               ↓
┌──────────────┬──────────────┬──────────────┬──────────────────┐
│   Shipping   │   Delivery   │ Inventory    │ Notifications    │
│  Service     │   Service    │  Service     │   Service        │
└──────────────┴──────────────┴──────────────┴──────────────────┘
                               ↓
┌──────────────┬──────────────┬──────────────┬──────────────────┐
│  PostgreSQL  │  Keycloak    │  ClickHouse  │ Observability    │
│  (Database)  │  (Auth)      │  (Analytics) │ (Prom/Loki/Tempo)│
└──────────────┴──────────────┴──────────────┴──────────────────┘
```

**Flow:**
1. Client places order → **Checkout Service** sends `ORDER_PLACED` event to Kafka
2. **Payments Service** consumes event, processes payment, emits `PAYMENT_SUCCEEDED`
3. **Shipping Service** consumes payment event, prepares shipment, emits `SHIPMENT_PREPARED`
4. **Delivery Service** consumes shipping event, delivers order, emits `DELIVERED`
5. **Notifications Service** sends customer notifications
6. **Inventory Service** tracks reserved stock

---

## 📋 Prerequisites

Before starting, install these on your machine:

| Tool | Version | Purpose |
|------|---------|---------|
| **Docker** | Latest | Container runtime |
| **k3s** | Latest | Lightweight Kubernetes |
| **kubectl** | Latest | Kubernetes CLI |
| **Helm** | 3.10+ | Kubernetes package manager |
| **Java/JDK** | 25 | Build microservices locally |
| **Maven** | 3.9+ | Java dependency management |
| **Git** | Latest | Version control |
| **ArgoCD CLI** | Optional | GitOps CLI (nice to have) |

### Installation (macOS)

```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Docker Desktop
# Download from https://www.docker.com/products/docker-desktop

# Install k3s (simple one-liner)
curl -sfL https://get.k3s.io | sh -

# Verify k3s is running
kubectl get nodes

# Install Helm
brew install helm

# Install Java 25
brew install openjdk@25
export PATH="/opt/homebrew/opt/openjdk@25/bin:$PATH"
java -version

# Install Maven
brew install maven
mvn -version
```

### Verification

```bash
# Should all return version numbers:
docker --version
kubectl version --client
helm version
java -version
mvn -version
```

---

## 🚀 Quick Start (5 minutes)

### Step 1: Clone & Navigate

```bash
git clone git@github.com:victorrumanski/senior-e-commerce.git
cd senior-e-commerce
```

### Step 2: Start k3s Cluster

```bash
# If you installed k3s with the curl script, it should already be running
# Verify:
kubectl get nodes
kubectl get pods -A

# If not running, start it:
k3s server &
```

### Step 3: Install ArgoCD

```bash
# Create Kubernetes namespace for ArgoCD
kubectl create namespace argocd

# Install ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

# Get initial ArgoCD password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
```

### Step 4: Port Forward to ArgoCD UI

```bash
# In a new terminal, port forward
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Open browser to https://localhost:8080
# Login with username: admin and password from Step 3
```

### Step 5: Deploy the Infrastructure (App-of-Apps)

```bash
# Apply the root "app-of-apps" which will install everything
kubectl apply -f argocd-apps/infra-app-of-apps.yaml
kubectl apply -f argocd-apps/apps-app-of-apps.yaml

# Watch ArgoCD sync the applications
kubectl get applications -n argocd -w
```

This will automatically deploy:
- ✅ PostgreSQL (with schemas for each service)
- ✅ Kafka (KRaft mode, no Zookeeper)
- ✅ Keycloak (authentication)
- ✅ ClickHouse (analytics)
- ✅ Prometheus + Grafana (metrics)
- ✅ Loki + Promtail (logs)
- ✅ Tempo (traces)
- ✅ All 8 microservices

---

## 🏗️ Build & Deploy Services Locally

### Option A: Let GitHub Actions Build (Recommended)

1. **Push code to GitHub**
   ```bash
   git push origin main
   ```

2. **GitHub Actions automatically:**
   - Runs `mvn package` to build JAR
   - Builds Docker image
   - Pushes to GitHub Container Registry (ghcr.io)
   - ArgoCD detects new image and deploys

3. **Check deployment status**
   ```bash
   kubectl get deployments -A
   kubectl logs -f deployment/auth-service -n auth-service
   ```

### Option B: Build & Deploy Locally (For Development)

If you're developing and want to test locally:

```bash
# 1. Build a single service
cd services/auth-service
mvn clean package

# 2. Build Docker image
docker build -t auth-service:local .

# 3. Load image into k3s
k3s ctr images import <(docker save auth-service:local)

# 4. Update Kubernetes deployment to use local image
kubectl set image deployment/auth-service \
  auth-service=auth-service:local \
  -n auth-service --record

# 5. Check logs
kubectl logs -f deployment/auth-service -n auth-service
```

---

## 🌐 Accessing Services

### Option 1: Local Ingress (Recommended)

k3s comes with **Traefik** installed. You can create Ingress rules to route traffic via domain names.

**Setup (one-time):**

```bash
# Create Ingress for local services
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: local-ingress
  namespace: ingress-traefik
spec:
  rules:
  - host: localtest.me
    http:
      paths:
      - path: /checkout
        pathType: Prefix
        backend:
          service:
            name: checkout-service
            port:
              number: 8080
      - path: /catalog
        pathType: Prefix
        backend:
          service:
            name: catalog-service
            port:
              number: 8080
      - path: /auth
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 8080
      - path: /api/health
        pathType: Prefix
        backend:
          service:
            name: checkout-service
            port:
              number: 8080
  - host: keycloak.localtest.me
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 80
  - host: prometheus.localtest.me
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: prometheus-operated
            port:
              number: 9090
  - host: grafana.localtest.me
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: kube-prometheus-stack-grafana
            port:
              number: 80
EOF
```

**Add to `/etc/hosts`** (or skip if localtest.me works for you):

```bash
echo "127.0.0.1 localtest.me keycloak.localtest.me prometheus.localtest.me grafana.localtest.me" | sudo tee -a /etc/hosts
```

**Access services directly:**

Then open in browser:
- Checkout: http://localtest.me/checkout/orders
- Catalog: http://localtest.me/catalog/
- Auth: http://localtest.me/auth/
- Keycloak: http://keycloak.localtest.me/
- Prometheus: http://prometheus.localtest.me/
- Grafana: http://grafana.localtest.me/ (login: admin/prom-operator)

### Option 2: Port Forwarding (If Ingress doesn't work)

If Ingress fails, fall back to port-forwarding:

```bash
# List all running services
kubectl get pods -A

# Checkout Service (REST API)
kubectl port-forward svc/checkout-service 8081:8080 -n checkout-service

# Access at http://localhost:8081/orders

# Catalog Service
kubectl port-forward svc/catalog-service 8082:8080 -n catalog-service

# Auth Service
kubectl port-forward svc/auth-service 8083:8080 -n auth-service

# Keycloak (for auth)
kubectl port-forward svc/keycloak 8888:80 -n identity

# Prometheus (metrics)
kubectl port-forward svc/prometheus-operated 9090:9090 -n observability

# Grafana (dashboards)
kubectl port-forward svc/kube-prometheus-stack-grafana 3000:80 -n observability
```

---

## 📊 Understanding the Event Flow

### Place an Order

```bash
curl -X POST http://localtest.me/checkout/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "items": [{"productId": "prod-1", "quantity": 2}],
    "shippingAddress": "123 Main St",
    "paymentMethodToken": "pm_123"
  }'
```

Or if using port-forward:
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### What Happens

1. **Checkout** receives request → creates `OrderPlacedEvent` → sends to Kafka topic `order-events`
2. **Payments** listens to `order-events` → processes payment → sends `PaymentSucceededEvent` to `payment-events`
3. **Shipping** listens to `payment-events` → prepares shipment → sends `ShipmentPreparedEvent` to `shipping-events`
4. **Delivery** listens to `shipping-events` → delivers → sends `DeliveredEvent` to `delivery-events`
5. **Notifications** listens to `delivery-events` → logs order delivered
6. **Inventory** listens to `payment-events` → commits stock reservation

### View Kafka Topics

```bash
# Port forward Kafka
kubectl port-forward svc/kafka 9092:9092 -n data

# List topics (in another terminal)
kubectl exec -it kafka-0 -n data -- \
  kafka-topics.sh --list --bootstrap-server localhost:9092

# View messages
kubectl exec -it kafka-0 -n data -- \
  kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic order-events \
    --from-beginning
```

---

## 🔍 Debugging & Monitoring

### Check Pod Status

```bash
# See all pods
kubectl get pods -A

# Describe a pod (see events/errors)
kubectl describe pod <pod-name> -n <namespace>

# View logs
kubectl logs <pod-name> -n <namespace>

# Follow logs (tail -f)
kubectl logs -f <pod-name> -n <namespace>

# View previous logs (if pod crashed)
kubectl logs <pod-name> -n <namespace> --previous
```

### Common Issues

**Issue: Pod stuck in `Pending` state**
```bash
# Check events
kubectl describe node

# Usually: not enough resources or image unavailable
# Solution: ensure Docker is running, k3s has enough memory
```

**Issue: Application won't start (CrashLoopBackOff)**
```bash
# View error logs
kubectl logs <pod-name> -n <namespace> --previous

# Common cause: missing environment variables
# Fix: check application.properties for required config
```

**Issue: Services can't reach Kafka/Postgres**
```bash
# Check service DNS
kubectl exec -it <pod> -n <namespace> -- nslookup kafka

# Should resolve to: kafka.data.svc.cluster.local
# If not: network policy or DNS issue
```

### View Metrics & Traces

- **Prometheus**: http://localhost:9090/
  - Query: `rate(http_requests_total[5m])`
  - See request rates across services

- **Grafana**: http://localhost:3000/
  - Dashboard for request latency, error rates
  - Pre-built dashboards available

- **Tempo**: View traces for distributed tracing
  - Query via Grafana

---

## 🔐 Required Environment Variables

Each microservice requires these environment variables (checked at startup):

```bash
# Database
DB_HOST=postgres
DB_NAME=ecomm
DB_USER=ecomm
DB_PASSWORD=ecomm-password
DB_SCHEMA=<service-specific>  # e.g., auth, catalog, etc.

# Kafka
KAFKA_HOST=kafka

# Keycloak (auth-service only)
KEYCLOAK_ISSUER_URI=http://keycloak.identity.svc.cluster.local/realms/ecomm
```

**Important:** If any required variable is missing, the service **fails to start** (no defaults).

This is intentional to ensure explicit configuration in production.

---

## 📝 Configuration Files

### Key Files to Know

| Path | Purpose |
|------|---------|
| `argocd-apps/` | ArgoCD Application manifests (infrastructure + services) |
| `services/*/` | Individual microservices (Java + Dockerfile) |
| `services/*/.github/workflows/build.yml` | GitHub Actions CI/CD pipeline |
| `services/*/src/main/resources/application.properties` | Service configuration (no defaults) |
| `charts/common-microservice/` | Shared Helm chart template |

### Kubernetes Manifests

Services are deployed via Helm values saved in ArgoCD Applications:

```yaml
# Example: argocd-apps/apps-checkout-service.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: apps-checkout-service
  namespace: argocd
spec:
  source:
    repoURL: <Docker registry>
    chart: common-microservice
    helm:
      values: |
        # Service replicas, resource limits, etc.
```

---

## 🎓 Learning Path

1. **Understanding the Flow** (1 hour)
   - Deploy the platform
   - Place an order via REST API
   - Watch events flow through Kafka
   - View logs in each service

2. **Modify a Service** (2 hours)
   - Clone this repo
   - Edit `services/checkout-service/src/main/java/CheckoutController.java`
   - Build locally: `mvn clean package`
   - Deploy: update Kubernetes, watch it restart

3. **Add a New Service** (3 hours)
   - Create `services/my-new-service/`
   - Copy structure from existing service
   - Add Kafka consumer/producer
   - Create ArgoCD Application
   - Push to GitHub → auto-deployed

4. **Deploy to Real Kubernetes** (advanced)
   - Create a cloud k8s cluster (EKS, GKE, etc.)
   - Store secrets in external vault (AWS Secrets Manager, Hashicorp Vault)
   - Configure HTTPS with cert-manager
   - Add ingress rules for domain names

---

## 🛠️ Useful Commands

```bash
# Cluster health
kubectl get nodes
kubectl get pods -A

# ArgoCD status
kubectl get applications -n argocd
argocd app list  # if ArgoCD CLI installed

# View service logs
kubectl logs -f deployment/<service-name> -n <namespace>

# Port forward any service
kubectl port-forward svc/<service-name> 8081:8080 -n <namespace>

# Exec into a pod
kubectl exec -it <pod-name> -n <namespace> -- /bin/bash

# Delete everything and start fresh
kubectl delete namespace argocd auth-service catalog-service # ... etc
k3s server --reset  # CAUTION: deletes entire k3s cluster data
```

---

## 📚 References

- **Kubernetes Basics**: https://kubernetes.io/docs/concepts/
- **ArgoCD Docs**: https://argo-cd.readthedocs.io/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Kafka**: https://kafka.apache.org/
- **Helm**: https://helm.sh/docs/

---

## 🤝 Next Steps

1. ✅ Get the platform running locally
2. 🔧 Modify a service and redeploy
3. 📈 Add metrics/tracing to your code
4. 🚀 Deploy to a cloud provider
5. 🔐 Implement OAuth 2.0 authentication properly
