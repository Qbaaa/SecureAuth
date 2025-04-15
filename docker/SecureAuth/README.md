## Running
cd .\docker\SecureAuth

docker compose --env-file .env.local --profile db up -d --build --force-recreate
docker-compose down

docker-compose --env-file .env.local up -d --build --force-recreate postgres
docker-compose down postgres

docker-compose up -d --build --force-recreate mail
docker-compose down mail

docker rmi $(docker images -f "dangling=true" -q)  
docker volume rm $(docker volume ls -qf dangling=true)


