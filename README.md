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
Ausserdem wurden alle Resourcen Angaben parametrisiert und für die Verwendung des Images in der DeploymentConfig muss noch ein ImageStream erstellt werden. Der Parameter *NAMESPACE* wird in seiner urspünglichen Bedeutung (in welchem
Namespace liegt der Jenkins *ImageStream* nicht mehr benötigt. Stattdessen wird er nun dafür verwendet den Namen des *Namespaces*, in dem Jenkins läuft als ENV Variable an den Pod mitzugeben. Dieser wird dort bspw für die Namen der aus
Openshift importierten Secrets benötigt.
Die Installation in Openshift im Projekt *projectname* erfolgt mit den folgenden Befehlen. Diverse Parameter sind im Template enthalten und können angepasst werden.
Im AGI werden die definierten Defaults verwendet. Eine Ausnahme bildet der Parameter *NAMESPACE*. Dieser muss mitgegeben werden und beinhaltet den Namen des *NAMESPACES*, in dem Jenkins laufen wird (im Beispiel agi-apps-test).

```
oc login
oc project projectname
git clone https://github.com/sogis/pipelines.git
cd pipelines
oc process -f jenkins-persistent-template.yaml -p NAMESPACE=agi-apps-test | oc apply -f- 
```
Das Login im Jenkins erfolgt über Openshift OAuth Authentifizierung, die vom Openshift Login Plugin zur Verfügung gestellt wird

Die im AGI vorhandenen Job Definitionen sind in diesem Repo unter Jobs abgelegt. Damit diese für den Jenkins Server verfügbar gemacht werden müssen sie in den *Pod* hochgeladen werden
```
oc project projectname
oc rsync jobs/ jenkins-podname:/var/lib/jenkins/jobs/
```
Anschliessend muss der Jenkins *Pod* neu gestartet werden

### Use credentials from Openshift secrets in Pipeline

Necessary for credential use in Jenkins is the openshift-jenkins-sync-plugin (https://github.com/openshift/jenkins-sync-plugin)
Im verwendeten Openshift Jenkins Server ist das Plugin defaultmässig aktiviert.

Um Openshift Credentials in Jenkins zu verwenden (z.B. für ein privates Github repo oder DB Passwörter und Usernamen) sind die folgenden Schritte notwendig:

* *Secret* in Openshift mit Passwort und Username erstellen
```
apiVersion: v1
kind: Secret
metadata:
  name: github-secret
stringData:
  username: user1
  password: password1
```
* Das *Secret* mit credential.sync.jenkins.openshift.io=true belabeln.
```
oc label secret github-secret credential.sync.jenkins.openshift.io=true
```
* Jetzt sollte das *Secret* in Jenkins als *global credential* verfügbar sein (projectname-secretname). Nachfolgend ein Codeschnipsel als Beispiel, wie man ein solches *global credential*, das aus dem *Secret* 
*pw-ogc-server* im Projekt *agi-apps-test* entstanden ist, in einer Pipeline einbindet
```
credentials = [
    usernamePassword(credentialsId: 'agi-apps-test-pw-ogc-server'          , usernameVariable: 'DbUserOgcServer'         , passwordVariable: 'PwdOgcServer'),
]
pipeline {
  stages {
    stage ('Configure Ressources') {
      steps {
        withCredentials ( credentials ) {
          configureRessources '${namespace}', "-f ${repo}/resources.yaml -p USER_OGC_SERVER=${DbUserOgcServer} -p PW_OGC_SERVER=${PwdOgcServer}"
          }
        }
      }
    }
  }
```

Weitere Informationen unter https://docs.openshift.com/container-platform/3.11/using_images/other_images/jenkins.html#sync-plug-in

### Credentials für apiUser
Damit via Pipeline auf die Jenkins Api zugegriffen werden kann wird ein Credentials mit Namen *jenkinsApi* erstellt.
Dafür unter https://jenkins-agi-apps-test.dev.so.ch/credentials/store/system/domain/_/newCredentials ein neues Credentials erstellen.
Username ist *apiUser* Passwort wird mit keypass erstellt und dort abgespeichert.

### Labeling Richtlinien
Folgende Labeling Richtlinien gelten für **alle** im Zusammenhang mit Jenkins erstellten Openshift Objekte, also auch für die Openshift Objekte, die für Pipelines benötigt werden.
Bei Objekten die nur für Jenkins benötigt werden soll folgendes Label gesetzt werden
```
metadata:
  labels:
    app: jenkins-persistent
```

Bei Objekten, die für die Ausführung von Pipelines in Jenkins benötigt werden, wie etwa *Secrets* oder Agents sollte zusätzlich der Name der Pipeline, für die das Objekt erstellt wird als Label ergänzt werden.
```
metadata:
  labels:
    app: jenkins-persistent
    pipeline: wgc
```
