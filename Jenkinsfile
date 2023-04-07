pipeline {
    agent any

    environment {
        POSTGRESQL_CRED = credentials('postgres-id')
        DB_JDBC_USER = "${POSTGRESQL_CRED_USR}"
        DB_JDBC_PASSWORD = "${POSTGRESQL_CRED_PSW}"
        SERVER_PORT=8099
        DATABASE_URL="jdbc:postgresql://postgresql:5432/dainf_labs"
        DATABASE_USERNAME="${POSTGRESQL_CRED_USR}"
        DATABASE_PASSWORD="${POSTGRESQL_CRED_PSW}"

        GOOGLE_CRED = credentials('google_client_id')

        GOOGLE_CLIENT_ID="${GOOGLE_CRED_USR}"
        GOOGLE_CLIENT_SECRET="${GOOGLE_CRED_PSW}"
        UTFPR_SECRET="123456"

        EMAIL_CRED = credentials('email_token_id')
        UTFPR_TOKEN_SECRET="${EMAIL_CRED_PSW}"
        UTFPR_EMAIL_ADDRESS="${EMAIL_CRED_USR}"
        UTFPR_EMAIL_PASSWORD="${POSTGRESQL_CRED_PSW}"
    }

    stages {   
        stage('Docker Compose UP') {
            steps {
                sh 'docker compose up -d '
            }
        }
    }
}
