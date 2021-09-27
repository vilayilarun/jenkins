FROM nginx:latest
EXPOSE 80
WORKDIR /usr/share/nginx/html
copy ./sample.html $WORKDIR

