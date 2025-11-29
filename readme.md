# Kubeadm Installation Checklist
Use this checklist to ensure all prerequisite steps are completed in the correct order.

## Phase 1: All Nodes (Control Plane & Workers)

[✅] Provision cloud VMs (1 Control Plane, 2+ Workers).  
[✅] Ensure nodes have unique hostnames

--Swap configuration
[✅] Disable swap memory `sudo swapoff -a`.  
[✅] Make swap disable persistent `sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab`.  

--CRI configuration
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

--Networking Prerequisites configuration
[✅] Load kernel modules (overlay, br_netfilter), Configure sysctl settings for Kubernetes networking (net.bridge.bridge-nf-call-iptables, net.ipv4.ip_forward, etc.), and Apply sysctl changes (sudo sysctl --system).  
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

## Phase 2: Local Workstation

[✅] Install kubectl (if not already present)
[✅] Install Lens Desktop by downloading it from the official website.  

## Phase 3: Control Plane Node Only

[✅] Run cluster initialization:
```shell
sudo kubeadm init \
  --apiserver-advertise-address=<control_plane-ip> \
  --apiserver-cert-extra-sans=<control_plane-ip> \
  --pod-network-cidr=10.244.0.0/16
```
[✅] Create .kube directory on the control plane, Copy the admin config: sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config, Set correct ownership: sudo chown $(id -u):$(id -g) $HOME/.kube/config
```
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

[✅] Install a Pod Network CNI plugin (e.g., Flannel): kubectl apply -f...kube-flannel.yml.  
```
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```
[✅] Verify control plane node moves to Ready status: `kubectl get nodes`
[✅] CRITICAL: Save the kubeadm join... command printed to your terminal.  

## Phase 4: Worker Nodes Only

[✅] For each worker node, run the kubeadm join command saved from Phase 3.  
```shell
# example
sudo kubeadm join <master-ip>:6443 --token <TOKEN> --discovery-token-ca-cert-hash sha256:<HASH>
```
[✅] On the control plane, verify all worker nodes are Ready: `kubectl get nodes`

## Phase 5: Local Workstation (GUI Connection)

[✅] Copy the contents of $HOME/.kube/config from your control plane to your local machine's ~/.kube/config file
[✅] Open Lens. It should automatically detect and connect to your new cluster

## Phase 6: MONITORING Metrics Lens
1. ensure metrics-server installed + prometheus-kube-stack

### TROUBLESHOOTING:
🆘 IF v1beta1.metrics.k8s.io FailedDiscoveryCheck
-> get into cp and very nod check the access by curl the service metrics-server servic
-> [PROBABLE SOLUTION]F lannel, which (by default) creates an "overlay network" using the VXLAN
VXLAN = This protocol wraps your pod traffic (like the ping) into UDP packets to send between nodes.
Flannel's VXLAN backend requires UDP port 8472 to be open between all nodes.
##TROUBLESHOOTING:
so add `ufw allow 8472/udp`

# Additional
below is an advance improvement to our vanilla k8s cluster

## HPA simulations:
in order to run HPA simulations in our k8s cluster you need following tools stacks : 
#### 1. service deployment with helm template
[✅] you may use service that i provided, use helm template in \logservice\k8s
[✅] ensure HPA rule implemented on kube config > Horizontal Pod Autoscalers > logservice-k8s

#### 2. longhorn
To all nodes do:
```shell
# Install prerequisites
sudo apt-get update
sudo apt-get install open-iscsi nfs-common -y

# Enable and start the iSCSI service
sudo systemctl enable --now iscsid

# Verify it's running
sudo systemctl status iscsid
```
then install using helm chart
```shell
# Then install Longhorn
kubectl apply -f https://raw.githubusercontent.com/longhorn/longhorn/v1.5.1/deploy/longhorn.yaml

# Wait and verify all Longhorn pods are running
kubectl get pods -n longhorn-system -w
```

#### 3. postgres
since our service use DB connection, install postgres to your stateful set
```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install my-postgresql bitnami/postgresql --version 18.1.11
```

#### 4. postman
needed only to do performance test

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

#### 3. Kiali
install kiali here with helm chart https://kiali.io/docs/installation/installation-guide/install-with-helm/



## Canary Deployment:
Advanced Traffic Management we will learn about VirtualService, and DestinationRule