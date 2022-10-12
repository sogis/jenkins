def call(String stage, String parameters){
    sh """
       oc process $parameters | oc apply -n $stage -f- 
    """
}
