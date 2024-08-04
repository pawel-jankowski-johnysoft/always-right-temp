#k3d cluster create --api-port 6550 -p '9080:80@loadbalancer' -p '9443:443@loadbalancer' --agents 2 --k3s-arg '--disable=traefik@server:*'
k3d cluster create --api-port 6550 -p '80:80@loadbalancer' -p '443:443@loadbalancer' --agents 2 --k3s-arg="--disable=traefik@server:0"
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
helm install community-operator mongodb/community-operator
#kubectl apply -f https://raw.githubusercontent.com/mongodb/mongodb-kubernetes-operator/master/config/samples/mongodb.com_v1_mongodbcommunity_cr.yaml
helm install my-strimzi-cluster-operator --set replicas=3 oci://quay.io/strimzi-helm/strimzi-kafka-operator
kubectl apply -f k8s/kafka.yml
k3d image import temperature_anomaly_analyzer
k3d image import temperature_measurement_generator
k3d image import anomalies_delivery
k3d image import anomalies_delivery_ui
kubectl apply -f k8s/
