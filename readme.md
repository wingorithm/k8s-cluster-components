# Kubeadm Installation Checklist #BuildK8sClusterWithMe
Use these checklists to ensure all prerequisite steps are completed in the correct order.

[pict here]

## Our Goal:
1. 🏗️ Setup onprem vanilla k8s cluster
2. 🔗 connect it to our k8s IDE (LENS)

you can also read from original docs here
- https://kubernetes.io/docs/reference/setup-tools/kubeadm/
- https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/

## Phase 1: Prepare All Nodes's Prerequisites 🧵

[✅] Provision cloud VMs (1 Control Plane, 2+ Workers). I use Vultr for the provisioning as shown below

[pict here]

[✅] Ensure nodes have unique hostnames

### 1. Swap configuration
[✅] Disable swap memory `sudo swapoff -a`.  
[✅] Make swap disable persistent `sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab`.  

### 2. CRI (Container Runtime interface) configuration
[✅] Install Docker’s official GPG key and repo.  
```shell
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```
[✅] Install containerd as the container runtime.  
```shell
sudo apt-get update
sudo apt-get install -y containerd.io
systemctl status containerd
```
[✅] Create containerd config directory `sudo mkdir -p /etc/containerd`

[✅] Generate default containerd config (containerd config default...), Set containerd to use SystemdCgroup = true, and Restart containerd.  
```shell
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml
sudo systemctl restart containerd
```

### 3. Networking Prerequisites configuration
[✅] Load kernel modules (overlay, br_netfilter), 

Configure sysctl settings for Kubernetes networking (net.bridge.bridge-nf-call-iptables, net.ipv4.ip_forward, etc.), 

and Apply sysctl changes (sudo sysctl --system).  
```shell
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### 4. Installing k8s Package and CLI 
[✅] Add the Kubernetes apt repository and GPG key.  
```shell
sudo apt-get install -y apt-transport-https ca-certificates curl gpg

# Download GPG key
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key | \
sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# Add repo (replace v1.31 with your desired major.minor if needed)
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] \
https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /" | \
sudo tee /etc/apt/sources.list.d/kubernetes.list
```
[✅] Install Kubernetes packages.  
`sudo apt-get update`
`sudo apt-get install -y kubelet kubeadm kubectl`

[✅] Hold package versions.  
`sudo apt-mark hold kubelet kubeadm kubectl`

## Phase 2: Local Workstation 💻
now let's switch the gear to our local workstation, we need a tool that might make our learning journey become easier (k8s cli and IDE 😘)...

[✅] Install kubectl (if not already present)

[✅] Install Lens Desktop by downloading it from the official website.  https://lenshq.io/products/lens-k8s-ide

## Phase 3: Control Plane Node 🧠
back to our cloud machine, now let's focus to instantiate the brain of our cluster the "control plane"

[✅] Run cluster initialization with kubeadm:
```shell
sudo kubeadm init \
  --apiserver-advertise-address=<control_plane-ip> \
  --apiserver-cert-extra-sans=<control_plane-ip> \
  --pod-network-cidr=10.244.0.0/16
```
[✅] IMPORTANT! Save the kubeadm join script... command printed to your terminal.  

[✅] Create .kube directory on the control plane, then copy the admin config: 
```shell
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config

# Set correct ownership:
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

[✅] Install a Pod Network CNI plugin (e.g., Flannel)
```
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```
[✅] Verify control plane node moves to Ready status: `kubectl get nodes`

## Phase 4: Worker Nodes 💪

[✅] For each worker node, run the kubeadm join command saved from Phase 3.  
```shell
# example
sudo kubeadm join <master-ip>:6443 --token <TOKEN> --discovery-token-ca-cert-hash sha256:<HASH>
```

[✅] On the control plane, verify all worker nodes are Ready: `kubectl get nodes`

## Phase 5: Local Workstation (GUI Connection) 💻
now we've completed the bare minimum of k8s cluster where there are a control-plane, worker, and proper CNI & CRI inside it. 
but it would be very wonderful if we are able to check it via our local right? (just like docker desktop)

[✅] Copy the contents of $HOME/.kube/config from your control plane to your local machine's ~/.kube/config file

[✅] Open Lens. It should automatically detect and connect to your new cluster

[picture here]

## Phase 6: MONITORING Metrics Lens 💹
[✅] ensure metrics-server installed + prometheus-kube-stack

for this step please learn about helm chart first
```shell
#  metrics server -----------
helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/
helm repo update

helm install metrics-server metrics-server/metrics-server \
  --namespace kube-system \
  --set args={--kubelet-insecure-tls}
  
#  prometheus stack -----------
kubectl create namespace monitoring
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install kube-prom-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring
```

[✅] validate the metrics-server works `kubectl top nodes`

### TROUBLESHOOTING:
#### 🆘 IF v1beta1.metrics.k8s.io FailedDiscoveryCheck
-> get into cp and very nod check the access by curl the service metrics-server servic

