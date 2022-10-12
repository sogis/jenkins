def call(String appName, String stage, String version, String repo, String replicas, String cpu_request, String cpu_limit, String memory_request, String memory_limit, String params = "") {
    sh """
        oc process -f $repo/deploymentconfig.yaml -p NAMESPACE=$stage -p TAG=$version -p REPLICAS=$replicas -p CPU_REQUEST=$cpu_request -p CPU_LIMIT=$cpu_limit -p MEMORY_REQUEST=$memory_request -p MEMORY_LIMIT=$memory_limit $params | oc apply -n $stage -f-
        oc rollout latest -n $stage $appName
        oc rollout status -n $stage dc $appName
    """
    }
