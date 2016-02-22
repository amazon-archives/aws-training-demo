#/bin/bash
export USERNAME=mydockeruserid
sudo yum update -y

sudo yum install -y docker

sudo service docker start

sudo usermod -a -G docker ec2-user
#need logout and re-login

docker info

git clone https://github.com/awslabs/ecs-demo-php-simple-app

cd ecs-demo-php-simple-app

cat Dockerfile

docker build -t $USERNAME/amazon-ecs-sample .

docker images

docker run -p 80:80 my-dockerhub-username/amazon-ecs-sample

docker login

docker info

docker push $USERNAME/amazon-ecs-sample