pipeline {
  agent {
    label "jenkins-maven"
  }
  environment {
    ORG = 'glefevre'
    APP_NAME = 'nuxeo-notification-stream'
    CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BUILD_NUMBER"
  }
  stages {
    stage('CI Build and push snapshot image') {
      when {
        branch 'feature-*'
      }
      steps {
        container('maven') {
          sh "mvn install -DskipTests"
          sh "export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"
        }
      }
    }
    stage('Run FTests') {
      when {
        branch 'feature-*'
      }
      environment {
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
      }
      steps {
        container('maven') {
           dir('charts/preview') {
             sh "make preview"
             sh "jx preview --app $APP_NAME --dir ../.."
           }
           dir('nuxeo-notification-stream-ftests') {
             sh "rm -fr node_modules || true"
             sh "npm install --no-package-lock"
             sh "npm run test --nuxeoUrl=http://nuxeo-notification-stream.jx-glefevre-nuxeo-notification-stream-pr-feature-jx.35.231.200.170.nip.io/nuxeo"
           }
        }
      }
    }
    stage('Build Release') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {

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
        container('maven') {
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
