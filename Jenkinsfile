pipeline {
    agent any

    environment {
        SPRING_PROFILES_ACTIVE="prod"
        POSTGRESQL_CRED = credentials('postgres-id')
        DB_JDBC_USER = "${POSTGRESQL_CRED_USR}"
        DB_JDBC_PASSWORD = "${POSTGRESQL_CRED_PSW}"
        SERVER_PORT=8099
        DATABASE_URL="jdbc:postgresql://postgresql:5432/dainf_labs"
        DATABASE_USERNAME="${POSTGRESQL_CRED_USR}"
        DATABASE_PASSWORD="${POSTGRESQL_CRED_PSW}"

        GOOGLE_CRED = credentials('dainf_labs_google_client_id')
        GOOGLE_CLIENT_ID="${GOOGLE_CRED_USR}"
        GOOGLE_CLIENT_SECRET="${GOOGLE_CRED_PSW}"

        EMAIL_CRED = credentials('UTFPRAPPSEmailAndPassword')
        UTFPR_TOKEN_SECRET="${EMAIL_CRED_PSW}"
        UTFPR_EMAIL_ADDRESS="${EMAIL_CRED_USR}"
        UTFPR_EMAIL_PASSWORD="${EMAIL_CRED_PSW}"
        UTFPR_EMAIL_USERNAME="${EMAIL_CRED_USR}"
        UTFPR_EMAIL_HOST="200.19.73.102"
        UTFPR_EMAIL_PORT="465"

        UTFPR_FRONT_URL="https://dainf-labs.app.pb.utfpr.edu.br/#"


        MINIO_CRED = credentials('dainf_labs_minio_id')
        MINIO_ENDPOINT="https://minio.app.pb.utfpr.edu.br"
        MINIO_PORT=443
        MINIO_ACCESS_KEY="${MINIO_CRED_USR}"
        MINIO_SECRET_KEY="${MINIO_CRED_PSW}"
        MINIO_SECURE=true
        MINIO_BUCKET_NAME="dainf-labs"
    }

    stages {   
        stage('Docker Compose UP') {
            steps {
                sh 'docker compose up -d --build'
            }
        }
    }
}
