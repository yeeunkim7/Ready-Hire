#!/bin/bash
# EC2 Ubuntu 초기 세팅 스크립트

# 패키지 업데이트
sudo apt-get update -y
sudo apt-get upgrade -y

# Docker 설치
sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ubuntu

# 디렉토리 생성
mkdir -p /home/ubuntu/ready-hire

# .env 파일 생성 (값은 직접 입력)
cat > /home/ubuntu/ready-hire/.env << 'EOF'
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=3600
JWT_REFRESH_TOKEN_EXPIRATION_SECONDS=1209600
OPENAI_API_KEY=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
OAUTH2_REDIRECT_URI=
PORTONE_V2_API_SECRET=
PORTONE_CHANNEL_KEY=
EOF

echo "EC2 세팅 완료! .env 파일에 실제 값을 입력해주세요."
echo "위치: /home/ubuntu/ready-hire/.env"