-> [PROBABLE SOLUTION] Flannel, which (by default) creates an "overlay network" using the VXLAN
VXLAN = This protocol wraps your pod traffic (like the ping) into UDP packets to send between nodes.
Flannel's VXLAN backend requires UDP port 8472 to be open between all nodes.

so add `ufw allow 8472/udp`

#### 🆘 IF metrics not fully shown on your LENS IDE
-> check your prometheus UI -> targets -> look for node exporter
[pict here]

-> [PROBABLE SOLUTION] if above is the case than you exporter still using hostIP 🥱, change it to k8s ClusterIp
```shell
# in prometheus helm chart node exporter segments set hostNetwork into false
prometheus-node-exporter:
  hostNetwork: false
  service:
    type: ClusterIP
    
# in prometheus segments, add nodeAddresses
prometheus:
 prometheusSpec:
    nodeAddresses:
      - InternalIP
```
[result pic heere]

---
# Additional
below is an opt advance improvement to our vanilla k8s cluster

## HPA simulations:
in order to run HPA simulations in our k8s cluster you need following tools stacks : 
#### 1. service deployment with helm template
[✅] you may use service that i provided, use helm template in \logservice\k8s

[✅] ensure HPA rule implemented on kube config > Horizontal Pod Autoscalers > logservice-k8s

#### 2. longhorn
[✅] To all nodes install iSCSI and NFS:
```shell
# Install prerequisites
sudo apt-get update
sudo apt-get install open-iscsi nfs-common -y

# Enable and start the iSCSI service
sudo systemctl enable --now iscsid

# Verify it's running
sudo systemctl status iscsid
```
[✅] then install longhorn using helm chart
```shell
# Then install Longhorn
kubectl apply -f https://raw.githubusercontent.com/longhorn/longhorn/v1.5.1/deploy/longhorn.yaml

# Wait and verify all Longhorn pods are running
kubectl get pods -n longhorn-system -w
```

#### 3. postgres
[✅]  since our service use DB connection, install postgres to your stateful set
```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install my-postgresql bitnami/postgresql --version 18.1.11
```

#### 4. postman
needed only to do performance test, watch how the test run: https://drive.google.com/file/d/1dEaIL-jdiKmjW9g1tegCVYjbXdrOhT7N/view?usp=sharing


## Service Mesh implementation using istio:
why need to use istio? https://www.youtube.com/watch?v=6zDrLvpfCK4

do we need gateway service? Strictly speaking, No.

in order to makes istio Service Mesh you need following tools stacks :

#### 1. multiple services
use just use all service in this repo
```shell
cd /a_service/k8s
helm install a-service ./ -n development

cd /b_service/k8s
helm install b-service ./ -n development
```

#### 2. istio and istio CLI
go to your control plane and run below command to download the Istio CLI
```shell
curl -L https://istio.io/downloadIstio | sh -
cd istio-1.28.0
export PATH=$PWD/bin:$PATH

#Check that you are able to run istioctl
istioctl version
```
we use istio ambient follow this setup checklist
https://istio.io/latest/docs/ambient/getting-started/

`kubectl apply -f .\gateway.yml`
`kubectl annotate gateway main-gateway networking.istio.io/service-type=ClusterIP --namespace=istio-system`

to register our services and database into istio service mesh we need to add label to the desired namespace
for services:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: development
  uid: e2b7978f-c6e6-464e-ada4-979a5fc4b416
  resourceVersion: '2752837'
  creationTimestamp: '2025-11-23T03:32:52Z'
  labels:
    kubernetes.io/metadata.name: development
    istio.io/dataplane-mode: ambient #add this to our business logic namespace
#the rest of config...
```

for databases (since it on default namespace):
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: default
  uid: a4db0581-e38f-4629-bde9-84681b957478
  resourceVersion: '17'
  creationTimestamp: '2025-11-02T06:55:15Z'
  labels:
    kubernetes.io/metadata.name: default
    istio.io/dataplane-mode: ambient #add this
```
#### 3. Kiali
install kiali here with helm chart https://kiali.io/docs/installation/installation-guide/install-with-helm/

kiali need prometheus to get essential metrics in the cluster, so ensure you implement this, since prometheus has created in other namespace
```yaml
#on kiali yaml file
spec:
  # other config... 
  external_services:
    grafana:
      # since we don't setup any dns to outside cluster so just us localhost, and we'll access via port forward
      url: "http://localhost:9098"
      internal_url: "http://prometheus-grafana.monitor.svc.cluster.local:80"
    prometheus:
      url: "http://prometheus-kube-prometheus-prometheus.monitor.svc.cluster.local:9090" #Add this
```

in this stag you'll able to see the mesh

[result pict here]

know let's add a little bit of config so that you can get metrics from service mesh and abl to see traffic visualization inside the mesh
apply `monitor-istio.yaml` and `monitor-apps.yaml`
```shell
kubectl apply -f .\monitor-istio.yaml
kubectl apply -f .\monitor-apps.yaml  
```

[pict definition here]
## Canary Deployment: <nanti aja ya, saya males hahaha>
Advanced Traffic Management we will learn about VirtualService, and DestinationRule