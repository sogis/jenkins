def call(String appName, String repo, String stage, String params, String configFileName, String jobName, String buildNumber, String branch, String arguments) {
    sh """
       pwd
       if [ -d "qwc_services" ]; then
         rm -rf qwc_services/* rm -rf qwc_services/.git
       fi
       git clone -b $branch https://github.com/sogis/qwc_services.git
    """
    dir("qwc_services") {
       sh """
          git config user.email agi.info@bd.so.ch
          git config user.name agiuser
       """
       if ( appName == "qwc_service" || appName == "qgis-server" || appName == "legend-service" ) {
          if ( appName == "qwc-service" ) {
             if ( stage == "gdi-production" ) {
                sh """
                   sed -i "s|geo-i.so.ch/analytics|geo.so.ch/analytics|" $appName/index.html
                   git add $appName/index.html
                """
                }
             filetypes = '*.json'
             gitAddFiles = appName + '/' + configFileName
             }
          else if ( appName == "qgis-server" ) {
             filetypes = '*.qgs'
             gitAddFiles = appName + '/' + configFileName + ' ' + appName + '/somap_wfs.qgs ' + appName + '/somap_print.qgs'
             }
          else if ( appName == "legend-service" ) {
             filetypes = '*.json, *.png'
             gitAddFiles = appName + '/' + configFileName + ' ' + appName + '/permissions.json ' + appName + '/*.png'
             }
          else {
             filetypes = '*.json'
             gitAddFiles = appName + '/' + configFileName + ' ' + appName + '/permissions.json'
             }
          sh """
             wget -r -np -nd -erobots=off -A '$filetypes' --reject-regex '/\\*.+\\*/|auto_refresh' --no-check-certificate --auth-no-challenge --user='$apiUser' --password='$PwdApiUser'  '$JENKINS_URL/job/$jobName/$buildNumber/artifact/config/default/' -P $appName
             ls -la data-service

             git add $gitAddFiles
             ls -la $appName
             git commit -m 'added files'
          """
          }
       }
    sh """   
       oc process -f $repo/buildconfig.yaml $params | oc apply -n $stage -f- 
       oc start-build $appName -n $stage --from-repo=qwc_services --wait $arguments
       rm -rf qwc_services/* rm -rf qwc_services/.git
    """
    }
