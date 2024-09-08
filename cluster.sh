k3d cluster create --api-port 6550 -p '80:80@loadbalancer' -p '443:443@loadbalancer' --agents 1 --k3s-arg="--disable=traefik@server:0"
istioctl install --set profile=demo -y
kubectl apply -f https://raw.githubusercontent.com/istio/istio/master/manifests/charts/base/crds/crd-all.gen.yaml

kubectl label namespace default istio-injection=enabled
helm install community-operator mongodb/community-operator
#kubectl apply -f https://raw.githubusercontent.com/mongodb/mongodb-kubernetes-operator/master/config/samples/mongodb.com_v1_mongodbcommunity_cr.yaml
helm install my-strimzi-cluster-operator --set replicas=3 oci://quay.io/strimzi-helm/strimzi-kafka-operator
k3d image import temperature_anomaly_analyzer temperature_measurement_generator anomalies_delivery anomalies_delivery_ui
kubectl apply -R -f k8s/
