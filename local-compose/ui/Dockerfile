FROM nginx
EXPOSE 80
RUN apt-get update && apt-get install -y vim && apt-get install -y curl
WORKDIR /usr/share/nginx/html
ADD front-end/target/scala-2.13/scalajs-bundler/main/dist .
WORKDIR /etc/nginx/
ADD local-compose/ui/main.cfg nginx.conf
ADD local-compose/ui/nginx.cfg conf.d/default.conf
# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log && ln -sf /dev/stderr /var/log/nginx/error.log