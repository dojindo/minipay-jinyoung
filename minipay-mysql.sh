sudo docker run -d \
  --name minipay-mysql \
  -e MYSQL_ROOT_PASSWORD=1234 \
  -e MYSQL_DATABASE=minipay \
  -e MYSQL_USER=topy \
  -e MYSQL_PASSWORD=1234 \
  -p 3306:3306 \
  mysql:latest