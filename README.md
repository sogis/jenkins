# AGI Jenkins
Betrieb AGI Jenkins und Pipeline Definitionen

## Betrieb Jenkins
Die Pipelines werden mit dem in Openshift enthaltenen Jenkins betrieben.
Es wird dafür die persistierte Jenkins Variante verwendet. Der Vorteil ist, dass der durch Openshift deployte Jenkins durch eine Reihe von Openshift Jenkins Plugins vollständig mit Openshift gekoppelt ist.
Einerseits kann so direkt auf Ressourcen innerhalb des Projekts zugegriffen werden, andererseits können durch entsprechendes Labelling dynamische Slaves aufgesetzt werden. Des Weiteren wird auch ein entsprechender Serviceaccount (jenkins) erstellt. Die Rechtevergabe kann entsprechend über diesen Serviceacount erfolgen.
Zum Einsatz kommt das Openshift Jenkins Client Plugin (direkte Kommunikation mit Openshift Cluster), das Openshift Jenkins Sync Plugin und das Kubernetes Plugin.
Der Jenkins wird im Namespace AGI Applications betrieben.
### ConfigMap jenkins-env-config
Datenbanknamen und die Namen der Datenbankserver sollten nicht in öffentlichen Repos auftauchen. Damit diese nicht in den Pipelines verwendet werden müssen, werden sie im Jenkins Server als ENV Variablen vorgehalten.
Damit diese ENV Variablen vorhanden sind wird eine *ConfigMap* mit den entsprechenden Einträgen für die ENV Variablen erstellt. Diese *ConfigMap* ist nicht Teil des nachfolgenden Installationstemplates, da ansonsten Datenbanknamen und
Namen der Datenbankserver ebenfalls wieder in einem öffentlichen Repo zu finden wären. Die *ConfigMap* liegt deshalb unter H:\BJSVW\Agi\GDI\Betrieb\Openshift\Pipelines\configmap-jenkins-env-vars.yaml
Sie wird folgendermassen im gleichen Projekt wie der Jenkins Server läuft erstellt.
```
oc project projectname
oc apply -f configmap-jenkins-env-vars.yaml
```
Die *DeploymentConfig* für den Jenkins Server ist im Jenkins Template für die Nutzung der *ConfigMap* entsprechend angepasst.
```
envFrom:
  - configMapRef:
    name: jenkins-env-config
```

### Installation Jenkins in Openshift
Für die Installation wurde das in Openshift enthaltene Template leicht angepasst.
So wird nun anstelle des in der Openshift Registry enthaltenen und veralteten Jenkins Images das Image *openshift/jenkins-2-centos7:v3.11* verwendet. Dieses ist aktueller und wird auch für den Gretl-Jenkins verwendet.
Ausserdem wurden alle Resourcen Angaben parametrisiert und für die Verwendung des Images in der DeploymentConfig muss noch ein ImageStream erstellt werden.
Die Installation in Openshift im Projekt *projectname* erfolgt mit den folgenden Befehlen. Diverse Parameter sind im Template enthalten und können angepasst werden.
Im AGI werden allerdings die definierten Defaults verwendet.

```
oc login
oc project projectname
git clone https://github.com/sogis/pipelines.git
cd pipelines
oc process -f jenkins-persistent-template.yaml | oc apply -f- 
```
Das Login im Jenkins erfolgt über Openshift OAuth Authentifizierung, die vom Openshift Login Plugin zur Verfügung gestellt wird

Die im AGI vorhandenen Job Definitionen sind in diesem Repo unter Jobs abgelegt. Damit diese für den Jenkins Server verfügbar gemacht werden müssen sie in den *Pod* hochgeladen werden
```
oc project projectname
oc rsync jobs/ jenkins-podname:/var/lib/jenkins/jobs/
```
Anschliessend muss der Jenkins *Pod* neu gestartet werden.
