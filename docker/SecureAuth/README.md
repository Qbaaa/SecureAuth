## Running
cd .\docker\SecureAuth

docker-compose --env-file .env.local up -d --build --force-recreate postgres

docker-compose down postgres

docker rmi $(docker images -f "dangling=true" -q)  
docker volume rm $(docker volume ls -qf dangling=true)
