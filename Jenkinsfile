pipeline {
  agent {
    label "builder-maven-nuxeo"
  }
  environment {
    ORG = 'glefevre'
    APP_NAME = 'nuxeo-notification-stream'
    CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BUILD_NUMBER"
    PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
  }
  stages {
    stage('CI Build and push snapshot image') {
      when {
        branch 'feature-*'
      }
      steps {
        container('maven-nuxeo') {
          sh "mvn install -DskipTests"
          sh "export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"

          dir('charts/preview') {
            sh "make preview"
            sh "jx preview --pull-secrets instance-clid --app $APP_NAME --namespace $PREVIEW_NAMESPACE --dir ../.."
          }

          script {
            previewUrl = sh(script: "kubectl get ing --namespace $PREVIEW_NAMESPACE | awk '{if (NR == 2) {print \$2}}'", returnStdout: true).trim()
          }
        }
      }
    }
    stage('Run FTests') {
      when {
        branch 'feature-*'
      }
      environment {
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
      }
      steps {
        echo "${previewUrl}"
        container('maven-nuxeo') {
           dir('nuxeo-notification-stream-ftests') {
             sh "npm config set @nuxeo:registry http://nexus.jx.35.231.200.170.nip.io/repository/test-gildas/"
             sh "rm -fr node_modules || true"
             sh "npm install --no-package-lock"
             sh "npm run test -- --nuxeoUrl=http://${previewUrl}/nuxeo"
           }
        }
      }
    }
    stage('Build Release') {
      when {
        branch 'master'
      }
      steps {
        container('maven-nuxeo') {

          // ensure we're not on a detached head
          sh "git checkout master"
          sh "git config --global credential.helper store"
          sh "jx step git credentials"

          // so we can retrieve the version in later steps
          sh "echo \$(jx-release-version) > VERSION"
          // sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          // sh "jx step tag --version \$(cat VERSION)"
          // sh "mvn clean deploy"
          sh "export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
        }
      }
    }
    stage('Promote to Environments') {
      when {
        branch 'master'
      }
      steps {
        container('maven-nuxeo') {
          dir('charts/nuxeo-notification-stream') {
            sh "jx step changelog --version v\$(cat ../../VERSION)"

            // release the helm chart
            sh "jx step helm release"

            // promote through all 'Auto' promotion Environments
            sh "jx promote -b --all-auto --timeout 1h --version \$(cat ../../VERSION)"
          }
        }
      }
    }
  }
  post {
        always {
          cleanWs()
        }
  }
}
