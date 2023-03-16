Betrieb AGI Jenkins und Pipeline Definitionen

siehe https://github.com/sogis/dok/blob/dok/dok_betrieb/Documents/jenkins/jenkins.md

**Achtung**

Damit der Abgleich der Openshift Secrets mit Jenkins funktioniert müssen bestimmte Abhängigkeiten beim Openshift Sync Plugin beachtet werden.
Die notwendigen Plugin Versionen sind im *plugins.txt* vermerkt.
Wenn das Openshift Sync Plugin upgedated wird muss geprüft werden, ob die Synchronisation noch funktioniert. Das sieht im Jenkins Log dann in etwa so aus
(das ist nur der Beginn danach folgen dann Log Einträge für den Abgleich von builder, secrets, etc)

```
2023-03-16 10:43:09 INFO io.fabric8.jenkins.openshiftsync.GlobalPluginConfiguration configChange OpenShift Sync Plugin processing a newly supplied configuration
2023-03-16 10:43:09 INFO io.fabric8.jenkins.openshiftsync.OpenShiftUtils shutdownOpenShiftClient Stopping openshift client: null
2023-03-16 10:43:09 INFO io.fabric8.jenkins.openshiftsync.OpenShiftUtils initializeOpenShiftClient Current OpenShift Client Configuration: io.fabric8.openshift.client.OpenShiftConfig@2c4cb1bc[oapiVersion=v1,openShiftUrl=https://kubernetes.default:443/oapi/v1/,buildTimeout=300000,openshiftApiGroupsEnabled=false,disableApiGroupCheck=false,trustCerts=true,disableHostnameVerification=false,masterUrl=https://kubernetes.default:443/,apiVersion=v1,namespace=agi-apps-test,caCertFile=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt,caCertData=<null>,clientCertFile=<null>,clientCertData=<null>,clientKeyFile=<null>,clientKeyData=<null>,clientKeyAlgo=<null>,clientKeyPassphrase=changeit,trustStoreFile=<null>,trustStorePassphrase=<null>,keyStoreFile=<null>,keyStorePassphrase=<null>,authProvider=<null>,requestConfig=io.fabric8.kubernetes.client.RequestConfig@28c8be74,contexts=[],currentContext=<null>,username=<null>,password=<null>,oauthToken=<null>,watchReconnectInterval=1000,watchReconnectLimit=-1,connectionTime...
2023-03-16 10:43:09 INFO io.fabric8.jenkins.openshiftsync.OpenShiftUtils initializeOpenShiftClient New OpenShift client initialized: io.fabric8.openshift.client.DefaultOpenShiftClient@5feea50d
2023-03-16 10:43:09 WARNING org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration setCollectDiskUsageBasedOnEnvironmentVariableIfDefined Unable to parse environment variable 'COLLECT_DISK_USAGE'. Must either be 'true' or 'false'. Ignoring...
2023-03-16 10:43:09 WARNING org.jenkinsci.plugins.prometheus.DiskUsageCollector collect Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not installed: java.lang.NoClassDefFoundError: com/cloudbees/simplediskusage/QuickDiskUsagePlugin
2023-03-16 10:43:09 INFO jenkins.InitReactorRunner$1 onAttained System config loaded
2023-03-16 10:43:10 INFO jenkins.InitReactorRunner$1 onAttained System config adapted
2023-03-16 10:43:10 INFO jenkins.InitReactorRunner$1 onAttained Loaded all jobs
2023-03-16 10:43:10 INFO jenkins.InitReactorRunner$1 onAttained Configuration for all jobs updated
2023-03-16 10:43:10 INFO hudson.model.AsyncPeriodicWork lambda$doRun$1 Started Download metadata
2023-03-16 10:43:10 INFO jenkins.util.groovy.GroovyHookScript execute Executing /var/lib/jenkins/init.groovy.d/update-center-init.groovy
2023-03-16 10:43:10 INFO hudson.model.AsyncPeriodicWork lambda$doRun$1 Finished Download metadata. 6 ms
2023-03-16 10:43:10 INFO jenkins.InitReactorRunner$1 onAttained Completed initialization
2023-03-16 10:43:10 WARNING jenkins.branch.WorkspaceLocatorImpl getWorkspaceRoot JENKINS-2111 path sanitization ineffective when using legacy Workspace Root Directory ‘${ITEM_ROOTDIR}/workspace’; switch to ‘${JENKINS_HOME}/workspace/${ITEM_FULL_NAME}’ as in JENKINS-8446 / JENKINS-21942
2023-03-16 10:43:10 INFO hudson.lifecycle.Lifecycle onReady Jenkins is fully up and running
2023-03-16 10:43:10 INFO hudson.util.Retrier start Attempt #1 to do the action check updates server
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.GlobalPluginConfigurationTimerTask doRun Confirming Jenkins is started
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.GlobalPluginConfigurationTimerTask stop Stopping all informers ...
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.GlobalPluginConfigurationTimerTask stop Stopped all informers
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.BuildConfigInformer start Starting BuildConfig informer for agi-apps-test!!
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.BuildConfigManager reconcileJobsAndBuildConfigs Reconciling jobs and build configs
2023-03-16 10:43:10 INFO io.fabric8.jenkins.openshiftsync.BuildConfigManager reconcileJobsAndBuildConfigs Checking job org.jenkinsci.plugins.workflow.job.WorkflowJob@3e548633[agi-apps-test/agi-apps-test-ilivalidator-pipeline] runs for BuildConfig agi-apps-test/ilivalidator-pipeline
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildConfigManager reconcileJobsAndBuildConfigs Reconciling jobs and build configs
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildConfigManager reconcileJobsAndBuildConfigs Checking job org.jenkinsci.plugins.workflow.job.WorkflowJob@3e548633[agi-apps-test/agi-apps-test-ilivalidator-pipeline] runs for BuildConfig agi-apps-test/ilivalidator-pipeline
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildConfigInformer onAdd BuildConfig informer received add event for: custom-jenkins-build
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildManager reconcileRunsAndBuilds Reconciling job runs and builds
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildConfigInformer onAdd BuildConfig informer received add event for: ilivalidator-pipeline
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildConfigInformer start BuildConfig informer started for namespace: agi-apps-test
2023-03-16 10:43:11 INFO io.fabric8.jenkins.openshiftsync.BuildInformer start Starting Build informer for agi-apps-test!!
```

Zu beachten sind die Abhängigkeiten die sich im pom.xml der entsprechenden Version finden (Beispiel Version 1.0.55 https://github.com/jenkinsci/openshift-sync-plugin/blob/openshift-sync-1.0.55/pom.xml)
